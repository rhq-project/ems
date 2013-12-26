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

package org.mc4j.ems.connection.support;

import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.settings.ConnectionSettings;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 4, 2005
 * @version $Revision: 581 $($Author: ghinkl $ / $Date: 2006-12-28 18:51:31 +0100 (Do, 28 Dez 2006) $)
 */
public interface ConnectionProvider {


//    MBeanServer getMBeanServer();

    ConnectionSettings getConnectionSettings();

    boolean isConnected();

    EmsConnection connect();

    void initialize(ConnectionSettings settings);

    void disconnect();

    void addConnectionListener(ConnectionListener connectionListener);

    void removeConnectionListener(ConnectionListener connectionListener);

    long getRoundTrips();

    long getFailures();

    EmsConnection getExistingConnection();
}
