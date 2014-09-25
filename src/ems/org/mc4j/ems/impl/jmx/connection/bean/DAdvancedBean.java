/*
 * Copyright 2002-2005 Greg Hinkle
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

import java.lang.reflect.Method;

import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.mc4j.ems.impl.jmx.connection.support.providers.AbstractConnectionProvider;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Nov 16, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class DAdvancedBean extends DMBean {

    protected Object mbeanProxy;


    public DAdvancedBean(AbstractConnectionProvider connectionProvider, ObjectName objectName) {
        super(connectionProvider, objectName);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> beanInterface) {

        if (mbeanProxy == null) {

            try {
                // 1.5 only stuff
                Class c = Class.forName("java.lang.management.ManagementFactory");
                Class mbsc = Class.forName("javax.management.MBeanServerConnection");
                Method m = c.getMethod("newPlatformMXBeanProxy",mbsc,String.class,Class.class);
                return (T) m.invoke(null,connectionProvider.getMBeanServer(), getBeanName().getCanonicalName(), beanInterface);

            } catch (Exception e) {
                // Expected if its not a platform mbean
                // e.printStackTrace();
            }

            MBeanServer server = getConnectionProvider().getMBeanServer();
            mbeanProxy =
                    MBeanServerInvocationHandler.newProxyInstance(server, getObjectName(), beanInterface, getNotifications().size() > 0);
        }
        return (T) mbeanProxy;
    }
}
