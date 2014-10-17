/*
 * Copyright 2002-2004 Greg Hinkle
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

package org.mc4j.ems.impl.jmx.connection.bean.attribute;

import java.io.NotSerializableException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mc4j.ems.connection.EmsException;
import org.mc4j.ems.connection.bean.attribute.AttributeChangeListener;
import org.mc4j.ems.connection.bean.attribute.EmsAttribute;
import org.mc4j.ems.impl.jmx.connection.bean.DMBean;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 4, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 *
 * TODO: Implement Comparable.
 */
public class DAttribute implements EmsAttribute {

    private static Log log = LogFactory.getLog(DAttribute.class);

    protected MBeanAttributeInfo info;
    protected DMBean bean;
    protected boolean loaded;

    protected boolean supportedType = true;

    protected Object currentValue;

    protected LinkedList<Throwable> failures;


    public DAttribute(MBeanAttributeInfo info, DMBean bean) {
        this.info = info;
        this.bean = bean;
        init();
    }

    /**
     * Initializes internal storage settings for the value history
     */
    protected void init() {
    }

    private String getControlProperty(String property,String defaultValue) {
        return bean.getConnectionProvider().getConnectionSettings().getControlProperties().getProperty(property,defaultValue);
    }


    // TODO GH: Should you be able to register for a certain frequency? and then be guaranteed that the
    // notifications won't be faster than that? Then the requests could be grouped as well
    public synchronized void registerAttributeChangeListener(AttributeChangeListener listener) {
//        if (changeListeners == null)
//            changeListeners = new HashSet<AttributeChangeListener>();
//
//        changeListeners.add(listener);
    }


    public Object getValue() {
        if (!loaded)
            refresh();
        return this.currentValue;
    }

    public int getValueSize() {
        return 0;
    }

    /**
     * Set the attribute on the server
     *
     * @param newValue The value to be set
     * @throws Exception
     */
    public void setValue(Object newValue) throws Exception {

        try {
            MBeanServer server = bean.getConnectionProvider().getMBeanServer();
            server.setAttribute(bean.getObjectName(),
                    new Attribute(getName(), newValue));
            alterValue(newValue);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
        refresh();
    }

    /**
     * Alters the internally stored value of this attribute. Does not update the
     * server value. Is intended for mass load of attribute data via the MBean.
     *
     * @param newValue
     */
    public void alterValue(Object newValue) {
        if ((newValue != null && !newValue.equals(currentValue)) || (newValue == null && currentValue != null)) {

            currentValue = newValue;
        }
    }

    protected boolean storeHistory(Object value) {
        if (value instanceof Number)
            return true;

        // TODO GH: Store statistics, possibly certain open mbean types

        return false;
    }

    /**
     * Updates the local value of this mbean from the server
     * <p/>
     * TODO we should not update to null on failure, but retain the last known
     */
    public synchronized Object refresh() {
        loaded = true;
        Object newValue = null;
        try {
            MBeanServer server = bean.getConnectionProvider().getMBeanServer();
            newValue = server.getAttribute(bean.getObjectName(), getName());

        } catch (ReflectionException e) {
            supportedType = false;
            registerFailure(e);
            throw new EmsException("Could not load attribute value " + e.toString(),e);
        } catch (InstanceNotFoundException e) {
            registerFailure(e);
            throw new EmsException("Could not load attribute value, bean instance not found " + bean.getObjectName().toString(),e);
        } catch (MBeanException e) {
            registerFailure(e);
            Throwable t = e.getTargetException();
            if (t != null)
                throw new EmsException("Could not load attribute value, target bean threw exception " + t.getLocalizedMessage(),t);
            else
                throw new EmsException("Could not load attribute value " + e.getLocalizedMessage(), e);
        } catch (AttributeNotFoundException e) {
            registerFailure(e);
            throw new EmsException("Could not load attribute value, attribute [" + getName() + "] not found",e);
        } catch(UndeclaredThrowableException e) {
            if (e.getUndeclaredThrowable() instanceof InvocationTargetException) {
                Throwable t = e.getCause();
                if (t.getCause() instanceof NotSerializableException) {
                    supportedType = false;
                    registerFailure(t.getCause());
                    throw new EmsException("Could not load attribute value " + t.getLocalizedMessage(),t.getCause());
                } else
                    throw new EmsException("Could not load attribute value " + t.getLocalizedMessage(),t);
            }
            throw new EmsException("Could not load attribute value " + e.getLocalizedMessage(),e);
        } catch (RuntimeException re) {
            supportedType = false;

            // TODO GH: Figure this one out
            // Getting weblogic.management.NoAccessRuntimeException on wl9
            registerFailure(re);
            throw new EmsException("Could not load attribute value " + re.getLocalizedMessage(),re);
        } catch (NoClassDefFoundError ncdfe) {
            supportedType = false;
            registerFailure(ncdfe);
            throw new EmsException("Could not load attribute value " + ncdfe.getLocalizedMessage(),ncdfe);
        } catch (Throwable t) {
            throw new EmsException("Could not load attribute value " + t.getLocalizedMessage(),t);
        }
        alterValue(newValue);
        return newValue;
    }

    /**
     * TODO GH: Should this be a list of failure objects that has more info or
     * perhaps a custom exception with the info? (timestamp, bean name, attribute name)
     * TODO GH: Should this be all failures, retrieval failures, what about set failures?
     * TODO GH: Should this be genericised for the server proxy objects?
     *
     * @return failures of interaction with server related to this attribute
     */
    public List<Throwable> getFailures() {
        return Collections.unmodifiableList(failures);
    }

    protected void registerFailure(Throwable t) {
        if (failures == null)
            failures = new LinkedList<Throwable>();
        failures.add(t);

        // Bounding this list to make sure memory doesn't grow
        if (failures.size() > 2)
            failures.removeFirst();

        log.debug("Attribute access failure " + t.getLocalizedMessage(),t);
    }


    public String getName() {
        return info.getName();
    }

    public String getType() {
        return info.getType();
    }

    private static final Set<Class> NUMERIC_TYPES = new HashSet();

    static {
        NUMERIC_TYPES.add(Short.TYPE);
        NUMERIC_TYPES.add(Short.class);
        NUMERIC_TYPES.add(Integer.TYPE);
        NUMERIC_TYPES.add(Integer.class);
        NUMERIC_TYPES.add(Long.TYPE);
        NUMERIC_TYPES.add(Long.class);
        NUMERIC_TYPES.add(Float.TYPE);
        NUMERIC_TYPES.add(Float.class);
        NUMERIC_TYPES.add(Double.TYPE);
        NUMERIC_TYPES.add(Double.class);
        NUMERIC_TYPES.add(BigInteger.class);
        NUMERIC_TYPES.add(BigDecimal.class);
    }

    private static final Map<String, Class> TYPES = new HashMap<String,Class>();

    static {
        TYPES.put(Boolean.TYPE.getName(), Boolean.TYPE);
        TYPES.put(Character.TYPE.getName(), Character.TYPE);
        TYPES.put(Byte.TYPE.getName(), Byte.TYPE);
        TYPES.put(Short.TYPE.getName(), Short.TYPE);
        TYPES.put(Integer.TYPE.getName(), Integer.TYPE);
        TYPES.put(Long.TYPE.getName(), Long.TYPE);
        TYPES.put(Float.TYPE.getName(), Float.TYPE);
        TYPES.put(Double.TYPE.getName(), Double.TYPE);
        TYPES.put(Void.TYPE.getName(), Void.TYPE);
    }

    public Class getTypeClass() {
        if (TYPES.containsKey(getType())) {
            return TYPES.get(getType());
        } else {
            // TODO: Switch to using ConnectionProvider.getClassloader(), oh and implement that too
            try {
                return Class.forName(getType(), true, getClass().getClassLoader());
            } catch (ClassNotFoundException e) {
                return null; // TODO: Unkown type, how to handle?
            }
        }
    }

    public boolean isNumericType() {
        return NUMERIC_TYPES.contains(getTypeClass());
    }


    public String getDescription() {
        return info.getDescription();
    }

    public boolean isWritable() {
        return info.isWritable();
    }

    public boolean isReadable() {
        return info.isReadable();
    }

    public boolean isSupportedType() {
        return supportedType;
    }

    public void setSupportedType(boolean supportedType) {
        this.supportedType = supportedType;
    }

    public int compareTo(Object o) {
        DAttribute otherAttribute = (DAttribute) o;
        return this.getName().compareTo(
                otherAttribute.getName());
    }
}
