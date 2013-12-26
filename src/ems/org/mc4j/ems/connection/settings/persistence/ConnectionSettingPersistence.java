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

package org.mc4j.ems.connection.settings.persistence;

import java.beans.Encoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.mc4j.ems.connection.EmsException;
import org.mc4j.ems.connection.settings.ConnectionSettings;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), May 9, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class ConnectionSettingPersistence {

    private static final ConnectionSettingPersistence INSTANCE = new ConnectionSettingPersistence();

    public static ConnectionSettingPersistence getInstance() {
        return INSTANCE;
    }

    public String encodeSettings(ConnectionSettings settings) {
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(settings.getClass().getClassLoader());
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            XMLEncoder e = new XMLEncoder(new BufferedOutputStream(os));
            e.setExceptionListener(new ExceptionListener() {
                public void exceptionThrown(Exception e) {
                    throw new EmsException("Could not encode connection settings",e);
                }
            });
            e.setPersistenceDelegate(File.class,new PersistenceDelegate() {
                protected Expression instantiate(Object oldInstance, Encoder out) {
                    File f = (File) oldInstance;
                    return new Expression(oldInstance, oldInstance.getClass(),"new", new Object[]{ f.getAbsolutePath() });
                }

            });
            e.writeObject(settings);
            e.close();
            e.flush();
            return os.toString();
        } finally{
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }


    public ConnectionSettings decodeSettings(String xml) {
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ConnectionSettings.class.getClassLoader());
        try {

            XMLDecoder dec = new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
            ConnectionSettings newSettings = (ConnectionSettings) dec.readObject();
            return newSettings;
        } finally{
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }
}
