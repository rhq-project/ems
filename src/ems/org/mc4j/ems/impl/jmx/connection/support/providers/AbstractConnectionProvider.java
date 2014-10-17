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
package org.mc4j.ems.impl.jmx.connection.support.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import javax.management.MBeanServer;

import org.mc4j.ems.connection.EmsConnectException;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.settings.ConnectionSettings;
import org.mc4j.ems.connection.support.ConnectionListener;
import org.mc4j.ems.connection.support.ConnectionProvider;
import org.mc4j.ems.impl.jmx.connection.DConnection;
import org.mc4j.ems.impl.jmx.connection.support.providers.proxy.StatsProxy;

/**
 * This Node is the abstract node representing a connection to a JMX Server.
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), January 2002
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public abstract class AbstractConnectionProvider implements ConnectionProvider {


    // Persistable settings
    protected ConnectionSettings connectionSettings;

    // Live information
    private boolean connected = false;

    protected boolean connectionFailure = false;

    protected DConnection existingConnection;

    protected List<ConnectionListener> connectionListeners = new ArrayList<ConnectionListener>();

    private Timer refreshTimer;

    private StatsProxy statsProxy;


    protected int connectionRoundTrips;

    public abstract MBeanServer getMBeanServer();

    public Object getMEJB() {
        return null;
    }

    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }


    public int compareTo(Object o) {
        ConnectionProvider otherProvider = (ConnectionProvider) o;
        return this.connectionSettings.getConnectionName().compareTo(otherProvider.getConnectionSettings().getConnectionName());
    }

    public void initialize(ConnectionSettings settings) {
        this.connectionSettings = settings;
    }


    public boolean isConnected() {
        return this.connected;
    }

    @Deprecated
    public void setConnected(boolean connected) throws Exception {
        if (connected) {
            connect();
        } else {
            disconnect();
        }
    }

    public final EmsConnection connect() {

        if (existingConnection != null) {
            // We were previously connected. Clear any cached data, since it could contain references to stale RMI stubs.
            existingConnection.unload();
        }
        try {
           doConnect();

           this.connected = true;
           this.connectionFailure = false;           
        } catch (Exception e) {
            throw new EmsConnectException("Could not connect [" + connectionSettings.getServerUrl() + "] " + e, e);
        }

        if (existingConnection == null) {
            DConnection connection = new DConnection("unknown", this);

            this.existingConnection = connection;
        } else {
            this.existingConnection.setConnectionProvider(this);    
        }
        for (ConnectionListener listener : connectionListeners) {
            listener.connect();
        }

        return this.existingConnection;
    }

   public EmsConnection getExistingConnection()
   {
      return existingConnection;
   }

   protected abstract void doConnect() throws Exception;


    public final void disconnect() {
        if (this.refreshTimer != null)
            this.refreshTimer.cancel();

        this.connected = false;
        this.connectionFailure = false;

        try {
            doDisconnect();
        } catch (Exception e) {
            throw new EmsConnectException("Could not close connection " +  e.toString(), e);
        }

        for (ConnectionListener listener : connectionListeners) {
            listener.disconnect();
        }
    }

    // TODO Should this be abstract?
    protected void doDisconnect() throws Exception {

    }


    public void addConnectionListener(ConnectionListener connectionListener) {
        this.connectionListeners.add(connectionListener);
    }

    public void removeConnectionListener(ConnectionListener connectionListener) {
        this.connectionListeners.remove(connectionListener);
    }


    public long getRoundTrips() {
        if (statsProxy != null)
            return statsProxy.getRoundTrips();
        else
            return 0;
    }

    public long getFailures() {
        if (statsProxy != null)
            return statsProxy.getFailures();
        else
            return 0;
    }

    public StatsProxy getStatsProxy() {
        return statsProxy;
    }

    public void setStatsProxy(StatsProxy statsProxy) {
        this.statsProxy = statsProxy;
    }
}
