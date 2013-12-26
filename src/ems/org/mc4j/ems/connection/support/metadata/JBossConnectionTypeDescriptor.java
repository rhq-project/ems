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

import java.util.Properties;

import javax.naming.Context;


/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Sep 30, 2004
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class JBossConnectionTypeDescriptor extends AbstractConnectionTypeDescriptor implements ConnectionTypeDescriptor {

    public boolean isMEJBCompliant() {
        return true;
    }

    public String getDisplayName() {
        return "JBoss";
    }

    public String getRecongnitionPath() {
        return "server/*/lib/jboss.jar";
    }

    public String getDefaultServerUrl() {
        return "jnp://localhost:1099";
    }

    public String getDefaultJndiName() {
        return "jmx/rmi/RMIAdaptor";
    }

    public String getDefaultInitialContext() {
        return "org.jnp.interfaces.NamingContextFactory";
    }

    public String getDefaultPrincipal() {
        return "";
    }

    public String getDefaultCredentials() {
        return "";
    }

    public String getConnectionType() {
        return "JBoss";
    }

    public String getConnectionMessage() {
        return null;
    }

    public Properties getDefaultAdvancedProperties() {
        Properties props = super.getDefaultAdvancedProperties();

        props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces" );
        props.put("jnp.disableDiscovery", "True");
        return props;
    }

   public boolean isUseChildFirstClassLoader() {
       return true;
   }


    public String[] getConnectionClasspathEntries() {
        return
        new String[] {
            // 3.x + jars
            "lib/jboss-jmx.jar",
            "client/jboss-common.jar",
            "lib/jboss-system.jar",
            "client/jbossall-client.jar",
            "client/log4j.jar",
            "*/*/lib/jboss.jar",
            "client/concurrent.jar",
            "client/jboss-jsr77-client.jar",
            // 3.2.3 jars
            "*/*/lib/jboss-transaction.jar",
            "lib/xercesImpl.jar",
            "lib/xml-apis.jar",
            // 4.0 jars
            "lib/dom4j.jar",
            "client/jnp-client.jar",
            "client/jmx-rmi-connector-client.jar",
            "client/jboss-j2ee.jar",
            "*/*/lib/jboss-management.jar",
            "*/*/lib/jbosssx.jar",
            "client/jbosssx-client.jar",
            "lib/endorsed/xercesImpl.jar",
            "lib/endorsed/xml-apis.jar",
            "*/*/lib/hibernate3.jar"
        };
    }

    public String getConnectionNodeClassName() {
        return "org.mc4j.ems.impl.jmx.connection.support.providers.JBossConnectionProvider";
    }
}
