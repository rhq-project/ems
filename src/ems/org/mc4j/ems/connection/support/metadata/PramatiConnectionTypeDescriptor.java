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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Sep 30, 2004
 * @version $Revision: 570 $($Author: ghinkl $ / $Date: 2006-04-12 21:14:16 +0200 (Mi, 12 Apr 2006) $)
 */
public class PramatiConnectionTypeDescriptor extends AbstractConnectionTypeDescriptor  {
    public boolean isMEJBCompliant() {
        return false;
    }

    public String getDisplayName() {
        return "Pramati 3.5+";
    }

    public String getRecongnitionPath() {
        return "server/lib/pramati/version.jar";
    }


     public String getServerVersion(File recognitionFile) {
        try {
            String version = null;

            URLClassLoader ld =
                new URLClassLoader(new URL[] { recognitionFile.toURL() });

            InputStream is = ld.getResourceAsStream("Version.props");
            if (is != null) {
                Properties props = new Properties();
                props.load(is);

                version = props.getProperty("PRODUCT_VERSION");
            }

            return version;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDefaultServerUrl() {
        return "service:jmx:rmi:///jndi/rmi://localhost:9191/rmi-client-connector";
    }

    public String getDefaultPrincipal() {
        return "";
    }

    public String getDefaultCredentials() {
        return "";
    }

    public String getDefaultInitialContext() {
        return "com.sun.jndi.rmi.registry.RegistryContextFactory";
    }

    public String getDefaultJndiName() {
        return null;
    }

    public String getConnectionType() {
        return "Pramati";
    }

    public String getConnectionMessage() {
        return null;
    }

    public String[] getConnectionClasspathEntries() {
        return
            new String[] {
                "pramati_client_all.jar"
            };
    }

    public String getConnectionNodeClassName() {
        return "org.mc4j.console.connection.PramatiConnectionProvider";
    }
}

