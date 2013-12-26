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
package org.mc4j.ems.connection.bean;

import java.util.List;
import java.util.SortedSet;

import org.mc4j.ems.connection.bean.attribute.EmsAttribute;
import org.mc4j.ems.connection.bean.notification.EmsNotification;
import org.mc4j.ems.connection.bean.operation.EmsOperation;
import org.mc4j.ems.connection.support.ConnectionProvider;

/**
 * An MBean.
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 4, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public interface EmsBean extends Comparable {

    /**
     * Returns the name of this MBean as an EmsBeanName (analogous to <tt>javax.management.ObjectName</tt>; never null.
     *
     * @return the name of this MBean as an EmsBeanName (analogous to <tt>javax.management.ObjectName</tt>; never null
     */
    EmsBeanName getBeanName();

    /**
     * Returns the connection provider that was used to load this MBean; never null.
     *
     * @return the connection provider that was used to load this MBean; never null
     */
    ConnectionProvider getConnectionProvider();

    /**
     * Returns a proxy for this MBean, typed to the specified interface (typically its MBean interface); never null.
     * Example Usage:
     * <code>
     * FooMBean fooMBean = getProxy(FooMBean.class);
     * </code>
     *
     * @param beanInterface the interface class that the proxy should implement
     * @param <T> the interface that the proxy should implement
     *
     * @return a proxy for this MBean, typed to the specified interface (typically its MBean interface); never null
     */
    <T> T getProxy(Class<T> beanInterface);

    /**
     * Loads local representations of this MBean's attributes, operations and notifications if not already loaded.
     * Current attribute values are not retrieved.
     */
    void loadSynchronous() ;

    /**
     * Unloads any cached metadata associated with this MBean. This is useful for handling dynamic mbeans with changing
     * descriptors or corner case situations where an MBean is unregistered and then another MBean with a non-equivalent
     * MBeanInfo is registered with the same ObjectName.
     *
     * @since 1.3
     */
    void unload();

    /**
     * Returns the set of all attributes for this MBean; never null.
     *
     * @return the set of all attributes for this MBean; never null
     */
    SortedSet<EmsAttribute> getAttributes();

    /**
     * Refresh, from the server, all attribute values for this MBean and return the set of attributes; never null.
     *
     * @return the list of all attributes, with updated values from the server; never null
     */
    List<EmsAttribute> refreshAttributes();

    /**
     * Return a specific subset of EmsAttributes for a bean based on
     * the requested list of attributes by name.
     * This method can be used to load a group of attributes with a single
     * server call. Attributes updated in this fashion do update their
     * internal representation and do fire events on the changes. Never null.
     *
     * @param names the names of attributes to load
     *
     * @return the list of requested attributes, with updated values from the server; never null
     */
    List<EmsAttribute> refreshAttributes(List<String> names);

    /**
     * Returns the attribute with the specified name, or null if this MBean has no such attribute.
     *
     * @param name the attribute name
     *
     * @return the attribute with the specified name, or null if this MBean has no such attribute
     */
    EmsAttribute getAttribute(String name);

    /**
     * The fully qualified class name of this MBean's interface (e.g. "com.example.FooMBean"), as defined in its
     * MBeanInfo.
     *
     * @return the name of this MBean's Java interface (e.g. "com.example.FooMBean"), as defined in its MBeanInfo
     */
    String getClassTypeName();

    /**
     * This MBean's interface class (e.g. com.example.FooMBean), as defined in its MBeanInfo.
     *
     * @return this MBean's interface class (e.g. com.example.FooMBean), as defined in its MBeanInfo
     *
     * @throws ClassNotFoundException if the MBean's interface class could not be found in our class loader
     */
    Class getClassType() throws ClassNotFoundException;

    /**
     * Returns the set of all operations for this MBean; never null.
     *
     * @return the set of all operations for this MBean; never null
     */
    SortedSet<EmsOperation> getOperations();

    /**
     * Returns an operation with the specified name, or null if this MBean has no such operations.
     * <b>NOTE: </b> If the MBean has more than one operation defined with the specified name, the operation that is
     * returned is indeterminate, which is why this method has been deprecated in favor of
     * {@link #getOperation(String, Class[])}.
     *
     * @param name the operation name
     *
     * @return an operation with the specified name, or null if this MBean has no such operations
     *
     * @deprecated {@link #getOperation(String, Class[])} should be used instead
     */
    EmsOperation getOperation(String name);

    /**
     * Returns the operation with the specified name and parameter types, or null if this MBean has no such operation.
     *
     * @param name the operation name
     * @param parameterTypes the operation parameter types
     *
     * @return the operation with the specified name and parameter types, or null if this MBean has no such operation
     */
    EmsOperation getOperation(String name, Class... parameterTypes);

    /**
     * Returns the set of all notifications for this MBean; never null.
     *
     * @return the set of all notifications for this MBean; never null
     */
    SortedSet<EmsNotification> getNotifications();

    /**
     * Returns the notification with the specified name, or null if this MBean has no such notification.
     *
     * @param name the notification name
     *
     * @return the notification with the specified name, or null if this MBean has no such notification
     */
    EmsNotification getNotification(String name);

    /**
     * Unregisters this MBean from the server.
     */
    void unregister();

    /**
     * This sends a request to the server to check that this
     * MBean is still registered on the server.
     *
     * @return true if the MBean is still registered in the MBeanServer
     */
    boolean isRegistered();

    /**
     * Returns true if this MBean defines one or more notifications, otherwise returns false.
     *
     * @return true if this MBean defines one or more notifications, otherwise returns false
     */
    boolean isNotificationEmiter();

    /**
     * Returns true if this MBean has one or more attributes or operations with unsupported types, otherwise returns
     * false.
     *
     * @return true if this MBean has one or more attributes or operations with unsupported types, otherwise returns
     *         false
     */
    boolean isHasUnsupportedType();
}
