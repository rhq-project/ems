/*
 * Copyright 2002-2009 Greg Hinkle
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
package org.mc4j.ems.impl.jmx.connection.support.providers.jaas;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

/**
 * A JAAS configuration for a JBoss client. This is the programmatic equivalent of the following auth.conf file:
 *
 * <code>
 * jboss
 * {
 *   org.jboss.security.ClientLoginModule required
 *     multi-threaded=true;
 * };
 * </code>
 *
 * @author Ian Springer
 */
public class JBossConfiguration extends Configuration {
    public static final String JBOSS_ENTRY_NAME = "jboss";

    private static final String JBOSS_LOGIN_MODULE_CLASS_NAME = "org.jboss.security.ClientLoginModule";
    private static final String MULTI_THREADED_OPTION = "multi-threaded";

    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        if (JBOSS_ENTRY_NAME.equals(name)) {
            Map options = new HashMap(1);
            options.put(MULTI_THREADED_OPTION, Boolean.TRUE.toString());
            AppConfigurationEntry appConfigurationEntry =
                new AppConfigurationEntry(JBOSS_LOGIN_MODULE_CLASS_NAME,
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
            return new AppConfigurationEntry[] {appConfigurationEntry};
        } else {
            throw new IllegalArgumentException("Unknown entry name: " + name);
        }
    }

    public void refresh() {
        return;
    }
}
