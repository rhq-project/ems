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
package org.mc4j.ems.test.beans;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcasterSupport;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Nov 10, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class MyTestBean extends NotificationBroadcasterSupport implements MyTestBeanMBean {

    private String message = DEFAULT_MESSAGE;

    private long number = 1L;

    private long sequenceNumber = 0L;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        sendNotification(
            new AttributeChangeNotification(this, sequenceNumber++, System.currentTimeMillis(),
                    "Attribute changed", "message", "String", this.message, message));

        this.message = message;
    }

    public Long getNumber() {
            sendNotification(
            new AttributeChangeNotification(this, sequenceNumber++, System.currentTimeMillis(),
                    "Attribute changed", "number", "Long", this.number, number));
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[] {
                new MBeanNotificationInfo(new String[] {"custom.event"},"event","A special custom evemt"),
                new MBeanNotificationInfo(new String[] {AttributeChangeNotification.ATTRIBUTE_CHANGE},"attributeChange","An attribute changed")
        };
    }


}
