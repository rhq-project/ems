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
package org.mc4j.ems.impl.jmx.connection.support.providers.proxy;

import java.io.NotSerializableException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.rmi.NoSuchObjectException;

import javax.management.MBeanServer;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mc4j.ems.connection.EmsConnectException;
import org.mc4j.ems.connection.EmsUnsupportedTypeException;
import org.mc4j.ems.connection.LoadException;
import org.mc4j.ems.connection.support.ConnectionProvider;
import org.mc4j.ems.impl.jmx.connection.support.providers.JBossConnectionProvider;

/**
 * A proxy for a remote MBeanServer stub, which does the following for each invocation:
 * <ul>
 * <li>if the invocation fails, attempts to reestablish the underlying RMI connection and then retries the invocation</li>
 * <li>if the underlying connection's type is JBoss, do a JAAS login before the invocation and a JAAS logout after it</li>
 * </ul>
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), January 2002
 * @author Ian Springer
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class GenericMBeanServerProxy implements InvocationHandler, StatsProxy {
    private static Log log = LogFactory.getLog(GenericMBeanServerProxy.class);

    private Object remoteServer;
    private ConnectionProvider provider;

    private long roundTrips;

    private long failures;

    private boolean reconnecting = false;

    /** Creates a new instance of Proxy */
    public GenericMBeanServerProxy(Object remoteServer) {
        this.remoteServer = remoteServer;
    }

    public GenericMBeanServerProxy() {
    }

    public ConnectionProvider getProvider() {
        return provider;
    }

    public void setProvider(ConnectionProvider provider) {
        this.provider = provider;
    }

    public Object getRemoteServer() {
        return remoteServer;
    }

    public void setRemoteServer(Object remoteServer) {
        this.remoteServer = remoteServer;
    }

    public Object invoke(
        Object proxy, Method m, Object[] args)
    throws Throwable {

//        SwingUtility.eventThreadAlert();
//
//        ConnectionInfoAction.addHit();

        Class serverClass = this.remoteServer.getClass();
        //org.openide.windows.IOProvider.getDefault().getStdOut().println("Looking at object: " + serverClass.getName());

        Method method = findMethod(m, serverClass);

//        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
        try {
            roundTrips++;
//            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            @SuppressWarnings({"UnnecessaryLocalVariable"})
            Object returnValue = invokeInternal(args, method);
            return returnValue;
        } catch(InvocationTargetException e) {
            failures++;
            if (e.getCause() != null) {
                Throwable t = e.getCause();
                if (t instanceof java.rmi.ConnectException) {
                    throw new EmsConnectException(t);
                } else if (t instanceof NoSuchObjectException) {
                    // This happens when the server comes back up and the stub is stale.
                    // Try to reconnect if this provider supports it (if it told the proxy what the provider was)
                    if (provider != null && !reconnecting) {
                        try {
                            log.info("Reestablishing RMI stub to restarted server ["
                                + this.provider.getConnectionSettings().getServerUrl() + "]...");
                            reconnecting = true;
                            provider.connect();
                            // Retry the invocation.
                            @SuppressWarnings({"UnnecessaryLocalVariable"})
                            Object returnValue = invokeInternal(args, method);
                            return returnValue;
                        } catch(Exception f) {
                            log.warn("Unable to reestablish RMI stub to restarted server ["
                                + this.provider.getConnectionSettings().getServerUrl() + "].", f);
                        } finally {
                            reconnecting = false;
                        }
                    }
                    // The reconnect failed, throw the original exception
                    // If the retry fails, it will throw its own exception
                    throw new EmsConnectException(t);
                } else if (t instanceof java.io.IOException) {
                    throw new EmsConnectException(t);
                } else if (t instanceof NotSerializableException) {
                    throw new EmsUnsupportedTypeException("Value was not serializable " + t.getLocalizedMessage(),t);
                } else {
                    throw new EmsConnectException("Connection failure " + t.getLocalizedMessage(), t);
                }
            } else {
                throw e;
            }
        } catch (Exception e) {
            failures++;
            // Log the method.toString() to aid debugging.
            log.error("Failed to invoke method [" + method + "]: " + e);
            //e.printStackTrace();
            throw e;
        } finally {
//            Thread.currentThread().setContextClassLoader(ctxLoader);
        }
    }

    protected Object invokeInternal(Object[] args, Method method)
        throws LoginException, IllegalAccessException, InvocationTargetException {
        boolean isJBossConnection = (this.provider instanceof JBossConnectionProvider);
        if (isJBossConnection) {
            JBossConnectionProvider jbossProvider = (JBossConnectionProvider) this.provider;
            // See https://jira.jboss.org/jira/browse/JOPR-9 for an explanation of why we have to re-set the
            // PrincipalInfo prior to every JBoss JMX call.
            //jbossProvider.resetPrincipalInfo();
            // Login via JAAS before making the call...
            jbossProvider.login();
        }
        Object returnValue;
        try {
            returnValue = method.invoke(this.remoteServer, args);
        } finally {
            if (isJBossConnection) {
                JBossConnectionProvider jbossProvider = (JBossConnectionProvider) this.provider;
                // Logout via JAAS before returning...
                jbossProvider.logout();
            }
        }
        return returnValue;
    }

    public long getRoundTrips() {
        return roundTrips;
    }

    public long getFailures() {
        return failures;
    }

    public MBeanServer buildServerProxy() {
        try {
            Object proxy = Proxy.newProxyInstance(
                GenericMBeanServerProxy.class.getClassLoader(),
                    new Class<?>[] { Class.forName("javax.management.MBeanServer")},
                this);
            return (MBeanServer) proxy;

        } catch (ClassNotFoundException e) {
            throw new LoadException("Unable to find JMX Classes", e);
        }
    }

    private static Method findMethod(Method m, Class serverClass) throws NoSuchMethodException {
        Method method;
        if ("queryMBeans".equals(m.getName())) {
            // TODO GH: This is horribly inefficient
            Method[] ms = serverClass.getMethods();
            method = null;
            for (int i = 0; i < ms.length; i++) {
                //org.openide.windows.IOProvider.getDefault().getStdOut().println("\t" + ms[i].getName() +
                //    Arrays.asList(ms[i].getParameterTypes()));
                if (ms[i].getName().equals("queryMBeans")) {
                    method = ms[i];
                }
                if (method == null) {
                    throw new EmsConnectException("Unsupported operation [" + m.getName() + "]");
                }
            }
            //method = serverClass.getMethod(m.getName(), new Class[] { ObjectName.class, QueryExp.class });
        } else {
            method = serverClass.getMethod(m.getName(), m.getParameterTypes());
        }

        // Do our best to return an accessible method to prevent an IllegalAccessException when the method gets invoked.
        return getAccessibleMethod(method);
    }

    private static Method getAccessibleMethod(Method method) {
        try {
           method.setAccessible(true);
           return method;
        } catch (SecurityException e) {
            // If the class declaring the method is itself not public, the method will not be accessible, so attempt to
            // find a method with the same signature on one of the server class's interfaces.
            if (!Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
                Method interfaceMethod = getInterfaceMethod(method);
                if (interfaceMethod != null) {
                    return interfaceMethod;
                }
            }
            return method;
        }
    }

    private static Method getInterfaceMethod(Method method) {
        Class<?>[] interfaceClasses = method.getClass().getInterfaces();
        for (int i = 0, interfaceClassesLength = interfaceClasses.length; i < interfaceClassesLength; i++) {
            Class interfaceClass = interfaceClasses[i];
            try {
                return interfaceClass.getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                // ignore
            } catch (SecurityException e) {
                // ignore
            }
        }
        // Return null to indicate we were unable to find an interface method.
        return null;
    }
}
