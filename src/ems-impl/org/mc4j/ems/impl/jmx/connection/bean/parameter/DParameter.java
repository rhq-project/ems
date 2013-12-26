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

package org.mc4j.ems.impl.jmx.connection.bean.parameter;

import javax.management.MBeanParameterInfo;

import org.mc4j.ems.connection.bean.parameter.EmsParameter;

/**
 * Created: Jul 20, 2005 1:12:14 AM
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net)
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class DParameter implements EmsParameter, Comparable {

    private String name;
    private String description;
    private String type;

    public DParameter(MBeanParameterInfo parameterInfo) {
        this.name = parameterInfo.getName();
        this.description = parameterInfo.getDescription();
        this.type = parameterInfo.getType();

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DParameter)) return false;

        DParameter that = (DParameter) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    public int compareTo(Object o) {
        int i =  this.name.compareTo(((DParameter)o).getName());
        if (i == 0) {
            i = this.getType().compareTo(((DParameter)o).getType());
        }
        return i;
    }
}
