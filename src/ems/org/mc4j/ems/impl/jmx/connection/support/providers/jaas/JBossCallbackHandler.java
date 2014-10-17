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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * @author Ian Springer
 */
public class JBossCallbackHandler implements CallbackHandler {
    private String username;
    private char[] password;

    public JBossCallbackHandler(String username, String password)
    {
        this.username = username;
        this.password = password.toCharArray();
    }

    public void handle(Callback[] callbacks) throws
        IOException, UnsupportedCallbackException
    {
        for (int i = 0; i < callbacks.length; i++) {
            Callback callback = callbacks[i];
            //System.out.println("Handling Callback [" + callback + "]...");
            if (callback instanceof NameCallback) {

                NameCallback nameCallback = (NameCallback) callback;
                nameCallback.setName(this.username);
            } else if (callback instanceof PasswordCallback) {
                PasswordCallback passwordCallback = (PasswordCallback) callback;
                passwordCallback.setPassword(this.password);
            } else {
                throw new UnsupportedCallbackException(callback, "Unrecognized Callback: " + callback);
            }
        }
    }
}
