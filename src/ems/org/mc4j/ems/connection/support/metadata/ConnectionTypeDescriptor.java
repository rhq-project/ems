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

package org.mc4j.ems.connection.support.metadata;

import java.io.File;
import java.io.Serializable;
import java.util.Properties;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Sep 30, 2004
 * @version $Revision: 575 $($Author: ghinkl $ / $Date: 2006-05-22 04:38:53 +0200 (Mo, 22 Mai 2006) $)
 */
public interface ConnectionTypeDescriptor extends Serializable {

    /**
     * Typically used to provide an example template for the url necessary to connect
     * to this server type.
     * @return The default server url for connecting to this server type.
     */
    String getDefaultServerUrl();

    String getDefaultJndiName();

    String getDefaultInitialContext();

    String getDefaultPrincipal();

    String getDefaultCredentials();

    String getConnectionType();

    String getConnectionMessage();

    String[] getConnectionClasspathEntries();

    String getConnectionNodeClassName();

    boolean isMEJBCompliant();

    boolean isUseManagementHome();

    String getDisplayName();

    String getRecongnitionPath();

    String getServerVersion(File recognitionFile);

    String getExtrasLibrary();

    Properties getDefaultAdvancedProperties();

    /**
     * True if the ClassLoaderFactory should use the connection specific library
     * classes before using the system classes. This may be, for example, to utilize
     * the WebSphere or WebLogic JMX classes instead of the JDK 1.5 classes.
     * @return true if connection classes should be used first
     */
    boolean isUseChildFirstClassLoader();

}
