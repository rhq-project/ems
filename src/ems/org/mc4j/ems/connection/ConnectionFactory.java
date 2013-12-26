/*
 * Copyright 2002-2010 Greg Hinkle
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
package org.mc4j.ems.connection;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mc4j.ems.connection.settings.ConnectionSettings;
import org.mc4j.ems.connection.support.ConnectionProvider;
import org.mc4j.ems.connection.support.classloader.ClassLoaderFactory;
import org.mc4j.ems.connection.support.metadata.ConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.GeronimoConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.InternalVMTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.J2SE5ConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.JBossConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.JDMKConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.JSR160ConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.LocalVMTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.Mx4jConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.Oc4jConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.PramatiConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.SJSASConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.Tomcat55ConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.Weblogic9ConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.Weblogic9Jsr77ConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.WeblogicConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.WebsphereConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.WebsphereStudioConnectionTypeDescriptor;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 5, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class ConnectionFactory {

    private static final String BROAD_SEARCH_PROPERTY = "mc4j.ems.fileSearchBroad";
    private static final int DEFAULT_SEARCH_DEPTH = 6;
    private static final String SEARCH_DEPTH_PROPERTY = "mc4j.ems.fileSearchDepth";

    public static final String COPY_JARS_TO_TEMP = "mc4j.ems.CopyJarsToTemp";

    public static final String JAR_TEMP_DIR = "mc4j.ems.JarTempDir";

    public static final String USE_CONTEXT_CLASSLOADER="mc4j.ems.UseContextClassLoader";

    private boolean broadSearch = false;
    private int searchDepth = DEFAULT_SEARCH_DEPTH;


    private static Log log = LogFactory.getLog(ConnectionFactory.class);

    public static final ConnectionTypeDescriptor[] CONNECTION_DESCRIPTORS =
            new ConnectionTypeDescriptor[]{
                    new InternalVMTypeDescriptor(),
                    new LocalVMTypeDescriptor(),
                    new JBossConnectionTypeDescriptor(),
                    new Tomcat55ConnectionTypeDescriptor(),
                    new JDMKConnectionTypeDescriptor(),
                    new J2SE5ConnectionTypeDescriptor(),
                    new JSR160ConnectionTypeDescriptor(),
                    new GeronimoConnectionTypeDescriptor(),
                    new Mx4jConnectionTypeDescriptor(),
                    new Oc4jConnectionTypeDescriptor(),
                    new PramatiConnectionTypeDescriptor(),
                    new SJSASConnectionTypeDescriptor(),
                    new WeblogicConnectionTypeDescriptor(),
                    new Weblogic9ConnectionTypeDescriptor(),
                    new Weblogic9Jsr77ConnectionTypeDescriptor(),
                    new WebsphereConnectionTypeDescriptor(),
                    new WebsphereStudioConnectionTypeDescriptor()};

    // TODO GH: Move to a SPI model allowing new types to be added later?
    // TODO GH: Perhaps go to a more externalized descriptor based model?
    public static List<ConnectionTypeDescriptor> getConnectionTypes() {
        return Arrays.asList(CONNECTION_DESCRIPTORS);
    }

    public ConnectionFactory() {
        if (System.getProperty(BROAD_SEARCH_PROPERTY) != null) {
            broadSearch = Boolean.valueOf(System.getProperty(BROAD_SEARCH_PROPERTY));
        }
        if (System.getProperty(SEARCH_DEPTH_PROPERTY) != null) {
            searchDepth = Integer.parseInt(System.getProperty(SEARCH_DEPTH_PROPERTY));
        }
    }

   /**
    * Build a connection provider given the settings. This should be the prefrence over connect.
    * Each request to connect will reuse the same classloader and provider rather than rebuilding
    * from scratch.
    * @param connectionSettings the connection settings for the connection
    * @return a ConnectionProvider that you can get live connection from.
    */
   public ConnectionProvider getConnectionProvider(final ConnectionSettings connectionSettings)
   {
      String className = connectionSettings.getConnectionType().getConnectionNodeClassName();

        try {
            // TODO GH: Does this need to be configurable per connection?

            ClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return ClassLoaderFactory.getInstance().buildClassLoader(connectionSettings);
                }
            });

            log.debug("Loading connection class [" + className + "] from ClassLoader [" + loader + "]...");

            // TODO GH: Add intelligent classloader layer here that can either work
            // directly against current classloader or build a non-delegating child
            // to override with connection specific classes
            Class clazz = Class.forName(className, false, loader);

            ConnectionProvider connectionProvider = (ConnectionProvider) clazz.newInstance();

            connectionProvider.initialize(connectionSettings);
            return connectionProvider;

        } catch (IllegalAccessException e) {
            throw new ConnectionException("Could not access ConnectionClass " + className, e);
        } catch (InstantiationException e) {
            throw new ConnectionException("Could not instantiate ConnectionClass " + className, e);
        } catch (ClassNotFoundException e) {
            throw new ConnectionException("Could not find ConnectionClass " + className, e);
        }

   }

   /**
    * @deprecated Use getConnectionProvider.connect... hold on to the connection provider to reopen the connection
    * @param connectionSettings the settings to build the connection on
    * @return a live connection
    */
    public EmsConnection connect(ConnectionSettings connectionSettings) {

        log.info("Connecting to " + connectionSettings + "...");
        
        return getConnectionProvider(connectionSettings).connect();
    }


    /**
     * This will find server classes for a ConnectionSettings by using the supplied
     * LibraryURI of the ConnectionSettings and searching for the ConnectClassPathEntries
     * of the ConnectionType supplied with the settings.
     * <p/>
     * This method should only be called once. If the entries need to be reset, you should
     * first clear the settings' classpath entries. This method appends class path entries
     * to the existing list.
     *
     * @param connectionSettings the ConnectionSettings to update with recommended class
     *                           path entries
     */
    public void discoverServerClasses(ConnectionSettings connectionSettings) {
        if (connectionSettings.getLibraryURI() != null) {
            long start = System.currentTimeMillis();
            String[] serverFiles = connectionSettings.getConnectionType().getConnectionClasspathEntries();

            // No server file dependencies
            if (serverFiles == null)
                return;

            File serverInstall = new File(connectionSettings.getLibraryURI());
            if (!serverInstall.exists())
                throw new LoadException("Supplied server installation does not exist " + connectionSettings.getLibraryURI());


            List<File> foundFiles = new ArrayList<File>();

            for (String serverFile : serverFiles) {
                if (broadSearch && serverFile.indexOf('/') >= 0) {
                    serverFile = serverFile.substring(serverFile.lastIndexOf('/')+1);
                }


                log.debug("Searching for library " + serverFile);
                File[] matchedFiles = null;
                try {
                    if (serverFile.indexOf('/') >= 0) {
                        matchedFiles = findDeepFiles(serverInstall, serverFile);
                    } else {
                        File file = findFile(serverInstall, serverFile);
                        if (file != null)
                            matchedFiles = new File[]{file};
                    }
                } catch (Exception e) {
                    log.info("Library dependency not found " + serverFile, e);
                }

                if (matchedFiles != null) {
                    for (File matchedFile : matchedFiles) {
                        if (matchedFile != null && !foundFiles.contains(matchedFile)) {
                            foundFiles.add(matchedFile);
                            log.debug("Library dependency resolved " + matchedFile.getAbsolutePath());
                        }
                    }
                } else {
                    log.info("Connection library dependancy [" + serverFile+ "] not found  in directory: " + serverInstall);
                }
            }

            if (connectionSettings.getClassPathEntries() == null)
                connectionSettings.setClassPathEntries(foundFiles);
            else
                connectionSettings.getClassPathEntries().addAll(foundFiles);

            log.info("Discovered libraries in " + (System.currentTimeMillis() - start) + " ms");
        }
    }


    private File findFile(File directory, String filename) {
        return findFile(directory, filename, 1);
    }

    private File findFile(File directory, String filename, int depth) {
        if (depth > searchDepth)
            return null;
        File[] children = directory.listFiles();
        if (children == null)
            return null;
        for (File child : children) {
            if (child.isDirectory()) {
                File result = findFile(child, filename, depth + 1);
                if (result != null)
                    return result;
            } else {
                if (filename.equalsIgnoreCase(child.getName()))
                    return child;
            }
        }
        return null;
    }


    private File[] findDeepFiles(File directory, String filename) {
        if (filename.startsWith("/"))
            filename = filename.substring(1);
        int in = filename.indexOf("/");
        if (in < 0) {

            if (filename.equals("*")) {
                return directory.listFiles();
            } else {
                File match = getChild(directory, filename);
                if (match == null)
                    return null;
                else
                    return new File[]{match};
            }

        } else {
            String dir = filename.substring(0, in);
            String restOfName = filename.substring(in + 1, filename.length());

            if (dir.equals("*")) {
                File[] children = directory.listFiles(new DirectoryFilter());
                if (children != null) {
                    for (File child : children) {
                        File[] childDir = findDeepFiles(child, restOfName);
                        if (childDir != null) {
                            return childDir;
                        }
                    }
                }
                log.debug("Could not find " + directory.getAbsolutePath() + " :: " + restOfName);
                return null;
            } else {
                File childDir = getChild(directory, dir);
                if (childDir == null) {
                    log.debug("Could not find " + directory.getAbsolutePath() + " :: " + restOfName);
                    return null;
                } else {
                    return findDeepFiles(childDir, restOfName);
                }
            }
        }
    }

    private static class DirectoryFilter implements FileFilter {
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }

    }

    public File getChild(File directory, final String childName) {
        if (directory == null)
            return null;
        if (!directory.exists())
            return null;
        File[] children = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return childName.equals(name);
            }
        });
        if (children.length == 1) {
            return children[0];
        } else {
            //log.info("Connection library dependency [" + childName + "] not found  in directory: " + directory.getAbsolutePath());
            return null;
        }
    }

}
