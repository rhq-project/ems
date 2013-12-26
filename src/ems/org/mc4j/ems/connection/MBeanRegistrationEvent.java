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

package org.mc4j.ems.connection;

import java.util.Set;

import org.mc4j.ems.connection.bean.EmsBean;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 4, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class MBeanRegistrationEvent {
    private Set<EmsBean> registered;
    private Set<EmsBean> deregistered;


    public MBeanRegistrationEvent(Set<EmsBean> registered, Set<EmsBean> deregistered) {
        this.registered = registered;
        this.deregistered = deregistered;
    }

    public Set<EmsBean> getRegistered() {
        return registered;
    }

    public Set<EmsBean> getDeregistered() {
        return deregistered;
    }
}
