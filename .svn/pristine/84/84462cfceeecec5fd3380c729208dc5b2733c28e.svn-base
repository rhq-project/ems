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
package org.mc4j.ems.impl.jmx.connection.support.providers;

import java.lang.management.ManagementFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.mc4j.ems.connection.support.metadata.InternalVMTypeDescriptor;

/**
 * Connect to the platform mbean server in the VM that EMS is running in.
 *
 * @author Greg Hinkle
 */
public class InternalVMProvider extends AbstractConnectionProvider
{

   protected MBeanServer server;

   public MBeanServer getMBeanServer()
   {
      return this.server;
   }

   protected void doConnect() throws Exception
   {
      String mbeanSearch =
          (String) getConnectionSettings().getAdvancedProperties().get(
              InternalVMTypeDescriptor.DEFAULT_DOMAIN_SEARCH);
      MBeanServer foundServer = null;
      if (mbeanSearch != null)
      {
          ArrayList mbeanServers = AccessController.doPrivileged(new PrivilegedAction<ArrayList>()
          {
              public ArrayList run()
              {
                  return MBeanServerFactory.findMBeanServer(null);
              }
          });
          for (Object mbeanServerObj : mbeanServers)
          {
              MBeanServer mbeanServer = (MBeanServer) mbeanServerObj;
              if (mbeanSearch.equals(mbeanServer.getDefaultDomain()))
              {
                  foundServer = mbeanServer;
              }
          }
      }

      if (foundServer == null)
      {
          foundServer = ManagementFactory.getPlatformMBeanServer();
      }
      this.server = foundServer;
   }
}
