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
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;

import org.mc4j.ems.impl.jmx.connection.support.providers.proxy.JMXRemotingMBeanServerProxy;
import org.mc4j.ems.impl.jmx.connection.support.providers.proxy.StatsProxy;

public class PramatiConnectionProvider extends AbstractConnectionProvider {

    private JMXConnector jmxConnector;
    private MBeanServerConnection serverConnection;
    private MBeanServer mbeanServer;

    private static final String PROTOCOL_PROVIDER_PACKAGE = "jmx.remote.protocol.provider.pkgs";
    private static final String PRAMATI_PROTOCOL_PROVIDER_PACKAGE = "com.pramati.jmx.connector";


    protected void doConnect() throws Exception {
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        // Create an RMI connector client
        JMXServiceURL url = new JMXServiceURL(this.connectionSettings.getServerUrl());

        HashMap<String, Object> env = new HashMap<String, Object>();

        /*if ((connectionSettings.getInitialContextName() != null) &&
            (connectionSettings.getInitialContextName().trim().length() > 0)) {
            env.put(Context.INITIAL_CONTEXT_FACTORY, connectionSettings.getInitialContextName());
        } else {
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        }*/

        env.put(Context.SECURITY_PRINCIPAL, connectionSettings.getPrincipal());
        env.put(Context.SECURITY_CREDENTIALS, connectionSettings.getCredentials());

        // Set and custom, advanced properties
        if (connectionSettings.getAdvancedProperties() != null) {
            for (Map.Entry<Object, Object> entry : connectionSettings.getAdvancedProperties().entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                env.put(key, value);
            }
        }

        //<prashant> Change here </prashant>
        env.put(PROTOCOL_PROVIDER_PACKAGE, PRAMATI_PROTOCOL_PROVIDER_PACKAGE);//Is this an advanced Property?

        // Set the credential
        String[] credentials =
                new String[]{
                        this.connectionSettings.getPrincipal(),
                        this.connectionSettings.getCredentials()};

        env.put(JMXConnector.CREDENTIALS, credentials);

        this.jmxConnector = JMXConnectorFactory.connect(url, env);
        serverConnection = this.jmxConnector.getMBeanServerConnection();

        //serverConnection.queryNames(null,null);

        StatsProxy proxy = new JMXRemotingMBeanServerProxy(serverConnection);
        setStatsProxy(proxy);
        this.mbeanServer = proxy.buildServerProxy();

        super.connect();

    }

    public void doDisconnect() {
        try {
            this.jmxConnector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MBeanServer getMBeanServer() {
        return this.mbeanServer;
    }


}


