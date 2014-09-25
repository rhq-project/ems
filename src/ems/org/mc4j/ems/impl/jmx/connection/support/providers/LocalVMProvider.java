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

package org.mc4j.ems.impl.jmx.connection.support.providers;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mc4j.ems.connection.EmsConnectException;
import org.mc4j.ems.connection.local.LocalVirtualMachine;
import org.mc4j.ems.impl.jmx.connection.support.providers.local.LocalVMConnector;

public class LocalVMProvider extends JMXRemotingConnectionProvider {

    private static Log log = LogFactory.getLog(LocalVMProvider.class);

    public static Map<Integer, LocalVirtualMachine> getManageableVirtualMachines() {

        try {
            Class.forName("com.sun.tools.attach.VirtualMachine");
            Class.forName("sun.jvmstat.monitor.HostIdentifier");
            Class.forName("sun.management.ConnectorAddressLink");
        } catch (ClassNotFoundException e) {
           log.debug("Can not lookup local virtual machines from this VM. Sun JDK 1.6 (mustang) or greater required for local vm lookups.");
           return Collections.emptyMap();
        }
        Map<Integer, LocalVirtualMachine> m = LocalVMConnector.getAllMonitorableVirtualMachines();

        // Don't want to expose our Local VM
        Map<Integer, LocalVirtualMachine> vms = new HashMap<Integer,LocalVirtualMachine>();
        for (Map.Entry<Integer, LocalVirtualMachine> entry : m.entrySet()) {
            vms.put(entry.getKey(),entry.getValue());
        }

        return vms;
    }


    protected void doConnect() {

        Integer vmid = Integer.parseInt(this.connectionSettings.getServerUrl());
        LocalVirtualMachine lvm = LocalVMConnector.getAllMonitorableVirtualMachines().get(vmid);
        if (lvm == null) {
            throw new EmsConnectException("Couldn't find VirtualMachine with id " + vmid);
        }

        if (!lvm.isManageable()) {
            try {
                LocalVMConnector.startManagementAgent(lvm);
            } catch (IOException e) {
                throw new EmsConnectException("Couldn't make vm manageable through agent attachment",e);
            }
        }
        connectionSettings.setServerUrl(lvm.getConnectorAddress());

        super.doConnect();
    }
}
