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

package org.mc4j.ems.connection.local;

import java.io.File;


public class LocalVirtualMachine {

    private String address;
    private String commandLine;
    private String displayName;
    private int vmid;
    private boolean isAttachSupported;
    private static final String LOCAL_CONNECTOR_ADDRESS_PROP = "com.sun.management.jmxremote.localConnectorAddress";

    public LocalVirtualMachine(int vmid, String commandLine, boolean attachSupported, String connectAddress) {
        this.vmid = vmid;
        this.commandLine = commandLine;
        address = connectAddress;
        isAttachSupported = attachSupported;
        displayName = getDisplayName(commandLine);
    }

    private static String getDisplayName(String s) {
        String as[] = s.split(" ", 2);
        if (as[0].endsWith(".jar")) {
            File file = new File(as[0]);
            String s1 = file.getName();
            if (as.length == 2)
                s1 = (new StringBuilder()).append(s1).append(" ").append(as[1]).toString();
            return s1;
        } else {
            return s;
        }
    }



    public int getVmid() {
        return vmid;
    }

    public boolean isManageable() {
        return address != null;
    }

    public boolean isAttachable() {
        return isAttachSupported;
    }

    public void setConnectorAddress(String address) {
        this.address = address;
    }

    public String getConnectorAddress() {
        return address;
    }

    public String getDisplayName() {
        return displayName;
    }


    public String getCommandLine() {
        return commandLine;
    }

    public String toString() {
        return commandLine;
    }
}