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
package org.mc4j.ems.impl.jmx.connection.support.providers.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;

/**
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), January 2002
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class JMXRemotingMBeanServerProxy implements InvocationHandler, StatsProxy {

    private MBeanServerConnection remoteServer;

    private long roundTrips;

    private long failures;

    private static final Class[] INTERFACES = { MBeanServer.class };


    /** Creates a new instance of Proxy */
    public JMXRemotingMBeanServerProxy(MBeanServerConnection remoteServer) {
        this.remoteServer = remoteServer;
    }


    public Object invoke(
        Object proxy, Method m, Object[] args)
    throws Throwable {

        Class serverClass = MBeanServerConnection.class; // this.remoteServer.getClass();

        Method method = serverClass.getMethod(m.getName(),m.getParameterTypes());

        // TODO GH: Throw as Runtime?
        try {
            roundTrips++;
            return method.invoke(this.remoteServer, args);
        } catch(InvocationTargetException ite) {
            failures++;
            Throwable t = ite.getTargetException();
            if (t != null)
                throw t;
            else
                throw ite;
        }
    }

    public MBeanServer buildServerProxy() {

        Object proxy =
            Proxy.newProxyInstance(
                JMXRemotingMBeanServerProxy.class.getClassLoader(),
                JMXRemotingMBeanServerProxy.INTERFACES,
                this);

        return (MBeanServer) proxy;
    }

    public long getRoundTrips() {
        return roundTrips;
    }

    public long getFailures() {
        return failures;
    }
}
