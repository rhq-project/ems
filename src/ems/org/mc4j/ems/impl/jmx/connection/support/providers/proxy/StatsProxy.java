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
package org.mc4j.ems.impl.jmx.connection.support.providers.proxy;

import javax.management.MBeanServer;

/**
 * An interface for proxies to declare their capability to track and provide statistics
 * on the network traffic.
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Nov 22, 2005
 * @version $Revision: 570 $($Author: ghinkl $ / $Date: 2006-04-12 21:14:16 +0200 (Mi, 12 Apr 2006) $)
 */
public interface StatsProxy {


    /**
     * @return a newly built dynamic proxy that will manage the connection
     */
    MBeanServer buildServerProxy();


    /**
     * @return the count of server calls made by this proxied connection
     */
    long getRoundTrips();

    /**
     * @return the count of server calls that threw an exception
     */
    long getFailures();
}
