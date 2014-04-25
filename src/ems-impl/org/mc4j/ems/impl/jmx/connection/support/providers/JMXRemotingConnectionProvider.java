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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnector;
import javax.management.remote.rmi.RMIServer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import org.mc4j.ems.connection.EmsConnectException;
import org.mc4j.ems.connection.EmsException;
import org.mc4j.ems.impl.jmx.connection.support.providers.proxy.JMXRemotingMBeanServerProxy;
import org.mc4j.ems.impl.jmx.connection.support.providers.proxy.JSR77ManagementMBeanServerProxy;
import org.mc4j.ems.impl.jmx.connection.support.providers.proxy.StatsProxy;

/**
 * Represents a Connection to a JSR 160 compliant RMI server
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), December 2003
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class JMXRemotingConnectionProvider extends AbstractConnectionProvider {

    private JMXConnector jmxConnector;
    private MBeanServerConnection serverConnection;
    private MBeanServer mbeanServer;

    private Object mejb;


    private static final String MEJB_JNDI = "ejb/mgmt/MEJB";


    protected void doConnect() {
        try {


            ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());


            // Create an RMI connector client
            JMXServiceURL url = new JMXServiceURL(this.connectionSettings.getServerUrl());

            Hashtable env = new Hashtable();

            if ((connectionSettings.getInitialContextName() != null) &&
                (connectionSettings.getInitialContextName().trim().length() > 0)) {
                env.put(Context.INITIAL_CONTEXT_FACTORY, connectionSettings.getInitialContextName());
            } else {
                env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
            }
            if (connectionSettings.getPrincipal() != null)
                env.put(Context.SECURITY_PRINCIPAL, connectionSettings.getPrincipal());
            if (connectionSettings.getCredentials() != null)
                env.put(Context.SECURITY_CREDENTIALS, connectionSettings.getCredentials());


            if (connectionSettings.getConnectionType().isUseManagementHome()) {
                InitialContext ctx = new InitialContext(env);
                this.mejb = retrieveMEJB(ctx);
            }


            /* Test crap
            ObjectName test = new ObjectName("*:Name=examplesServer,Type=ServerRuntime,*");
            Set testResults = this.mejb.queryNames(test, null);
            for (Iterator iterator = testResults.iterator(); iterator.hasNext();) {
                ObjectName testName = (ObjectName) iterator.next();
                System.out.println("MBean: " + testName.getCanonicalName());
                MBeanInfo testInfo = this.mejb.getMBeanInfo(testName);
                MBeanAttributeInfo[] atts = testInfo.getAttributes();
                for (int i = 0; i < atts.length; i++) {
                    MBeanAttributeInfo att = atts[i];
                    System.out.println("\tAttribute: " + att.getName() + ":" + att.getType());
                }
            }
            */


            //env.put("jmx.remote.protocol.provider.pkgs","com.pramati.jmx.connector");


            // Set and custom, advanced properties
            if (connectionSettings.getAdvancedProperties() != null) {
                Set<Map.Entry<Object,Object>> entries = connectionSettings.getAdvancedProperties().entrySet();
                for (Map.Entry entry : entries) {
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();

                    env.put(key, value);
                }
            }

            // Set the credential
            String[] credentials =
                new String[]{
                    this.connectionSettings.getPrincipal(),
                    this.connectionSettings.getCredentials()};

            env.put(JMXConnector.CREDENTIALS, credentials);


            //testSsl();

            if (ssl) {
                this.jmxConnector = new RMIConnector(sslStub, null);
            } else {
                this.jmxConnector = JMXConnectorFactory.connect(url, env);
                serverConnection = this.jmxConnector.getMBeanServerConnection();
            }
            //serverConnection.queryNames(null,null);



            StatsProxy proxy = null;
            if (connectionSettings.getConnectionType().isUseManagementHome()) {
                proxy = new JSR77ManagementMBeanServerProxy(this.mejb);
            } else {
                proxy = new JMXRemotingMBeanServerProxy(serverConnection);
            }
            setStatsProxy(proxy);
            this.mbeanServer = proxy.buildServerProxy();

        } catch (MalformedURLException e) {
            throw new EmsConnectException("Malformed url", e);
        } catch (IOException e) {
            throw new EmsConnectException("IOException: Check service availability",e);
        } catch (NamingException e) {
            throw new EmsConnectException("Naming failure",e);
        }

    }

    public boolean isConnected() {
        try {
            // We query for something that is unlikely to exist, as we just want to test
            // the connection and not load a huge number of mbean names.
            this.mbeanServer.queryNames(new ObjectName("foo:does=notexist"),null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    RMIServer sslStub;
    boolean ssl = false;

    private boolean testSsl() {
        JMXServiceURL url = null;
        String host = null;
        int port = 0;
        String start = "/jndi/rmi://";
        String end = "/jmxrmi";
        try {
            url = new JMXServiceURL(this.connectionSettings.getServerUrl());
            String path = url.getURLPath();
            if (path.startsWith(start)) {
                String hostAndPort = path.substring(start.length(), path.length() - end.length());
                String[] hp = hostAndPort.split(":");
                host = hp[0];
                port = Integer.parseInt(hp[1]);
            }

            // Try SSL
            Registry registry = LocateRegistry.getRegistry(host,port, new SslRMIClientSocketFactory());
            sslStub = (RMIServer) registry.lookup("jmxrmi");
            ssl = true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            // Try regular
            try {
                Registry registry = LocateRegistry.getRegistry(url.getHost(),url.getPort());
                sslStub = (RMIServer) registry.lookup("jmxrmi");
            } catch (RemoteException e1) {
                e1.printStackTrace();
            } catch (NotBoundException e1) {
                e1.printStackTrace();
            }

        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return ssl;
    }

    public void doDisconnect() throws IOException {
        this.jmxConnector.close();
    }

    private Object retrieveMEJB(Context ic) {
        try {
            java.lang.Object objref = ic.lookup(MEJB_JNDI);

//            ManagementHome home =
//                (ManagementHome) PortableRemoteObject.narrow(objref, ManagementHome.class);
//            Management mejb = home.create();
//            return mejb;

            Class managementHomeClass = Class.forName("javax.management.j2ee.ManagementHome.class");

            Object managementHome = PortableRemoteObject.narrow(objref, managementHomeClass);
            Method m = managementHomeClass.getMethod("create",new Class[] {});
            Object managementEjb = m.invoke(managementHome);
            return managementEjb;
        } catch (NamingException ne) {
            throw new EmsException("",ne);
        }catch (Exception ce) {
            throw new EmsException("",ce);
        }
    }

    public MBeanServer getMBeanServer() {
        return this.mbeanServer;
    }
}
