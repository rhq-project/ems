package org.mc4j.ems.store;

import java.util.List;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 12, 2005
 * @version $Revision: 570 $($Author: ghinkl $ / $Date: 2006-04-12 21:14:16 +0200 (Mi, 12 Apr 2006) $)
 */
public interface ValueHistory {
    int getHistorySize();

    List<Value> getValues();

    void addValue(Value value);
}
