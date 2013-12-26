/*
 * Copyright 2002-2007 Greg Hinkle
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
 * @version $Revision: 582 $($Author: ghinkl $ / $Date: 2007-04-11 00:12:17 +0200 (Mi, 11 Apr 2007) $)
 */
public class Mx4jConnectionTypeDescriptor extends AbstractConnectionTypeDescriptor  {

    public boolean isMEJBCompliant() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDisplayName() {
        return "MX4J 1.x";
    }

    public String getRecongnitionPath() {
        return null;
    }

    public String getDefaultServerUrl() {
        return "rmi://localhost:1099";
    }

    public String getDefaultJndiName() {
        return "jrmp";
    }

    public String getDefaultInitialContext() {
        return "com.sun.jndi.rmi.registry.RegistryContextFactory";
    }

    public String getConnectionMessage() {
        return "This connection type is for connections to MX4J 1.x. For versions 2.x of MX4J " +
            "use the JSR 160 connection type above.";
    }

    public String getDefaultPrincipal() {
        return null;
    }

    public String getDefaultCredentials() {
        return null;
    }

    public String getConnectionType() {
        return "MX4J";
    }

    public String[] getConnectionClasspathEntries() {
        return null;
    }

    public String getConnectionNodeClassName() {
        return "org.mc4j.console.connection.Mx4jConnectionNode";
    }

    public String getExtrasLibrary() {
        return "MX4J";
    }
}
