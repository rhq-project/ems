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

package org.mc4j.ems.connection.settings;


import java.io.File;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.mc4j.ems.connection.support.metadata.ConnectionTypeDescriptor;

/**
 * Options for connections
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), January 2002
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class ConnectionSettings implements Serializable, Comparable {

    private ConnectionTypeDescriptor connectionType;

    private String connectionName;
    private String jndiName;
    private String initialContextName;
    private String serverUrl;
    private String principal;
    private String credentials;

    private Properties advancedProperties = new Properties();

    private Properties controlProperties = new Properties();

    private boolean autoConnect;

    private boolean liveTree;

    private String libraryURI;

    /**
     * Holds value of property classPathEntries.
     */
    private List<File> classPathEntries;

    /**
     * Version -1371536502434280115L: MC4J 1.2bx
     * Version 2: 1.3
     */
    private static final long serialVersionUID = 3;


    private static final ObjectStreamField[] serialPersistentFields = {
            new java.io.ObjectStreamField("autoConnect", boolean.class),
            new java.io.ObjectStreamField("liveTree", boolean.class),
            new java.io.ObjectStreamField("classPathEntries", java.io.File[].class),
            new java.io.ObjectStreamField("connectionName", java.lang.String.class),
            new java.io.ObjectStreamField("connectionType", ConnectionTypeDescriptor.class),
            new java.io.ObjectStreamField("credentials", java.lang.String.class),
            new java.io.ObjectStreamField("initialContextName", java.lang.String.class),
            new java.io.ObjectStreamField("jndiName", java.lang.String.class),
            new java.io.ObjectStreamField("libraryURI", java.lang.String.class),
            new java.io.ObjectStreamField("principal", java.lang.String.class),
            new java.io.ObjectStreamField("serverUrl", java.lang.String.class),
            new java.io.ObjectStreamField("advancedProperties", Properties.class),
            new java.io.ObjectStreamField("controlProperties", Properties.class)};


    public boolean equals(Object object) {
        boolean result = false;
        if (object instanceof ConnectionSettings) {
            ConnectionSettings that = (ConnectionSettings) object;

            if ((that.getConnectionName().equals(this.getConnectionName())) &&
                    (that.getServerUrl().equals(this.getServerUrl()))) {
                result = true;
            }
        }
        return result;
    }

    public int hashCode() {
        int result = this.connectionName.hashCode();
        result = 31 * result + this.serverUrl.hashCode();
        return result;
    }

    /**
     * Getter for property connectionType.
     *
     * @return Value of property connectionType.
     */
    public ConnectionTypeDescriptor getConnectionType() {
        return connectionType;
    }

    public void initializeConnectionType(ConnectionTypeDescriptor connectionTypeDescriptor) {
        this.connectionType = connectionTypeDescriptor;
        this.advancedProperties = connectionType.getDefaultAdvancedProperties();
        this.initialContextName = connectionType.getDefaultInitialContext();
        this.serverUrl = connectionType.getDefaultServerUrl();
        this.jndiName = connectionType.getDefaultJndiName();
    }


    /**
     * Setter for property connectionType.
     *
     * @param connectionType New value of property connectionType.
     */
    public void setConnectionType(ConnectionTypeDescriptor connectionType) {
        this.connectionType = connectionType;
    }

    /**
     * Getter for property credentials.
     *
     * @return Value of property credentials.
     */
    public String getCredentials() {
        return credentials;
    }

    /**
     * Setter for property credentials.
     *
     * @param credentials New value of property credentials.
     */
    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    /**
     * Getter for property initialContextName.
     *
     * @return Value of property initialContextName.
     */
    public String getInitialContextName() {
        return initialContextName;
    }

    /**
     * Setter for property initialContextName.
     *
     * @param initialContextName New value of property initialContextName.
     */
    public void setInitialContextName(String initialContextName) {
        this.initialContextName = initialContextName;
    }

    /**
     * Getter for property jndiName.
     *
     * @return Value of property jndiName.
     */
    public String getJndiName() {
        return jndiName;
    }

    /**
     * Setter for property jndiName.
     *
     * @param jndiName New value of property jndiName.
     */
    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    /**
     * Getter for property name.
     *
     * @return Value of property name.
     */
    public String getConnectionName() {
        return this.connectionName;
    }

    /**
     * Setter for property name.
     *
     * @param connectionName New value of property name.
     */
    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    /**
     * Getter for property principal.
     *
     * @return Value of property principal.
     */
    public String getPrincipal() {
        return principal;
    }

    /**
     * Setter for property principal.
     *
     * @param principal New value of property principal.
     */
    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    /**
     * Getter for property serverUrl.
     *
     * @return Value of property serverUrl.
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Setter for property serverUrl.
     *
     * @param serverUrl New value of property serverUrl.
     */
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * Getter for property autoConnect.
     *
     * @return Value of property autoConnect.
     */
    public boolean isAutoConnect() {
        return autoConnect;
    }

    /**
     * Setter for property autoConnect.
     *
     * @param autoConnect New value of property autoConnect.
     */
    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    /**
     * Getter for property liveTree.
     *
     * @return Value of property liveTree.
     */
    public boolean isLiveTree() {
        return liveTree;
    }

    /**
     * Setter for property liveTree.
     *
     * @param liveTree New value of property liveTree.
     */
    public void setLiveTree(boolean liveTree) {
        this.liveTree = liveTree;
    }

    /**
     * The installation path (on disk) for the server to be
     * connected to. Setting this allows you to utilize the connection
     * factory to find the classpath libraries needed to make the connection.
     */
    public String getLibraryURI() {
        return this.libraryURI;
    }

    public void setLibraryURI(String libraryURI) {
        this.libraryURI = libraryURI;
    }


    /**
     * Getter for property classPathEntries.
     *
     * @return Value of property classPathEntries.
     */
    public List<File> getClassPathEntries() {
        return this.classPathEntries;
    }


    /**
     * Setter for property classPathEntries.
     *
     * @param classPathEntries New value of property classPathEntries.
     */
    public void setClassPathEntries(List<File> classPathEntries) {
        this.classPathEntries = classPathEntries;
    }

    public Properties getAdvancedProperties() {
        return advancedProperties;
    }

    public void setAdvancedProperties(Properties advancedProperties) {
        this.advancedProperties = advancedProperties;
    }

    public int compareTo(Object o) {
        return getConnectionName().compareTo(((ConnectionSettings)o).getConnectionName());
    }


    public Properties getControlProperties() {
        return controlProperties;
    }

    public void setControlProperties(Properties controlProperties) {
        this.controlProperties = controlProperties;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("EmsConnection [");
        buf.append(connectionType.getConnectionType());
        buf.append("]\n\tInitial Context = ");
        buf.append(initialContextName);
        buf.append("\n\tJNDI Name = ");
        buf.append(jndiName);
        buf.append("\n\tServer URL = ");
        buf.append(serverUrl);
        buf.append("\n\tPrinciple = ");
        buf.append(principal);
        buf.append("\n\tCredentials = ");
        buf.append(credentials);

        if (advancedProperties != null) {
            for (Map.Entry entry : advancedProperties.entrySet()) {
                buf.append("\n\t Advanced Property [");
                buf.append(entry.getKey());
                buf.append("] = ");
                buf.append(entry.getValue());
            }
        }
        if (advancedProperties != null) {
            for (Map.Entry entry : controlProperties.entrySet()) {
                buf.append("\n\t Control Property [");
                buf.append(entry.getKey());
                buf.append("] = ");
                buf.append(entry.getValue());
            }
        }

        return buf.toString();
    }
}
