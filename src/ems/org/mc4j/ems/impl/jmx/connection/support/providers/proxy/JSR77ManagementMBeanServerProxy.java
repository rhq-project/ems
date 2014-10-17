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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.management.MBeanServer;
import javax.management.j2ee.Management;

/**
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), January 2002
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class JSR77ManagementMBeanServerProxy implements InvocationHandler, StatsProxy {

    private Management mejb;

    private long roundTrips;

    private long failures;

    private static final Class[] INTERFACES = { MBeanServer.class };


    /** Creates a new instance of Proxy */
    public JSR77ManagementMBeanServerProxy(Management mejb) {
        this.mejb = mejb;
    }

    public JSR77ManagementMBeanServerProxy(Object omejb) {
        this((Management) omejb);
    }

    public Object invoke(
        Object proxy, Method m, Object[] args)
    throws Throwable {

        Class serverClass = Management.class; // this.remoteServer.getClass();

        Method method = serverClass.getMethod(m.getName(),m.getParameterTypes());

        // TODO CSC: should all IOExceptions been catched here and thrown as an MC4JIOException (RuntimeException)
        // to avoid the occurence of an UndeclaredThrowableExcpetion???
        try {
            roundTrips++;
            return method.invoke(this.mejb, args);
        } catch(Exception e) {
            failures++;
            throw e;
        }
    }


    // TODO GH: Think about notification listers and how management ejbs have a seperate registry
    public MBeanServer buildServerProxy() {

        Object proxy =
            Proxy.newProxyInstance(
                JSR77ManagementMBeanServerProxy.class.getClassLoader(),
                JSR77ManagementMBeanServerProxy.INTERFACES,
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
