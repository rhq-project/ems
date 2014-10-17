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


import java.lang.reflect.Field;
import java.util.Hashtable;

import javax.management.MBeanServer;
import javax.management.j2ee.Management;
import javax.management.j2ee.ManagementHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.mc4j.ems.impl.jmx.connection.support.providers.proxy.GenericMBeanServerProxy;

/**
 * This Node acts as a connection to an OC4J MBean Server via a MEJB.
 * The MEJB connection is ontained using a J2EE ApplicationClient. This
 * involves a workaround which requires a META-INF/application-client.xml to be
 * present in one of the JAR files which the client is loading. The application-client.xml
 * file can be completely empty, it just needs to be present.
 * <p/>
 * Accordingly, an empty application-client.xml has been added to the src/etc directory
 * and is included in the mc4j_core.jar file when it is created.
 *
 * @author Steve Button(sbutton@users.sourceforge.net), March 2004
 * @version 1.0
 */
public class Oc4jConnectionProvider extends AbstractConnectionProvider {
    private static final boolean M_DEBUG = false;
    protected MBeanServer mbeanServer;
    protected Management mejb;

   
    /**
     * Connect to the OC4J ManagementEJB. This should only be used while the JSR160
     * interface is not available.
     *
     */
    protected void doConnect() throws Exception {

        log("Oc4jConnectionProvider");
        log(connectionSettings.toString());
        logMessage("ConnectionSettings: " + connectionSettings.toString());
        Context ctx = null;

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        try {
            //The OC4J connection type
            Hashtable env = new Hashtable();
            env.put(Context.PROVIDER_URL, connectionSettings.getServerUrl());
            env.put(Context.SECURITY_PRINCIPAL, connectionSettings.getPrincipal());
            env.put(Context.SECURITY_CREDENTIALS, connectionSettings.getCredentials());
            env.put(Context.INITIAL_CONTEXT_FACTORY, connectionSettings.getInitialContextName());

            Context oc4jctx = new InitialContext(env);

            Object obj = (ManagementHome) oc4jctx.lookup(connectionSettings.getJndiName());
            ManagementHome mgmtHome = (ManagementHome) PortableRemoteObject.narrow(obj, ManagementHome.class);
            Management oc4jmbs = mgmtHome.create();
            GenericMBeanServerProxy proxy = new GenericMBeanServerProxy(getMBeanServer());
            setStatsProxy(proxy);
            this.mbeanServer = proxy.buildServerProxy();

            this.mejb = oc4jmbs;

            super.connect();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public String getConstant(Class clazz, String name) throws Exception {
        Field field = clazz.getField(name);
        return (String) field.get(null);
    }

    public void doDisconnect() {
        //this.connector.close();
    }

    public Management getMEJB() {
        return this.mejb;
    }

    public MBeanServer getMBeanServer() {
        return this.mbeanServer;
    }

    private void logMessage(String msg) {
        if (!M_DEBUG)
            return;
       /* NotifyDescriptor d =
            new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(d);*/
    }

    private void log(String msg) {
        if (!M_DEBUG)
            return;
        System.out.println(msg);
    }


}
