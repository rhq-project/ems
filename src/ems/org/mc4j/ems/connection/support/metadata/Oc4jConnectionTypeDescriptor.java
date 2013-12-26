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



/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Sep 30, 2004
 * @version $Revision: 570 $($Author: ghinkl $ / $Date: 2006-04-12 21:14:16 +0200 (Mi, 12 Apr 2006) $)
 */
public class Oc4jConnectionTypeDescriptor extends AbstractConnectionTypeDescriptor  {
    public boolean isMEJBCompliant() {
        return true;
    }

    public String getDisplayName() {
        return "OC4J";
    }

    public String getRecongnitionPath() {
        return "j2ee/home/oc4j.jar";
    }

    public String getDefaultServerUrl() {
        return "ormi://localhost:23791/default";
    }

    public String getDefaultJndiName() {
        return "java:comp/env/ejb/mgmt/MEJB";
    }

    public String getDefaultInitialContext() {
        return "com.evermind.server.ApplicationClientInitialContextFactory";
    }

    public String getDefaultPrincipal() {
        return "admin";
    }

    public String getDefaultCredentials() {
        return "";
    }

    public String getConnectionType() {
        return "OC4J";
    }

    public String getConnectionMessage() {
        return null;
    }

    public String[] getConnectionClasspathEntries() {
        return
            new String[] {
                "adminclient.jar",
                "ejb.jar",
                "oc4j-internal.jar",
                "admin_client.jar"
            };
    }

    public String getConnectionNodeClassName() {
        return "org.mc4j.console.connection.Oc4jConnectionProvider";
    }
}
