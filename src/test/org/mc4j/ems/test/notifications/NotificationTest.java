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
package org.mc4j.ems.test.notifications;

import java.util.SortedSet;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import org.mc4j.ems.connection.ConnectionFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.notification.EmsNotification;
import org.mc4j.ems.connection.settings.ConnectionSettings;
import org.mc4j.ems.connection.support.metadata.JSR160ConnectionTypeDescriptor;
import org.mc4j.ems.test.beans.MyTestBean;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Nov 10, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class NotificationTest  {

    String testMBeanName = "ems.test:type=test,name=MyTestMBean";

    EmsConnection connection;

    EmsBean testBean;

    @Configuration(beforeTestClass = true)
    public void testSetup() {
        System.out.println("Here");
        ConnectionSettings settings = new ConnectionSettings();
        settings.initializeConnectionType(new JSR160ConnectionTypeDescriptor());
        settings.setServerUrl("service:jmx:rmi:///jndi/rmi://localhost:9999/server");

        ConnectionFactory factory = new ConnectionFactory();
        connection = factory.connect(settings);

        connection.loadSynchronous(true);
    }

    @Test(groups= {"registration"})
    public void testRegistration() {
        testBean = connection.registerBean(MyTestBean.class.getName(), testMBeanName);

    }

    @Test(groups = { "functest" })
    public void testNotificationRegister() {

        SortedSet<EmsNotification> notifications = testBean.getNotifications();

        for (EmsNotification notification : notifications) {

            System.out.println("Notif: " + notification);
            notification.startListening();
        }
    }



}
