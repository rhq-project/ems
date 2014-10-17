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

package org.mc4j.ems.connection.support.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mc4j.ems.connection.ConnectionFactory;
import org.mc4j.ems.connection.EmsConnectException;
import org.mc4j.ems.connection.EmsException;
import org.mc4j.ems.connection.settings.ConnectionSettings;
import org.mc4j.ems.connection.support.metadata.JSR160ConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.LocalVMTypeDescriptor;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 5, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class ClassLoaderFactory {

    private static ClassLoaderFactory INSTANCE;

    private static Log log = LogFactory.getLog(ClassLoaderFactory.class);

    private static Map<String, File> jarCache = new HashMap<String, File>();

    private static Map<FileKey, File> tempJarCache = Collections.synchronizedMap(new HashMap<FileKey, File>());

    private static Map<Long, WeakReference<ClassLoader>> classLoaderCache = Collections.synchronizedMap(new HashMap<Long, WeakReference<ClassLoader>>());

    static {
        String className = System.getProperty("org.mc4j.ems.classloaderfactory");
        if (className != null) {
            try {
                INSTANCE = ((Class<ClassLoaderFactory>) Class.forName(className)).newInstance();
            } catch (Exception e) {
                throw new EmsException("Unable to load custom classloader factory " + className, e);
            }
        }

        if (INSTANCE == null) {
            INSTANCE = new ClassLoaderFactory();
        }
    }

    /**
     * Retrieves the configured classloader factory for EMS. This can be customized by
     * setting the system property "org.mc4j.ems.classloaderfactory".
     *
     * @return the Classloader Factory used to build the connection classloader
     */
    public static ClassLoaderFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Clears this factory's caches. You usually only call this when you need
     * help cleaning out the classloaders created by this factory.
     */
    public static void clearCaches() {
    	jarCache.clear();
    	tempJarCache.clear();
    	classLoaderCache.clear();
    }

    /**
     * TODO GH: Implement a special classloader that can load classes from
     * within a jar inside another jar or perhaps just ship the impl jar separately...
     */
    protected URL storeImplToTemp(String archiveResource, File tempDir) {
        try {

            if (jarCache.containsKey(archiveResource)) {
                return jarCache.get(archiveResource).toURI().toURL();
            }

            InputStream is = ClassLoaderFactory.class.getClassLoader().getResourceAsStream(archiveResource);

            if (is == null) {
                throw new EmsException("Unable to find resource to store [" + archiveResource + "]");
            }

            // String tmpPath = System.getProperty("java.io.tmpdir");

            File tmpFile = copyFileToTemp(archiveResource, is, tempDir);

            jarCache.put(archiveResource, tmpFile);

            return tmpFile.toURI().toURL();

        } catch (FileNotFoundException e) {
            throw new EmsException("Unable to store jar [" + archiveResource + "] to temp dir [" + tempDir + "].", e);
        } catch (IOException e) {
            throw new EmsException("Unable to store jar [" + archiveResource + "] to temp dir [" + tempDir + "].", e);
        }
    }

    private File copyFileToTemp(String archiveResource, InputStream is, File directory) throws IOException {
        String jarName = new File(archiveResource).getName();
        jarName = jarName.substring(0, jarName.length() - 4);

        File tmpFile = File.createTempFile(jarName, ".jar", directory);
        tmpFile.deleteOnExit();

        log.trace("Copying jar [" + archiveResource + "] to temporary file [" + tmpFile.getAbsolutePath() + "]");

        FileOutputStream fos = new FileOutputStream(tmpFile);
        byte[] buffer = new byte[4096];
        int size = is.read(buffer);
        while (size != -1) {
            fos.write(buffer, 0, size);
            size = is.read(buffer);
        }
        fos.close();
        is.close();
        return tmpFile;
    }

/*

    private static String digest(String algorithm, ByteBuffer buffer) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(buffer.duplicate());
        byte[] digest = md.digest();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            result.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
//        new Formatter(System.out).format
//            ("%-5s: %0" + (digest.length * 2) + "x%n",
//                algorithm, new BigInteger(1, digest));
    }

*/


    public URL getCachedTempForFile(File file, File directory) throws MalformedURLException {
        try {
            FileKey key = new FileKey(file);
            File result = tempJarCache.get(key);
            if (result == null) {
                result = copyFileToTemp(file.getName(), new FileInputStream(file), directory);
                tempJarCache.put(key, result);
            }
            return result.toURI().toURL();
//        long modified = file.lastModified();
//        file.hashCode()
//        FileChannel channel = new RandomAccessFile(file, "r").getChannel();
//        file.le
//        ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int) channel.size());
//        digest("md5", buffer);
//        channel.close();
        } catch (IOException ioe) {
            log.debug("Could not create temporary copy of jar [" + file + "]", ioe);
            return file.toURI().toURL(); // Will have to refer directly to the file since we couldn't make a temp copy
        }
    }


    public ClassLoader buildClassLoader(ConnectionSettings settings) {

        String tempDirString = (String) settings.getControlProperties().get(ConnectionFactory.JAR_TEMP_DIR);
        File tempDir = null;
        if (tempDirString != null) {
            tempDir = new File(tempDirString);
        }

        Boolean useContextClassLoader = Boolean.valueOf(settings.getAdvancedProperties().getProperty(ConnectionFactory.USE_CONTEXT_CLASSLOADER, "false"));
        if (useContextClassLoader.booleanValue()) {
            return Thread.currentThread().getContextClassLoader();
        }

        List<URL> entries = new ArrayList<URL>();

        if (settings.getClassPathEntries() != null) {
            for (File file : settings.getClassPathEntries()) {
                try {
                    if (Boolean.valueOf(settings.getControlProperties().getProperty(ConnectionFactory.COPY_JARS_TO_TEMP, "false"))) {
                        entries.add(getCachedTempForFile(file,tempDir));
                    } else {
                        entries.add(file.toURI().toURL());
                    }
                } catch (MalformedURLException e) {
                    throw new EmsConnectException("Unable to read class path library url", e);
                }
            }
        }

        if (settings.getConnectionType() instanceof LocalVMTypeDescriptor) {
            // Need tools.jar if its not already loaded
            try {
                Class.forName("com.sun.tools.attach.VirtualMachine");
            } catch (ClassNotFoundException e) {
                // Try to load tools.jar
                File toolsJar = null;
                toolsJar = findToolsJarForHome(System.getProperty("java.home"));
                if (toolsJar == null) {
                    toolsJar = findToolsJarForHome(System.getProperty("env_java_home"));
                }

                if (toolsJar != null) {
                    try {
                        log.debug("Found tools.jar at " + toolsJar.getPath());
                        entries.add(toolsJar.toURI().toURL());
                    } catch (MalformedURLException e1) { /* Unnexpected */ }
                } else {
                    throw new EmsConnectException("Unable to find tools.jar. Add it to your classpath to use Sun local vm connections.");
                }
            }
        }

        if (entries.isEmpty()) {
            return ClassLoaderFactory.class.getClassLoader();
        }

        // TODO: Check if file exists, log warning if not

        URL[] entryArray = entries.toArray(new URL[entries.size()]);
        ClassLoader loader = null;

        long key = Arrays.hashCode(entryArray);

        WeakReference<ClassLoader> loaderReference = classLoaderCache.get(key);
        if (loaderReference != null) {
            loader = classLoaderCache.get(key).get();
        }

        if (loader == null) {

            // WARNING: Relatively disgusting hack. hiding classes is not a good thing
            if (settings.getConnectionType().isUseChildFirstClassLoader()) {
                loader = new ChildFirstClassloader(entryArray, ClassLoaderFactory.class.getClassLoader());
            } else {
                // TODO was NestedJarClassLoader
                //loader = new ChildFirstClassloader(entryArray, ClassLoaderFactory.class.getClassLoader());
                loader = new URLClassLoader(entryArray, ClassLoaderFactory.class.getClassLoader());
                //loader = new NestedJarClassLoader(entryArray, ClassLoaderFactory.class.getClassLoader());
            }

            classLoaderCache.put(key, new WeakReference<ClassLoader>(loader));

            if (log.isDebugEnabled()) {
                StringBuffer buf = new StringBuffer("Classloader built with: \n");
                for (URL url : entries) {
                    buf.append("\t").append(url).append("\n");
                }
                log.info(buf.toString());
            }

        }
        return loader;
    }

    private File findToolsJarForHome(String javaHome) {
        File toolsJar = null;
        if (javaHome != null) {
            File javaHomeDir = new File(javaHome);
            if (!javaHomeDir.exists() || !javaHomeDir.isDirectory())
                return null;
            toolsJar = findToolsJar(javaHomeDir);
            if (toolsJar == null) {
                toolsJar = findToolsJar(javaHomeDir.getParentFile());
            }
        }
        return toolsJar;
    }

    public File findToolsJar(File home) {
        File f = new File(home, "lib" + File.separator + "tools.jar");
        log.debug("Looking for tools jar at: " + f.getPath());
        return f.exists() && f.isFile()?f:null;
    }


    public static class FileKey {
        long lastModified;
        long size;
        File f;

        public FileKey(File f) {
            this.lastModified = f.lastModified();
            this.size = f.length();
            this.f = f;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FileKey fileKey = (FileKey) o;

            if (lastModified != fileKey.lastModified) return false;
            if (size != fileKey.size) return false;
            if (!f.equals(fileKey.f)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = (int) (lastModified ^ (lastModified >>> 32));
            result = 31 * result + (int) (size ^ (size >>> 32));
            result = 31 * result + f.hashCode();
            return result;
        }
    }


}
