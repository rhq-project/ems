/*
 * Copyright 2002-2009 Greg Hinkle
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

import java.util.Properties;

import javax.management.MBeanServer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mc4j.ems.connection.ConnectionException;
import org.mc4j.ems.impl.jmx.connection.support.providers.jaas.JBossCallbackHandler;
import org.mc4j.ems.impl.jmx.connection.support.providers.jaas.JBossConfiguration;
import org.mc4j.ems.impl.jmx.connection.support.providers.proxy.GenericMBeanServerProxy;

/**
 * Represents a Connection to a JBoss JMX Server. This connection
 * works against the JBoss RMI connector. If a principal and
 * credentials are specified in the connection settings, JAAS is
 * used to authenticate prior to each call.
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), January 2002
 * @author Ian Springer
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class JBossConnectionProvider extends AbstractConnectionProvider {
    private static final String NAMING_CONTEXT_FACTORY_CLASS_NAME = "org.jnp.interfaces.NamingContextFactory";
//    private static final String MEJB_JNDI = "ejb/mgmt/MEJB";

    private MBeanServer mbeanServer;
    private GenericMBeanServerProxy proxy;
    private LoginContext loginContext;
//    private Management mejb;

    private static Log log = LogFactory.getLog(JBossConnectionProvider.class);

    protected void doConnect() throws Exception {
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();

        try {
            System.setProperty("jmx.serial.form", "1.1");

            // TODO: Used to need this, but it appears to work without now (verify)
            // Change the context classloader as the JBoss version of the
            // MBeanServerFactory uses it to find their class

            ClassLoader childLoader = this.getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(childLoader);

            InitialContext context = getInitialContext();

            if (getConnectionSettings().getPrincipal() != null) {
                initJaasLoginContext();
            }

            Object rmiAdaptor = context.lookup(this.connectionSettings.getJndiName());

            // GH: Works around a real strange "LinkageError: Duplicate class found"
            // by loading these classes in the main connection classloader
            //Class foo = RMINotificationListener.class;
            //foo = RMINotificationListenerMBean.class;

            // TODO GH!: I think this fixes notifications, but breaks compatibility with at least 3.0.8
            //RMIConnectorImpl connector = new RMIConnectorImpl(rmiAdaptor);

            if (this.proxy != null) {
                // This is a reconnect
                log.debug("Reconnecting to remote JBoss MBeanServer...");
                this.proxy.setRemoteServer(rmiAdaptor);
            } else {
                this.proxy = new GenericMBeanServerProxy(rmiAdaptor);
                this.proxy.setProvider(this);
                setStatsProxy(proxy);
                this.mbeanServer = proxy.buildServerProxy();
            }
            //this.mgmt = retrieveMEJB();
        } finally {
            // Set the context classloader back to what it was.
            Thread.currentThread().setContextClassLoader(currentLoader);
        }
    }

    private InitialContext getInitialContext() throws NamingException {
        Properties props = this.connectionSettings.getAdvancedProperties();
        if (!NAMING_CONTEXT_FACTORY_CLASS_NAME.equals(this.connectionSettings.getInitialContextName())) {
            log.warn("Unsupported initial context factory [" + this.connectionSettings.getInitialContextName()
                + "] - only " + NAMING_CONTEXT_FACTORY_CLASS_NAME
                + " is supported for JBoss connections; using that instead...");
        }
        props.put(Context.INITIAL_CONTEXT_FACTORY, NAMING_CONTEXT_FACTORY_CLASS_NAME);
        props.put(Context.PROVIDER_URL, this.connectionSettings.getServerUrl());

        try {
            InitialContext context = new InitialContext(props);
            return context;
        } catch(NoInitialContextException e) {
            // Try to be more helpful, indicating the reason we couldn't make the connection in this
            // common case of missing libraries.
            if (e.getCause() instanceof ClassNotFoundException) {
                throw new ConnectionException("Necessary classes not found for remote connection, check installation path configuration.",
                    e.getCause());
            }
            throw e;
        }
    }


    /* GH: an aborted attempt at manually changing the polling type
    public class RMIAdaptorExtension extends RMIConnectorImpl {
        public RMIAdaptorExtension(RMIAdaptor rmiAdaptor) {
            super(rmiAdaptor);

            try {
                Field field = RMIConnectorImpl.class.getField("mEventType");
                if (!Modifier.isPrivate(field.getModifiers())) {
                    field.set(this, new Integer(RMIConnectorImpl.NOTIFICATION_TYPE_POLLING));
                }
            } catch (NoSuchFieldException nsfe) {
            } catch (IllegalAccessException iae) {
            }
        }
    }
    */


    public void doDisconnect() {
    	this.loginContext = null;
    }

    /*   public Object getMEJB() {
            if (mejb == null) {
                mejb = retrieveMEJB();
            }
            return mejb;
        }


        private Management retrieveMEJB() {
            try {
                Context ic = getInitialContext();
                java.lang.Object objref = ic.lookup(MEJB_JNDI);
                ManagementHome home =
                    (ManagementHome)PortableRemoteObject.narrow(objref,ManagementHome.class);
                Management mejb = home.create();
                return mejb;
            } catch(NamingException ne) {
                ErrorManager.getDefault().notify(ne);
            } catch(RemoteException re) {
                 ErrorManager.getDefault().notify(re);
            } catch(Exception ce) {
                 ErrorManager.getDefault().notify(ce);
            }
            return null;
        }
    */
    public MBeanServer getMBeanServer() {
        return this.mbeanServer;
    }

    public void login() throws LoginException
    {
        if (this.loginContext != null) {
            this.loginContext.login();
        }
    }

    public void logout() throws LoginException
    {
        if (this.loginContext != null) {
            this.loginContext.logout();
        }
    }

    private void initJaasLoginContext() throws LoginException {
        Configuration jaasConfig = new JBossConfiguration();
        Configuration.setConfiguration(jaasConfig);
        JBossCallbackHandler jaasCallbackHandler = new JBossCallbackHandler(
            this.connectionSettings.getPrincipal(), this.connectionSettings.getCredentials());
        this.loginContext = new LoginContext(JBossConfiguration.JBOSS_ENTRY_NAME, jaasCallbackHandler);
    }    
}
