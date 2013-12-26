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

package org.mc4j.ems.impl.jmx.connection.support.providers;

import java.lang.reflect.Method;
import java.util.Hashtable;

import javax.management.MBeanServer;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.mc4j.ems.impl.jmx.connection.support.providers.proxy.GenericMBeanServerProxy;

/**
 * This Node acts as a connection to a WebLogic(tm) MBean Server.
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), March 2002
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class WeblogicConnectionProvider extends AbstractConnectionProvider {

    protected MBeanServer mbeanServer;


    public void doConnect() throws Exception {

        Context ctx = null;
        System.setProperty("jmx.serial.form", "1.0");

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();        
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());


            // Class.forName(super.initialContextFactory, true, loader);

            Hashtable props = new Hashtable();
            props.put(Context.INITIAL_CONTEXT_FACTORY, connectionSettings.getInitialContextName());
            props.put(Context.PROVIDER_URL, connectionSettings.getServerUrl());
            props.put(Context.SECURITY_PRINCIPAL, connectionSettings.getPrincipal());
            props.put(Context.SECURITY_CREDENTIALS, connectionSettings.getCredentials());

            ctx = new InitialContext(props);


            //Class homeClass = Class.forName("weblogic.management.MBeanHome");
            //Field field = homeClass.getField("ADMIN_JNDI_NAME");
            //String adminJndiName = (String) field.get(null);

            //String adminJndiName = "weblogic.management.adminhome";
            Object home = ctx.lookup(connectionSettings.getJndiName());

            Class homeClass = home.getClass();

            Method method = homeClass.getMethod("getMBeanServer",new Class[] { } );

            Object mbeanServerObject = method.invoke(home, new Object[] { } );
            GenericMBeanServerProxy proxy = new GenericMBeanServerProxy(mbeanServerObject);
            setStatsProxy(proxy);
            this.mbeanServer = proxy.buildServerProxy();

        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public MBeanServer getMBeanServer() {
        return this.mbeanServer;
    }


}
