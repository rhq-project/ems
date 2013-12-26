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

package org.mc4j.ems.test;

import java.util.SortedSet;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;

import org.mc4j.ems.connection.ConnectionFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.attribute.EmsAttribute;
import org.mc4j.ems.connection.settings.ConnectionSettings;
import org.mc4j.ems.connection.support.metadata.JBossConnectionTypeDescriptor;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 5, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class ConnectionTest {

    private EmsConnection connection;

    public static void main(String[] args) throws Exception, InstanceNotFoundException, IntrospectionException {
        ConnectionTest test = new ConnectionTest();
        try {
//            test.testUpdateSpeed();

            // test.testCreateRemove();
            Thread.sleep(10000);
        } finally{
            test.testClose();
        }
    }

    public ConnectionTest() throws Exception {
        ConnectionSettings settings = getSettings();


        ConnectionFactory factory = new ConnectionFactory();

        factory.discoverServerClasses(settings);

        EmsConnection connection = factory.connect(settings);

        connection.loadSynchronous(true);


        SortedSet<EmsBean> beans = connection.getBeans();
        for (EmsBean bean : beans) {
            System.out.println("Bean [" + bean.getBeanName() + "]" + bean);
            for (EmsAttribute attribute : bean.getAttributes()) {
                System.out.println("\t" + attribute.getName() + " - " + attribute.getDescription());
            }
        }

        this.connection = connection;
    }

    private ConnectionSettings getSettings() {
        ConnectionSettings settings = new ConnectionSettings();

        // Local JDK 5 test
//        settings.initializeConnectionType(new J2SE5ConnectionTypeDescriptor());
//        settings.setConnectionName("test");
//        settings.setServerUrl("service:jmx:rmi:///jndi/rmi://localhost:9777/jmxrmi");


        settings.initializeConnectionType(new JBossConnectionTypeDescriptor());
        settings.setConnectionName("JBoss Test");
        settings.setServerUrl("jnp://localhost:1099");
        //settings.setLibraryURI("/Users/ghinkle/development/tools/jboss-4.0.3SP1-installer");
        settings.setLibraryURI("C:\\tools\\jboss\\jboss-4.2.0.CR1");
        //settings.setPrincipal("admin"); settings.setCredentials("admin");



        // Local Tomcat/Chires test
//        settings.initializeConnectionType(new J2SE5ConnectionTypeDescriptor());
//        settings.setConnectionName("tomcat");
//        settings.setServerUrl("service:jmx:rmi:///jndi/rmi://localhost:9003/jmxrmi");
//        settings.setPrincipal("adminRole");
//        settings.setCredentials("admin");
//        settings.setClassPathEntries(new File[] {
//            new File("C:\\projects\\chires\\dist\\chires.jar"),
//            new File("E:\\tools\\jakarta-tomcat-5.0.27\\server\\lib\\catalina.jar"),
//            new File("E:\\tools\\jakarta-tomcat-5.0.27\\server\\lib\\catalina-optional.jar")
//        });


        // Weblogic 8.1 test
//        settings.initializeConnectionType(new WeblogicConnectionTypeDescriptor());
//        settings.setConnectionName("test");
//        settings.setCredentials("weblogic");
//        settings.setPrincipal("weblogic");
//        settings.setClassPathEntries(new File[] { new File("c:\\bea\\weblogic81\\server\\lib\\weblogic.jar")});
        

        // Weblogic 9 - jsr 77 test
//        settings.setConnectionName("test");
//        settings.setServerUrl("service:jmx:t3://localhost:7001/jndi/weblogic.management.mbeanservers.runtime");//edit");//domainruntime");
//        settings.setConnectionType(new Weblogic9Jsr77ConnectionTypeDescriptor());
//        settings.setConnectionName("test");
//        settings.setConnectionType(new Weblogic9ConnectionTypeDescriptor());
//        settings.setPrincipal("weblogic");
//        settings.setCredentials("weblogic");


        // Weblogic 9 - test
//        settings.setConnectionName("test");
//        settings.setServerUrl("service:jmx:t3://localhost:7001/jndi/weblogic.management.mbeanservers.runtime");//edit");//domainruntime");
//        settings.initializeConnectionType(new Weblogic9ConnectionTypeDescriptor());
//        settings.setConnectionName("test");
//        settings.setPrincipal("weblogic");
//        settings.setCredentials("weblogic");
//        settings.setClassPathEntries(Arrays.asList(new File[] {
//            new File("E:\\tools\\weblogic9\\weblogic90b\\server\\lib\\weblogic.jar")
//        }));


        return settings;


//        String xml = ConnectionSettingPersistence.getInstance().encodeSettings(settings);
//        System.out.println(xml);
//        return ConnectionSettingPersistence.getInstance().decodeSettings(xml);
    }

    public void testClose() {
        connection.close();
    }

    public void testUpdateSpeed() {
        long start = System.currentTimeMillis();
        for (EmsBean bean :connection.getBeans()) {
            bean.refreshAttributes();
        }
        System.out.println("Batched update time: " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        for (EmsBean bean :connection.getBeans()) {
            for (EmsAttribute attribute : bean.getAttributes()) {
                attribute.refresh();
//                System.out.println("Update history: " + attribute.getValueHistory().getValues().size());
            }
        }
        System.out.println("Individual update time: " + (System.currentTimeMillis() - start) + "ms");

    }

    public void testContinuousRefresh() throws Exception {

          while (true) {
            for (EmsBean bean :connection.getBeans()) {
                for (EmsAttribute attribute : bean.getAttributes()) {
                    if ("HeapFreeCurrent".equals(attribute.getName())) {
                        System.out.println(bean.getBeanName().getCanonicalName() + " - " + attribute.getValue());
                        System.out.println("Tracking history: " + attribute.getValueHistory().getHistorySize());
                    }
                }
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void testCreateRemove() throws Exception {
        final Boolean failure = Boolean.FALSE;

        connection.loadSynchronous(true);
        Thread t = new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < 100; i++) {
                    try {
                        connection.createMBean(TestService.class.getName(), "test:type=TestService,id=bean_" + i);
                        // connection.removeMBean(new ObjectName("test:type=TestService,id=bean_" + (i - 1)));
                        Thread.sleep(200);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
        Thread t2 = new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < 100; i++) {
                    try {
                        connection.removeMBean("test:type=TestService,id=bean_" + (i - 1));
                        Thread.sleep(210);
                    } catch (Exception e) {
                        try { Thread.sleep(300); } catch (InterruptedException e1) { }
                        e.printStackTrace();
                    }
                }
            }
        });
        t2.start();

        t.join();
        t2.join();

        connection.loadSynchronous(false);
    }
}
