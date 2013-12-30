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
package org.mc4j.ems.impl.jmx.connection.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mc4j.ems.connection.EmsBeanNotFoundException;
import org.mc4j.ems.connection.EmsUnsupportedTypeException;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.EmsBeanName;
import org.mc4j.ems.connection.bean.attribute.EmsAttribute;
import org.mc4j.ems.connection.bean.notification.EmsNotification;
import org.mc4j.ems.connection.bean.operation.EmsOperation;
import org.mc4j.ems.connection.bean.parameter.EmsParameter;
import org.mc4j.ems.impl.jmx.connection.bean.attribute.DAttribute;
import org.mc4j.ems.impl.jmx.connection.bean.attribute.DUnkownAttribute;
import org.mc4j.ems.impl.jmx.connection.bean.notification.DNotification;
import org.mc4j.ems.impl.jmx.connection.bean.operation.DOperation;
import org.mc4j.ems.impl.jmx.connection.support.providers.AbstractConnectionProvider;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 4, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class DMBean implements EmsBean {

    private static Log log = LogFactory.getLog(DMBean.class);

    protected AbstractConnectionProvider connectionProvider;

    protected ObjectName objectName;

    protected EmsBeanName beanName;

    private MBeanInfo info;
    private boolean loaded = false;
    private boolean unsupportedType = false;
    protected boolean deleted = false;

    private Map<String, EmsAttribute> attributes;// = new TreeMap<String, EmsAttribute>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, EmsOperation> operations;// = new TreeMap<String, EmsOperation>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, EmsNotification> notifications;// = new TreeMap<String, EmsNotification>(String.CASE_INSENSITIVE_ORDER);

    protected List<Throwable> failures;

    public DMBean(AbstractConnectionProvider connectionProvider, ObjectName objectName) {
        this.connectionProvider = connectionProvider;
        this.objectName = objectName;
        this.beanName = new DBeanName(objectName);
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public EmsBeanName getBeanName() {
        return beanName;
    }

    public AbstractConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    public List<Throwable> getFailures() {
        return failures;
    }

    protected void registerFailure(Throwable t) {
        if (failures == null) {
            failures = new LinkedList<Throwable>();
        }
        failures.add(t);
        log.debug("MBean access failure", t);
    }

    public boolean isHasUnsupportedType() {
        return unsupportedType;
    }

    public boolean isNotificationEmiter() {
        try {
            return connectionProvider.getMBeanServer().isInstanceOf(getObjectName(), "javax.management.NotificationEmitter");
        } catch (InstanceNotFoundException e) {
            throw new EmsBeanNotFoundException("Bean doesn't exist", e);
        }
    }

    /**
     * @param beanInterface
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> beanInterface) {
        throw new UnsupportedOperationException("Proxies not supported pre-jmx 1.2");
    }

    public synchronized void loadSynchronous() {
        if (!loaded) {
            try {
                info = connectionProvider.getMBeanServer().getMBeanInfo(this.objectName);

                if (info.getAttributes().length>0) {

                    this.attributes =  new TreeMap<String, EmsAttribute>(String.CASE_INSENSITIVE_ORDER);
                    for (MBeanAttributeInfo attributeInfo : info.getAttributes()) {
                        DAttribute attribute = new DAttribute(attributeInfo, this);
                        this.attributes.put(attributeInfo.getName(), attribute);
                    }
                }

                if (info.getOperations().length > 0) {
                    this.operations = new TreeMap<String, EmsOperation>(String.CASE_INSENSITIVE_ORDER);
                    for (MBeanOperationInfo operationInfo : info.getOperations()) {
                        DOperation operation = new DOperation(operationInfo, this);
                        this.operations.put(operationInfo.getName(), operation);
                    }
                }

                if (info.getNotifications().length>0) {
                    this.notifications = new TreeMap<String, EmsNotification>(String.CASE_INSENSITIVE_ORDER);
                    for (MBeanNotificationInfo notificationInfo : info.getNotifications()) {
                        DNotification notification = new DNotification(notificationInfo, this);
                        this.notifications.put(notificationInfo.getName(), notification);
                    }
                }

            } catch (InstanceNotFoundException infe) {
                this.deleted = true;

            } catch (Exception e) {
                unsupportedType = true;
                RuntimeException f = new EmsUnsupportedTypeException("Could not load MBean info, unsupported type on bean " + objectName, e);
                // TODO: Memory Leak below... don't do that
                //registerFailure(f);
                // TODO should we throw this here?
                //throw f;
            } finally {
                loaded = true;
            }
        }
    }

    public void unload() {
        if (loaded) {
            loaded = false;
            info = null;
            attributes.clear();
            operations.clear();
            notifications.clear();
        }
    }

    public boolean isRegistered() {

        MBeanServer server = getConnectionProvider().getMBeanServer();

        return server.isRegistered(getObjectName());
    }

    public EmsAttribute getAttribute(String name) {
        if (!loaded)
            loadSynchronous();

        EmsAttribute attribute = this.attributes.get(name);
        if (attribute == null && unsupportedType) {
            attribute = new DUnkownAttribute(this, name);
            this.attributes.put(name,attribute);
        }

        return attribute;
    }

    public SortedSet<EmsAttribute> getAttributes() {
        if (!loaded)
            loadSynchronous();
        return new TreeSet<EmsAttribute>(this.attributes.values());
    }

    protected boolean hasUnsupportedType = false;


    public List<EmsAttribute> refreshAttributes() {
        if (info == null)
            loadSynchronous();

        MBeanAttributeInfo[] infos = new MBeanAttributeInfo[0];
        try {
            infos = this.info.getAttributes();
        } catch (RuntimeException e) {
            // If this throws an exception, there's a good chance our cached
        }

        // MUST be careful to only ask for types that we know we have
        // otherwise the RMI call will fail and we will get no data.
        List<String> nameList = new ArrayList<String>();
        for (MBeanAttributeInfo info : infos) {
            try {
                findType(info.getType());
                // If we know the type, add it to the list
                nameList.add(info.getName());
            } catch (ClassNotFoundException cnfe) {
                log.info("Can't load attribute type of [" + info.getName() + "] because class not locally available");
            }
        }

        return refreshAttributes(nameList);
    }


    public List<EmsAttribute> refreshAttributes(List<String> attributeNames) {
        if (info == null)
            loadSynchronous();

        MBeanServer server = getConnectionProvider().getMBeanServer();

        try {
            String[] names = attributeNames.toArray(new String[attributeNames.size()]);

            AttributeList attributeList =
                    server.getAttributes(
                            getObjectName(),
                            names);

            List<EmsAttribute> attributeResults = new ArrayList<EmsAttribute>();

            Iterator iter = attributeList.iterator();
            while (iter.hasNext()) {
                Attribute attr = (Attribute) iter.next();
                EmsAttribute attribute = getAttribute(attr.getName());

                attribute.alterValue(attr.getValue());

//                if (!values.containsKey(attribute.getName())) {
//                    attribute.setSupportedType(false);
//                }

                attributeResults.add(attribute);

            }
            return attributeResults;
        } catch (InstanceNotFoundException infe) {
            this.deleted = true;
            throw new RuntimeException("Unable to load attributes, bean not found", infe);
        } catch (Exception e) {
            // TODO: determine which exceptions to register, which to throw and what to log
//                e.printStackTrace();

            // Don't load them as a set anymore...
            this.hasUnsupportedType = true;
            //System.out.println(ExceptionUtility.printStackTracesToString(e));

            // If we still we're unable to load all the attributes at once
            // lets load as many as we can, one at a time.
//            for (EmsAttribute attribute : getAttributes()) {
//                attribute.refresh();
//            }

            throw new RuntimeException("Unable to load attributes on bean [" + getBeanName().toString() + "] " + e.getMessage(), e);
        }
//        } else {
//            // If the object has unsupported attribute types
//            // lets load as many as we can, one at a time.
//            System.out.println("Loading individually: " + getObjectName().getCanonicalName());
//            for (EmsAttribute attribute : getAttributes()) {
//                attribute.refresh();
//            }
//        }

    }

    public String getClassTypeName() {
        return getMBeanInfo().getClassName();
    }

    private static final Map TYPES = new HashMap();

    static {
        TYPES.put(Boolean.TYPE.getName(), Boolean.TYPE);
        TYPES.put(Character.TYPE.getName(), Character.TYPE);
        TYPES.put(Byte.TYPE.getName(), Byte.TYPE);
        TYPES.put(Short.TYPE.getName(), Short.TYPE);
        TYPES.put(Integer.TYPE.getName(), Integer.TYPE);
        TYPES.put(Long.TYPE.getName(), Long.TYPE);
        TYPES.put(Float.TYPE.getName(), Float.TYPE);
        TYPES.put(Double.TYPE.getName(), Double.TYPE);
        TYPES.put(Void.TYPE.getClass(), Void.TYPE);
    }

    public static Class findType(String className) throws ClassNotFoundException {

        if (TYPES.containsKey(className)) {
            return (Class) TYPES.get(className);
        } else {
            return Class.forName(className, true, DMBean.class.getClassLoader());
        }
    }

    public Class getClassType() throws ClassNotFoundException {
        String className = getMBeanInfo().getClassName();
        return findType(className);
    }

    public EmsOperation getOperation(String name) {
        if (info == null) loadSynchronous();
        return this.operations.get(name);
    }

    /**
     * Retrieves an operation from an mbean which matches the specified name
     * and signature. The method is modeled after Class.getMethod()
     * The order of parameterTypes must much the order of arguments returned
     * for the operation by EMS.
     *
     * If not matching operation is found on the EmsBean then null is returned
     */
    public EmsOperation getOperation(String operationName, Class... parameterTypes) {
        getOperations();
        if (operations == null || operations.isEmpty()) {
            return null;
        }

        String[] parameterTypeNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            // TODO check whether this should be getName,getCanonicalName,
            // getSimpleName
            // just needs to match what EMS returns
            parameterTypeNames[i] = parameterTypes[i].getName();
        }
        EmsOperation selectedOperation = null;
        for (EmsOperation operation : operations.values()) {
            if (!operation.getName().equals(operationName)) {
                continue;
            }

            List<EmsParameter> operationParameters = operation.getParameters();
            if (parameterTypeNames.length != operationParameters.size()) {
                // different number of parameters to what we are looking for
                continue;
            }
            String[] operationParameterTypeNames = new String[operationParameters.size()];
            int i = 0;
            for (EmsParameter operationParameter : operationParameters) {
                operationParameterTypeNames[i] = operationParameter.getType();
                i++;
            }

            if (Arrays.equals(parameterTypeNames, operationParameterTypeNames)) {
                selectedOperation = operation;
                break;
            }
        }
        return selectedOperation;
    }


    public SortedSet<EmsOperation> getOperations() {
        if (info == null) loadSynchronous();
        return new TreeSet<EmsOperation>(this.operations.values());
    }

    public EmsNotification getNotification(String name) {
        if (info == null) loadSynchronous();
        return this.notifications.get(name);
    }

    public SortedSet<EmsNotification> getNotifications() {
        if (info == null) loadSynchronous();
        return new TreeSet<EmsNotification>(this.notifications.values());
    }

    public void unregister() {
        try {
            connectionProvider.getMBeanServer().unregisterMBean(getObjectName());
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            throw new EmsBeanNotFoundException("Could not unregister bean, instance not found [" + getObjectName().getCanonicalName() + "]", e);
        }
        // TODO: Shouldn't we remove the MBean from our map too?
        //connectionProvider.getExistingConnection().removeMBean(getObjectName().toString());
    }

    protected MBeanInfo getMBeanInfo() {
        if (info == null) loadSynchronous();
        return info;
    }


    public int compareTo(Object o) {
        DMBean otherBean = (DMBean) o;
        return this.getObjectName().getCanonicalName().compareTo(
                otherBean.getObjectName().getCanonicalName());
    }

    public String toString() {
        return this.getBeanName().toString();
    }
}
