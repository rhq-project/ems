/*
 * Copyright 2002-2011 Greg Hinkle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mc4j.ems.impl.jmx.connection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mc4j.ems.connection.ConnectionTracker;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.EmsException;
import org.mc4j.ems.connection.EmsMalformedObjectNameException;
import org.mc4j.ems.connection.EmsUnsupportedTypeException;
import org.mc4j.ems.connection.MBeanRegistrationEvent;
import org.mc4j.ems.connection.MBeanRegistrationListener;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.support.ConnectionProvider;
import org.mc4j.ems.impl.jmx.connection.bean.DAdvancedBean;
import org.mc4j.ems.impl.jmx.connection.bean.DMBean;
import org.mc4j.ems.impl.jmx.connection.support.providers.AbstractConnectionProvider;

/**
 * TODO GH: Decide exception handling strategy (runtime?)
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 4, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class DConnection implements EmsConnection {

    private static Log log = LogFactory.getLog(DConnection.class);

    protected String connectionName;
    protected AbstractConnectionProvider connectionProvider;

    protected SortedMap<DObjectName, EmsBean> beanMap;

    protected boolean loaded;

    // TODO Do this with 1.4 support
//    protected PooledConnectionTracker tracker;

    protected List<MBeanRegistrationListener> registrationListeners = new ArrayList<MBeanRegistrationListener>();

    public DConnection(String connectionName, ConnectionProvider connectionProvider) {
        this.connectionName = connectionName;
        this.connectionProvider = (AbstractConnectionProvider) connectionProvider;
        beanMap = new TreeMap<DObjectName, EmsBean>(new DObjectNameComparator());

//        this.tracker = new PooledConnectionTracker(this);
    }

    public void setConnectionProvider(AbstractConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public ConnectionTracker getTracker() {
//        return tracker;
        return null;
    }

    public void refresh() {
        try {
            loadSynchronous(false);
        } catch (Exception e) {
            log.warn("Could not refresh connection [" + connectionProvider.getConnectionSettings().getServerUrl() + "] " + e.toString(), e);
        }
    }

    public void close() {

//        tracker.stopTracker();
        connectionProvider.disconnect();
        LogFactory.release(connectionProvider.getClass().getClassLoader());
    }

    /* TODO: Check WebLogic <= 8.1
       This was how mc4j worked since weblogic <= 8.1 won't allow a null search on queryNames,
       but will on queryMBeans
    if (connectionSettings.getConnectionType() instanceof WeblogicConnectionTypeDescriptor) {
        Set objectInstances = server.queryMBeans(null,null);
        objectNames = new HashSet();
        for (Iterator iterator = objectInstances.iterator(); iterator.hasNext();) {
            ObjectInstance objectInstance = (ObjectInstance) iterator.next();
            objectNames.add(objectInstance.getObjectName());
        }
    } else {
        objectNames = server.queryNames(null,null);
    }*/

    @SuppressWarnings({"unchecked"})
    public synchronized void loadSynchronous(boolean deep) {

        if (!connectionProvider.isConnected())
            connectionProvider.connect();

        log.info("Querying MBeanServer for all MBeans...");

        MBeanServer mBeanServer = connectionProvider.getMBeanServer();
        Set<ObjectName> objectNames = null;
        try {
            objectNames = (Set<ObjectName>) mBeanServer.queryNames(new ObjectName("*:*"), null);
        } catch (MalformedObjectNameException e) { /* Should never happen */ }

        SortedMap<ObjectName, DMBean> retrievedBeansMap = new TreeMap<ObjectName, DMBean>(new ObjectNameComparator());

        if (!loaded) {
            log.info("Found " + objectNames.size() + " MBeans - starting load...");
        }

        Set<DObjectName> currentKeys = new HashSet<DObjectName>(this.beanMap.keySet());

        for (ObjectName objectName : objectNames) {

            // TODO: We're loading the beans on every run here i think... only load it if its not in the beanMap

            DMBean bean = mapBean(objectName, deep);
            retrievedBeansMap.put(objectName, bean);
        }

        Set<EmsBean> newBeans = new HashSet<EmsBean>();
        Set<EmsBean> removedBeans = new HashSet<EmsBean>();

        for (Map.Entry<ObjectName, DMBean> entry : retrievedBeansMap.entrySet()) {
            if (!currentKeys.contains(entry.getKey())) {
                newBeans.add(entry.getValue());
            }
        }

        for (DObjectName name : currentKeys) {
            if (!retrievedBeansMap.containsKey(name.getObjectName())) {
                removedBeans.add(beanMap.remove(name));
            }
        }

        if (loaded && log.isDebugEnabled()) {
            log.debug("Added " + newBeans.size() + " and removed " + removedBeans.size() + " since previous load.");
        }

        loaded = true;
        fireRegistrationEvent(newBeans, removedBeans);
    }

    public void unload() {
        for (EmsBean bean : beanMap.values()) {
            bean.unload();
        }
    }

    static boolean isJMX12 = false;

    static {
        try {
            Class.forName("javax.management.MBeanServerInvocationHandler");
            isJMX12 = true;
        } catch (ClassNotFoundException e) {
        }
    }

    private DMBean mapBean(ObjectName objectName, boolean loadSynchronous) {
        DMBean bean = null;
        DObjectName dObjectName = new DObjectName(objectName);

        // If the bean is unknown to the internal map, create our local representation and add it
        synchronized (this) {
            if (!this.beanMap.keySet().contains(dObjectName)) {
                if (isJMX12) {
                    bean = new DAdvancedBean(connectionProvider, objectName);
                } else {
                    bean = new DMBean(connectionProvider, objectName);
                }
                beanMap.put(dObjectName, bean);
            }
        }

        // If the bean was just created then optional load its metadata synchronously
        // Do this outside the synchronized block
        if (bean != null && loadSynchronous) {
            try {
                bean.loadSynchronous();
            } catch (EmsUnsupportedTypeException e) {
                // Keep loading other beans even if one has an unsupported type
                log.info("Bean metadata not loaded, unsupported type on [" + objectName.toString() + "]", e);
            }
        }

        if (bean == null) {
            return (DMBean) this.beanMap.get(dObjectName);
        } else {
            return bean;
        }
    }


    private void fireRegistrationEvent(Set<EmsBean> added, Set<EmsBean> removed) {
        if (!registrationListeners.isEmpty()) {
            MBeanRegistrationEvent event = new MBeanRegistrationEvent(added, removed);
            for (MBeanRegistrationListener listener : registrationListeners) {
                listener.registrationChanged(event);
            }
        }
    }

    public synchronized void addRegistrationListener(MBeanRegistrationListener registrationListener) {
        registrationListeners.add(registrationListener);
    }

    public synchronized void removeRegistrationListener(MBeanRegistrationListener registrationListener) {
        registrationListeners.remove(registrationListener);
    }


    /**
     * This will register a new MBean, but that may not be immediately recognized
     *
     * @param className
     * @param objectName
     */
    public void createMBean(String className, String objectName)
            throws EmsException {
        ObjectName on = null;
        try {
            on = new ObjectName(objectName);

            connectionProvider.getMBeanServer().createMBean(className, on);
        } catch (Exception e) {
            throw new EmsException("Could not create MBean", e);
        }
        // TODO: Shouldn't we add the MBean to our map too?
    }

    public void removeMBean(String objectName) throws EmsException {
        ObjectName on = null;
        try {
            on = new ObjectName(objectName);

            connectionProvider.getMBeanServer().unregisterMBean(on);
        } catch (Exception e) {
            throw new EmsException("Could not remove MBean", e);
        }
        // TODO: Shouldn't we remove the MBean from our map too?
    }

    public synchronized SortedSet<EmsBean> getBeans() {
        if (!loaded) {
            refresh();
        }
        return new TreeSet<EmsBean>(beanMap.values());
    }

    protected EmsBean getBean(ObjectName objectName) {
        return this.beanMap.get(new DObjectName(objectName));
    }

    public EmsBean getBean(String objectName) {
        try {
            ObjectName name = new ObjectName(objectName);
            return getBean(name);
        } catch (MalformedObjectNameException e) {
            throw new EmsMalformedObjectNameException("Invalid ObjectName [" + objectName + "]",e);
        }
    }

    /**
     * This will run the query, creating our internal bean representation
     * as needed and return the full list of both previously and newly
     * mapped beans from the corresponding query.
     *
     * @param objectName
     * @param query
     * @return The list of EmsBeans representing mbeans that match the query
     */
    @SuppressWarnings({"unchecked"})
    public List<EmsBean> queryBeans(ObjectName objectName, QueryExp query) {

        Set<ObjectName> objectNames =
                (Set<ObjectName>) connectionProvider.getMBeanServer().queryNames(objectName, query);

        List<EmsBean> results = new ArrayList<EmsBean>();
        for (ObjectName name : objectNames) {
            results.add(mapBean(name, false));
        }
        return results;
    }

    /**
     * Utility to perform a query without ObjectName in your classpath
     *
     * @param objectName
     * @return the list of EmsBeans matching your query
     * @throws RuntimeException when the ObjectName is not valid
     */
    public List<EmsBean> queryBeans(String objectName) {
        try {
            ObjectName name = null;

            if (objectName != null)
                name = new ObjectName(objectName);


            return queryBeans(name, null);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException("Illegal ObjectName " + objectName, e);
        }
    }


    public EmsBean registerBean(String className, String objectName) {

        try {
            ObjectName objName = new ObjectName(objectName);
            ObjectInstance instance = connectionProvider.getMBeanServer().createMBean(className, objName);
            return mapBean(instance.getObjectName(), false);
        } catch (MBeanException e) {
            e.printStackTrace();
            throw new EmsException("Couldn't create MBean", e);
        } catch (OperationsException e) {
            throw new EmsException("Couldn't create MBean", e);
        } catch (ReflectionException e) {
            throw new EmsException("Couldn't create MBean", e);
        }
    }

    public EmsBean registerBean(String className, String objectName, Object[] params, String[] signature) {

        try {
            ObjectName objName = new ObjectName(objectName);
            ObjectInstance instance = connectionProvider.getMBeanServer().createMBean(className, objName, params, signature);
            return mapBean(instance.getObjectName(), false);
        } catch (MBeanException e) {
            e.printStackTrace();
            throw new EmsException("Couldn't create MBean", e);
        } catch (OperationsException e) {
            throw new EmsException("Couldn't create MBean", e);
        } catch (ReflectionException e) {
            throw new EmsException("Couldn't create MBean", e);
        }
    }


    public long getRoundTrips() {
        return connectionProvider.getRoundTrips();
    }

    public long getFailures() {
        return connectionProvider.getFailures();
    }

    public Object buildObjectName(String objectName) throws EmsMalformedObjectNameException {
        try {
            return new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            throw new EmsMalformedObjectNameException("Object Name not valid [" + objectName + "]", e);
        }
    }


    private static class ObjectNameComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }
    }

    public ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    /**
     * Some object name implementations are not equal if the properties are in different orders.
     * The RI compares cannonical property forms as expected, JBoss is broken.
     *
     * This class is a key to search and get around that potential issue by always key on
     * the cannoical form, but ordering by the default form.
     */
    public static class DObjectName {

        private ObjectName objectName;

        public DObjectName(ObjectName objectName) {
            this.objectName = objectName;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final DObjectName that = (DObjectName) o;

            return objectName.getCanonicalName().equals(that.objectName.getCanonicalName());
        }

        public int hashCode() {
            return objectName.getCanonicalName().hashCode();
        }

        public int compareTo(DObjectName o) {
            return toString().compareTo(o.toString());
        }

        public String toString() {
            return objectName.toString();
        }

        public ObjectName getObjectName() {
            return objectName;
        }
    }

    public static class DObjectNameComparator implements Comparator<DObjectName> {
        public int compare(DObjectName o1, DObjectName o2) {
            return o1.getObjectName().getCanonicalName().compareTo(o2.getObjectName().getCanonicalName());
        }
    }
}
