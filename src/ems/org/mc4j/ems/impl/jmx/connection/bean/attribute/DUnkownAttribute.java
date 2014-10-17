/*
 * Copyright 2002-2005 Greg Hinkle
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
package org.mc4j.ems.impl.jmx.connection.bean.attribute;

import org.mc4j.ems.impl.jmx.connection.bean.DMBean;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Nov 16, 2005
 * @version $Revision: 570 $($Author: ghinkl $ / $Date: 2006-04-12 21:14:16 +0200 (Mi, 12 Apr 2006) $)
 */
public class DUnkownAttribute extends DAttribute {

    protected String name;

    public DUnkownAttribute(DMBean bean, String name) {
        super(null, bean);
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public String getType() {
        return "Unknown";
    }


    public String getDescription() {
        return "Unkown";
    }

    public boolean isWritable() {
        return true;
    }

    public boolean isReadable() {
        return true;
    }

    

}
