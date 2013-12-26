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

package org.mc4j.ems.connection.bean.operation;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mc4j.ems.connection.EmsInvocationException;
import org.mc4j.ems.connection.bean.parameter.EmsParameter;

/**
 * An MBean operation.
 * 
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 4, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public interface EmsOperation extends Comparable {


    String getName();

    String getDescription();

    List<EmsParameter> getParameters();

    String getReturnType();

    Impact getImpact();

    Object invoke(Object... parameters) throws EmsInvocationException;



    public static class Impact implements Serializable {

        private int ordinal;
        private String name;

        public static Impact INFO = new Impact(0,"Info");
        public static Impact ACTION = new Impact(0,"Action");
        public static Impact ACTION_INFO = new Impact(0,"Action Info");
        public static Impact UNKNOWN = new Impact(0,"Unknown");

        private static final Impact[] arrayValues = {INFO, ACTION, ACTION_INFO, UNKNOWN };
        public static final List VALUES = Collections.unmodifiableList(Arrays.asList(arrayValues));
        private Impact(int ordinal, String name) {
            this.ordinal = ordinal;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getOrdinal() {
            return ordinal;
        };

        private Object readResolve() throws ObjectStreamException {
            return arrayValues[ordinal];
        }

    }
}
