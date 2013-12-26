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

package org.mc4j.ems.impl.jmx.connection.support.providers.local;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServer;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.spi.AttachProvider;
import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.MonitoredVmUtil;
import sun.jvmstat.monitor.VmIdentifier;
import sun.management.ConnectorAddressLink;

import org.mc4j.ems.connection.local.LocalVirtualMachine;

public class LocalVMConnector {

    public static void main(String[] args) throws Exception {

        //String path = System.getProperty("java.library.path");
        //path = path + ";C:\\jdk\\jdk1.6.0\\jre\\bin";
        //System.setProperty("java.library.path",path);
        //System.out.println("Path: " + path);
//        System.loadLibrary("management.dll");
//        sun.management.Agent.startAgent();


        List<VirtualMachineDescriptor> descriptors = VirtualMachine.list();
        for (VirtualMachineDescriptor vm : descriptors) {
            System.out.println("VM: " + vm.displayName());
        }

        MonitoredHost monitoredHost = MonitoredHost.getMonitoredHost(new HostIdentifier((String) null));
        Set all = monitoredHost.activeVms();


        System.out.println("Providers List:");
        for (AttachProvider p : AttachProvider.providers()) {
            System.out.println("\t" + p);
        }

        Map<Integer, LocalVirtualMachine> m = getAllVirtualMachines();
        for (Object e : m.entrySet()) {
            Map.Entry<Integer, LocalVirtualMachine> entry = (Map.Entry<Integer, LocalVirtualMachine>) e;
            System.out.println(entry.getKey() + "::" + entry.getValue());
            System.out.println("\tattachable: " + entry.getValue().isAttachable() + " manageable: " + entry.getValue().isManageable());
            if (entry.getValue().isAttachable() && !entry.getValue().isManageable()) {
                startManagementAgent(entry.getValue());
                if (!entry.getValue().isManageable())
                    throw new RuntimeException("Couldn't start management agent");
                System.out.println("\tAddress is now: " + entry.getValue().getConnectorAddress());
            }

        }
    }


    public MBeanServer getMBeanServer() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void doConnect() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public static Map<Integer, LocalVirtualMachine> getAllVirtualMachines() {
        Map<Integer, LocalVirtualMachine> hashmap = new HashMap<Integer, LocalVirtualMachine>();
        getMonitoredVMs(hashmap);
        getAttachableVMs(hashmap);
        return hashmap;
    }

    public static Map<Integer, LocalVirtualMachine> getAllMonitorableVirtualMachines() {
        Map<Integer, LocalVirtualMachine> vms = getAllVirtualMachines();
        Map<Integer, LocalVirtualMachine> manageableVMs = new HashMap<Integer, LocalVirtualMachine>();
        for (Map.Entry<Integer, LocalVirtualMachine> entry : vms.entrySet()) {
            if (entry.getValue().isAttachable() || entry.getValue().isManageable()) {
                manageableVMs.put(entry.getKey(), entry.getValue());
            }
        }
        return manageableVMs;
    }

    private static void getMonitoredVMs(Map<Integer, LocalVirtualMachine> map) {
        MonitoredHost monitoredhost;
        Set set;
        try {
            monitoredhost = MonitoredHost.getMonitoredHost(new HostIdentifier((String) null));
            set = monitoredhost.activeVms();
        }
        catch (URISyntaxException urisyntaxexception) {
            throw new InternalError(urisyntaxexception.getMessage());
        }
        catch (MonitorException monitorexception) {
            throw new InternalError(monitorexception.getMessage());
        }

        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof Integer) {
                int i = ((Integer) obj).intValue();
                String s = obj.toString();
                boolean flag = false;
                String s1 = null;
                try {
                    MonitoredVm monitoredvm = monitoredhost.getMonitoredVm(new VmIdentifier(s));
                    s = MonitoredVmUtil.commandLine(monitoredvm);
                    flag = MonitoredVmUtil.isAttachable(monitoredvm);
                    s1 = ConnectorAddressLink.importFrom(i);
                    monitoredvm.detach();
                }
                catch (Exception exception) {
                }
                map.put((Integer) obj, new LocalVirtualMachine(i, s, flag, s1));
            }
        }
    }

    private static void getAttachableVMs(Map<Integer, LocalVirtualMachine> map) {
        List<VirtualMachineDescriptor> list = VirtualMachine.list();  // This is the offending call for the library load error
        for (VirtualMachineDescriptor virtualmachinedescriptor : list) {
            try {
                Integer integer = Integer.valueOf(virtualmachinedescriptor.id());
                if (!map.containsKey(integer)) {
                    boolean flag = false;
                    String s = null;
                    try {
                        VirtualMachine virtualmachine = VirtualMachine.attach(virtualmachinedescriptor);
                        flag = true;
                        Properties properties = virtualmachine.getAgentProperties();
                        s = (String) properties.get("com.sun.management.jmxremote.localConnectorAddress");
                        virtualmachine.detach();
                    }
                    catch (AttachNotSupportedException attachnotsupportedexception) {
                    }
                    catch (IOException ioexception) {
                    }
                    map.put(integer, new LocalVirtualMachine(integer.intValue(), virtualmachinedescriptor.displayName(), flag, s));
                }
            }
            catch (NumberFormatException numberformatexception) {
            }
        }
    }


    public static void startManagementAgent(LocalVirtualMachine lvm)
        throws IOException {

        if (!lvm.isAttachable())
            throw new IOException((new StringBuilder()).
                append("This virtual machine \"").
                append(lvm.getVmid()).append("\" does not support dynamic attach.").toString());


        VirtualMachine virtualmachine = null;
        String s = String.valueOf(lvm.getVmid());
        try {
            virtualmachine = VirtualMachine.attach(s);
        }
        catch (AttachNotSupportedException attachnotsupportedexception) {
            IOException ioexception = new IOException(attachnotsupportedexception.getMessage());
            ioexception.initCause(attachnotsupportedexception);
            throw ioexception;
        }
        String s1 = virtualmachine.getSystemProperties().getProperty("java.home");
        String s2 = (new StringBuilder()).append(s1).append(File.separator).append("jre").append(File.separator).append("lib").append(File.separator).append("management-agent.jar").toString();
        File file = new File(s2);
        if (!file.exists()) {
            s2 = (new StringBuilder()).append(s1).append(File.separator).append("lib").append(File.separator).append("management-agent.jar").toString();
            file = new File(s2);
            if (!file.exists())
                throw new IOException("Management agent not found");
        }
        s2 = file.getCanonicalPath();
        try {
            virtualmachine.loadAgent(s2, "com.sun.management.jmxremote");
        }
        catch (AgentLoadException agentloadexception) {
            IOException ioexception1 = new IOException(agentloadexception.getMessage());
            ioexception1.initCause(agentloadexception);
            throw ioexception1;
        }
        catch (AgentInitializationException agentinitializationexception) {
            IOException ioexception2 = new IOException(agentinitializationexception.getMessage());
            ioexception2.initCause(agentinitializationexception);
            throw ioexception2;
        }
        Properties properties = virtualmachine.getAgentProperties();
        lvm.setConnectorAddress((String) properties.get("com.sun.management.jmxremote.localConnectorAddress"));
        virtualmachine.detach();
    }
}
