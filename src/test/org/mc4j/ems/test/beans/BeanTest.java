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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.management.ObjectName;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import org.mc4j.ems.connection.ConnectionFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.attribute.EmsAttribute;
import org.mc4j.ems.connection.bean.notification.EmsNotification;
import org.mc4j.ems.connection.bean.notification.EmsNotificationEvent;
import org.mc4j.ems.connection.bean.notification.EmsNotificationListener;
import org.mc4j.ems.connection.settings.ConnectionSettings;
import org.mc4j.ems.connection.support.metadata.J2SE5ConnectionTypeDescriptor;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Nov 11, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class BeanTest {

    EmsConnection connection;

    EmsBean testBean;

    String name;

    MyTestBeanMBean proxy;

    String testMBeanName = "ems.test:type=test,name=MyTestMBean";

    @Configuration(beforeTestClass = true)
    public void setup() {
        System.out.println("Here");
        ConnectionSettings settings = new ConnectionSettings();
        settings.initializeConnectionType(new J2SE5ConnectionTypeDescriptor());
        settings.setServerUrl("service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi");

        ConnectionFactory factory = new ConnectionFactory();
        connection = factory.connect(settings);

        connection.loadSynchronous(true);

    }


    @Test(groups = "functest")
    public void testRegistration() throws ClassNotFoundException {
        testBean = connection.registerBean(MyTestBean.class.getName(), testMBeanName);

        name = testBean.getBeanName().toString();

    }

    @Test(dependsOnMethods = {"testRegistration"})
    public void testLookup() {
        EmsBean bean = connection.getBean(name);
        assert bean != null; // Found bean?

        ObjectName o;
        bean = connection.getBean(testMBeanName);
        assert bean != null;

    }

    @Test(dependsOnMethods = {"testRegistration"})
    public void testProxy() throws ClassNotFoundException {
        Object obj = testBean.getProxy(MyTestBeanMBean.class);

        assert obj instanceof MyTestBeanMBean;

        proxy = (MyTestBeanMBean) obj;

        String msgVal = (String) testBean.getAttribute("message").refresh();
        assert proxy.getMessage().equals(msgVal);
    }

    @Test(dependsOnMethods = {"testRegistration"})
    public void testAttributes() throws Exception {
        SortedSet<EmsAttribute> attributes = testBean.getAttributes();
        System.out.println("Attributes found: " + attributes.size());
//        assert attributes.size() == 3;

        EmsAttribute msgAttr = testBean.getAttribute("message");

        assert msgAttr != null;

        Class typeClass = msgAttr.getTypeClass();
        assert typeClass.equals(String.class);

        // Try refreshing
        assert msgAttr.refresh().equals(MyTestBeanMBean.DEFAULT_MESSAGE);

        msgAttr.setValue("New Value");

        assert msgAttr.getValue().equals("New Value");
    }


    @Test(dependsOnMethods = {"testRegistration", "testProxy"})
    public void testNotifications() throws InterruptedException {

        SortedSet<EmsNotification> notifications = testBean.getNotifications();

        for (EmsNotification notification : notifications) {
            System.out.println(notification.getName());
        }


        final List<EmsNotificationEvent> recievedEvents = new ArrayList<EmsNotificationEvent>();

        EmsNotification notification = testBean.getNotification("attributeChange");
        notification.addNotificationListener(new EmsNotificationListener() {
            public void handleNotification(EmsNotificationEvent event) {
                recievedEvents.add(event);
                System.out.println("Notification received: " + event.toString());
            }
        });
        notification.startListening();

        proxy.setMessage("New Message");

        Thread.sleep(500);

        assert recievedEvents.size() == 1;
        EmsNotificationEvent event = recievedEvents.get(0);
        assert event.getBean() == testBean;

    }

}
