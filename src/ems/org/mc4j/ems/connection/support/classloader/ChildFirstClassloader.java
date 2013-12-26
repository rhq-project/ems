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

import java.net.URL;
import java.net.URLClassLoader;

/**
 * WARNING: GH - DISGUSTING HACK
 * This is an aweful little hack that allows us to execute under jdk 1.5 (which includes jmx)
 * while utilizing the jmx classes we load from somewhere else. We just override the classloader
 * delegation for cases of the "javax.management" classes.
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 5, 2005
 * @version $Revision: 589 $($Author: ghinkl $ / $Date: 2008-04-10 21:52:05 +0200 (Do, 10 Apr 2008) $)
 */
public class ChildFirstClassloader extends URLClassLoader {

        public ChildFirstClassloader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        protected synchronized Class loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
            Class c = findLoadedClass(name);
            if (c == null) {
                if (name.indexOf("org.mc4j") == -1 ||
                        name.indexOf("org.apache.commons.logging") == -1) { //true) {//name.indexOf("javax.management") >= 0) {
//                   if (name.contains("log4j.xml"))
//                      System.out.println("Looking for " + name);
                    try {
                        try {
                            c = findClass(name);
                        } catch (SecurityException se) {
                            int i = name.lastIndexOf('.');
                            String pkgname = name.substring(0, i);
                            // Check if package already loaded.
                            Package pkg = getPackage(pkgname);
                            if (pkg == null) {
                                definePackage(pkgname, null, null, null, null, null, null, null);
                            }
                        }
                        if (resolve) {
                            resolveClass(c);
                        }
                    } catch (ClassNotFoundException cnfe) {
                        c = super.loadClass(name, resolve);
                    }
                } else {
                    c = super.loadClass(name, resolve);
                }
            }
            return c;
        }

   @Override
   public URL getResource(String name)
   {
      URL res = findResource(name);
      if (res == null)
         res = super.getResource(name);
      return res;
   }

/*
        private Class defineClass(String name, Resource res) throws IOException {
   int i = name.lastIndexOf('.');
   URL url = res.getCodeSourceURL();
   if (i != -1) {
       String pkgname = name.substring(0, i);
       // Check if package already loaded.
       Package pkg = getPackage(pkgname);
       Manifest man = res.getManifest();
       if (pkg != null) {
      // Package found, so check package sealing.
      if (pkg.isSealed()) {
          // Verify that code source URL is the same.
          if (!pkg.isSealed(url)) {
         throw new SecurityException(
             "sealing violation: package " + pkgname + " is sealed");
          }

      } else {
          // Make sure we are not attempting to seal the package
          // at this code source URL.
          if ((man != null) && isSealed(pkgname, man)) {
         throw new SecurityException(
             "sealing violation: can't seal package " + pkgname +
             ": already loaded");
          }
      }
       } else {
      if (man != null) {
          definePackage(pkgname, man, url);
      } else {
                    definePackage(pkgname, null, null, null, null, null, null, null);
                }
       }
   }*/

}