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

public class InternalVMTypeDescriptor extends J2SE5ConnectionTypeDescriptor {

    public static final String DEFAULT_DOMAIN_SEARCH = "mc4j.ems.DefaultDomainSearch";
    
    public boolean isMEJBCompliant() {
        return false;
    }

    public String getDefaultServerUrl() {
        return "internal";
    }

    public String getDisplayName() {
        return "Internal";
    }

    public String getConnectionType() {
        return "Internal";
    }

    public String getConnectionNodeClassName() {
        return "org.mc4j.ems.impl.jmx.connection.support.providers.InternalVMProvider";
    }

}
