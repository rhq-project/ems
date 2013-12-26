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
package org.mc4j.ems.connection.bean.attribute;

import org.mc4j.ems.store.ValueHistory;

/**
 * An MBean attribute.
 * 
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 4, 2005
 * @version $Revision: 620 $($Author: ianpspringer $ / $Date: 2010-08-02 21:03:05 +0200 (Mo, 02 Aug 2010) $)
 */
public interface EmsAttribute extends Comparable {
    /** Default true */
    String CONTROL_ATTRIBUTE_HISTORY = "Attribute.history";
    /** Default CompleteValueHistory */
    String CONTROL_ATTRIBUTE_HISTORY_CLASS = "Attribute.history.class";
    /** Default 1 */
    String CONTROL_ATTRIBUTE_HISTORY_DEPTH = "Attribute.history.depth";


    void registerAttributeChangeListener(AttributeChangeListener listener);

    /**
     * Returns the locally stored value of this attribute. Does not ask the server for the current value.
     *
     * @return the locally stored value of this attribute
     */
    Object getValue();

    /**
     * Set the attribute's value on the server.
     *
     * @param newValue the value to be set
     *
     * @throws Exception if there was a failure to set the attribute
     */
    void setValue(Object newValue) throws Exception;

    /**
     * Alters the internally stored value of this attribute. Does not update the
     * server value. Is intended for mass updates of attribute data via the MBean.
     *
     * @param newValue the value to be set
     */
    void alterValue(Object newValue);

    /**
     * Updates the local value of this attribute from the server.
     */
    Object refresh();

    /**
     *
     * @return
     */
    ValueHistory getValueHistory();

    /**
     *
     * @return
     */
    String getName();

    /**
     *
     * @return
     */
    String getType();

    /**
     *
     * @return
     */
    Class getTypeClass();

    /**
     *
     * @return
     */
    boolean isNumericType();

    String getDescription() ;

    /**
     * Returns true if this attribute is readable, or false if it is not.
     *
     * @return true if this attribute is readable, or false if it is not
     */
    boolean isReadable();

    /**
     * Returns true if this attribute is writable, or false if it is not.
     *
     * @return true if this attribute is writable, or false if it is not
     */
    boolean isWritable();

    /**
     *
     * @return
     */
    boolean isSupportedType();

    /**
     *
     * @param supportedType
     */
    void setSupportedType(boolean supportedType);

    /**
     *
     * @return
     */
    int getValueSize();
}
