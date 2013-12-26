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

package org.mc4j.ems.store;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 6, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class CompleteValueHistory implements ValueHistory {

    /**
     * Newer values are added at the end of the list. Oldest values first.
     */
    protected List<Value> values = new LinkedList<Value>();

    protected int historySize;

    public CompleteValueHistory() {
        this(-1);
    }

    public CompleteValueHistory(int historySize) {
        this.historySize = historySize;
    }

    public int getHistorySize() {
        return values.size();
    }

    public void setMaximumHistorySize(int historySize) {
        this.historySize = historySize;
    }

    public List<Value> getValues() {
        return Collections.unmodifiableList(values);
    }

    public void addValue(Value value) {
        values.add(value);
        if (historySize > 0 && historySize < values.size()) {
            values.remove(0);
        }
    }


}
