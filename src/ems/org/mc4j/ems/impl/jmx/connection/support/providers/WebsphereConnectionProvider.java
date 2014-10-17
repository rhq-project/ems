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
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.j2ee.Management;
import javax.naming.Context;
import javax.naming.NamingException;

import org.mc4j.ems.impl.jmx.connection.support.providers.proxy.GenericMBeanServerProxy;

/**
 * This Node acts as a connection to a WebSphere(tm) MBean Server (TMX4J based).
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), January 2004
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class WebsphereConnectionProvider extends AbstractConnectionProvider {

    protected GenericMBeanServerProxy statsProxy;
    protected MBeanServer mbeanServer;
    private Management mejb;

    private static final String MEJB_JNDI = "ejb/mgmt/MEJB";


    protected void doConnect() throws Exception {
        Context ctx = null;

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        try {

            //System.setProperty("jmx.serial.form", "1.0");

            /* From a WS admin article
            Properties clientProps = new Properties();
            connectProps.setProperty(AdminClient.CONNECTOR_TYPE,
                                     AdminClient.CONNECTOR_TYPE_SOAP);
            connectProps.setProperty(AdminClient.CONNECTOR_HOST, "localhost");
            connectProps.setProperty(AdminClient.CONNECTOR_PORT, "8879");

            AdminClient adminClient = null;
            try
            {
               adminClient = AdminClientFactory.createAdminClient(clientProps);
            }
            catch (ConnectorException e)
            {
               System.out.println("Exception creating admin client: " + e);
            }
            */

            Class adminClientClass =
                Class.forName("com.ibm.websphere.management.AdminClient", true, this.getClass().getClassLoader());
            Class adminClientFactoryClass =
                Class.forName("com.ibm.websphere.management.AdminClientFactory");


            // TODO GH: LATEST! This works from a SUN VM...
            // Autodetect the vm and suggest the correct factory
//            Hashtable env = new Hashtable();
//            env.put(Context.INITIAL_CONTEXT_FACTORY,
//                 "com.sun.jndi.cosnaming.CNCtxFactory");
//            env.put(Context.PROVIDER_URL, "corbaname:iiop:localhost:2809/NameServiceServerRoot");
            //env.put(Context.PROVIDER_URL, "iiop://localhost:2809/NameServiceServerRoot");
//            ctx = new InitialContext(env);
            //this.mejb = retrieveMEJB(ctx);



            /*
            Properties orbprops = new Properties();
            orbprops .put("org.omg.CORBA.ORBClass", "com.ibm.CORBA.iiop.ORB");
            orbprops .put("com.ibm.CORBA.ORBInitRef.NameService",
                  "corbaloc:iiop:localhost:2809/NameService");
            orbprops .put("com.ibm.CORBA.ORBInitRef.NameServiceServerRoot",
                  "corbaloc:iiop:localhost:2809/NameServiceServerRoot");
            ORB _orb = ORB.init((String[])null, orbprops );

            org.omg.CORBA.Object obj = _orb.resolve_initial_references("NameService");
            NamingContextExt initCtx = NamingContextExtHelper.narrow(obj);
            Object objref = initCtx.resolve_str("java:comp/env/ejb/mgmt/MEJB");
            ManagementHome home =
                (ManagementHome)PortableRemoteObject.narrow(objref,ManagementHome.class);
            this.mejb = home.create();
*/



            //props.put(Context.SECURITY_PRINCIPAL, connectionSettings.getPrincipal());
            //props.put(Context.SECURITY_CREDENTIALS, connectionSettings.getCredentials());


            Properties props = new Properties();
            URI serverUrl = new URI(connectionSettings.getServerUrl());

            if (serverUrl.getScheme().equalsIgnoreCase("http") || serverUrl.getScheme().equalsIgnoreCase("https")) {
                //System.setProperty("javax.net.debug", "ssl,handshake,data,trustmanager");
                //Security.addProvider(new sun.security.provider.Sun());
                System.setProperty("java.protocol.handler.pkgs","com.sun.net.ssl.internal.www.protocol");
                //System.setProperty("ssl.SocketFactory.provider", "javax.net.ssl.SSLSocketFactory");
                props.put(
                    getConstant(adminClientClass, "CONNECTOR_TYPE"),
                    getConstant(adminClientClass, "CONNECTOR_TYPE_SOAP"));
            } else {
                props.put(
                    getConstant(adminClientClass, "CONNECTOR_TYPE"),
                    getConstant(adminClientClass, "CONNECTOR_TYPE_RMI"));
            }

            String username = connectionSettings.getPrincipal(); 
            String password = connectionSettings.getCredentials(); 
            boolean security = ((username != null) && (!"".equals(username))); 
            if (security) { 
                props.setProperty(getConstant(adminClientClass, "CONNECTOR_SECURITY_ENABLED"), "true"); 
                props.setProperty(getConstant(adminClientClass, "USERNAME"), username); 
                props.setProperty(getConstant(adminClientClass, "PASSWORD"), password); 
            } else { 
                props.setProperty(getConstant(adminClientClass, "CONNECTOR_SECURITY_ENABLED"), "false");
            }

            props.put(
                getConstant(adminClientClass, "CONNECTOR_HOST"),
                serverUrl.getHost());
            props.put(
                getConstant(adminClientClass, "CONNECTOR_PORT"),
                String.valueOf(serverUrl.getPort()));

            Method createMethod =
                adminClientFactoryClass.getMethod("createAdminClient", Properties.class);

            Object adminClient =
                createMethod.invoke(null, props);

            this.statsProxy = new GenericMBeanServerProxy(adminClient);
            this.mbeanServer = statsProxy.buildServerProxy();

            //this.mejb = retrieveMEJB(ctx);

            // TODO GH: Customize exception and error messages to help
            // with typical problems (jsse jars missing, passwords, etc.)
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }



    public String getConstant(Class clazz, String name) throws Exception {
        Field field = clazz.getField(name);
        return (String) field.get(null);
    }



//    public Management getMEJB() {
//        return mejb;
//    }


    private Management retrieveMEJB(Context ic) {
        try {
            java.lang.Object objref = ic.lookup(MEJB_JNDI);
//            ManagementHome home =
//                (ManagementHome)PortableRemoteObject.narrow(objref,ManagementHome.class);
//            Management mejb = home.create();
            return mejb;
        } catch(NamingException ne) {
//            ErrorManager.getDefault().notify(ne);
//        } catch(RemoteException re) {
//             ErrorManager.getDefault().notify(re);
        } catch(Exception ce) {
//             ErrorManager.getDefault().notify(ce);
        }
        return null;
    }
    public MBeanServer getMBeanServer() {
        return this.mbeanServer;
    }


    public long getRoundTrips() {
        return statsProxy.getRoundTrips();
    }

    public long getFailures() {
        return statsProxy.getFailures();
    }

}
