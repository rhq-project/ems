package org.mc4j.ems.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.mc4j.ems.connection.ConnectionFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.attribute.EmsAttribute;
import org.mc4j.ems.connection.settings.ConnectionSettings;
import org.mc4j.ems.connection.support.metadata.J2SE5ConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.JBossConnectionTypeDescriptor;

/**
 * Created by IntelliJ IDEA.
 * User: ghinkle
 * Date: Oct 25, 2005
 * Time: 1:42:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class MultiConnectionTest  {


    public static void main2(String[] args) throws MalformedURLException, ClassNotFoundException {
        URLClassLoader a = new URLClassLoader(new URL[] { new URL("file:/Users/ghinkle/Desktop/Downloads/jboss-3.2.5/lib/jboss-jmx.jar")});
        URLClassLoader b = new URLClassLoader(new URL[] { new URL("file:/Users/ghinkle/development/tools/jboss-4.0.2/lib/jboss-jmx.jar")});

        Class.forName("javax.management.MBeanServer",true,a);
        Class.forName("javax.management.MBeanServer",true,b);


    }

    public static void main(String[] args) throws InterruptedException {

        ConnectionSettings jboss = new ConnectionSettings();
        jboss.initializeConnectionType(new JBossConnectionTypeDescriptor());

        jboss.setConnectionName("JBoss Test");
//        jboss.setServerUrl("jnp://10.0.1.2:2099");//edit");//domainruntime");
        jboss.setServerUrl("jnp://127.0.0.1:1099");

//        jboss.setLibraryURI("/Users/ghinkle/development/tools/jboss-3.2.3");
//        jboss.setLibraryURI("/Users/ghinkle/development/tools/jboss-3.2.4");
//        jboss.setLibraryURI("/Users/ghinkle/development/tools/jboss-3.2.5");
        jboss.setLibraryURI("/Users/ghinkle/development/tools/jboss-4.0.3SP1-installer/");

//        jboss.setPrincipal("admin"); jboss.setCredentials("admin");

        ConnectionFactory factory = new ConnectionFactory();

        factory.discoverServerClasses(jboss);

        EmsConnection jbossConnection = factory.connect(jboss);

        long start = System.currentTimeMillis();
        jbossConnection.loadSynchronous(true);
        System.out.println("Loaded " + jbossConnection.getBeans().size() + " in " + (System.currentTimeMillis()-start) + "ms");


        EmsBean bean1 = jbossConnection.getBean("jboss.mq.destination:service=Queue,name=A"); //"jboss.mq:service=MessageCache");
        System.out.println("QUEUE SEARCH: " + bean1);

        List beans = jbossConnection.queryBeans("jboss:service=*");
        System.out.println("Found " + beans.size() + " beans from query.");

        EmsBean bean = jbossConnection.getBean("jboss:service=TransactionManager"); //"jboss.mq:service=MessageCache");
        //jbossConnection.getBean("jboss.system:type=ServerInfo");
        EmsAttribute attribute = bean.getAttribute("TransactionCount"); //"TotalCacheSize");
/*
        for (int i=0;i<20;i++) {
            try {
                Thread.sleep(10000);

                System.out.println("Attribute " + attribute.getName() + ": " + attribute.refresh());
            } catch(Exception e) {
                System.out.println("Exc: " + e.getClass().getName());
            }
        }*/
    }

    public static void main3(String[] args) {


        ConnectionSettings jboss32 = new ConnectionSettings();
        jboss32.initializeConnectionType(new JBossConnectionTypeDescriptor());

        jboss32.setConnectionName("JBoss 3.2");
        jboss32.setServerUrl("jnp://localhost:1099");//edit");//domainruntime");
        jboss32.setLibraryURI("/Users/ghinkle/Desktop/Downloads/jboss-3.2.5");

        ConnectionFactory factory = new ConnectionFactory();

        factory.discoverServerClasses(jboss32);

        EmsConnection jboss32Connection = factory.connect(jboss32);


        ConnectionSettings jboss4 = new ConnectionSettings();
        jboss4.initializeConnectionType(new JBossConnectionTypeDescriptor());

        jboss4.setConnectionName("JBoss 3.2");
        jboss4.setServerUrl("jnp://10.0.1.2:2099");//edit");//domainruntime");
        jboss4.setLibraryURI("/Users/ghinkle/development/tools/jboss-4.0.2/");


        factory.discoverServerClasses(jboss4);

        EmsConnection jboss4Connection = factory.connect(jboss4);

        jboss4Connection.loadSynchronous(false);

//        EmsBean bean = jboss4Connection.getBean("jboss.management.local:EJBModule=hq-entity.jar,J2EEApplication=hq.ear,J2EEServer=Local,j2eeType=EntityBean,name=LocalPlatform");
//
//        Stats stats = (Stats) bean.getAttribute("stats").refresh();
//        System.out.println(stats.getClass());

        jboss32Connection.loadSynchronous(false);


        System.out.println("JBoss 3.2 - bean count: " + jboss32Connection.getBeans().size());
        System.out.println("JBoss 4.0.2 - bean count: " + jboss4Connection.getBeans().size());



        ConnectionSettings jdk5 = new ConnectionSettings();
        jdk5.initializeConnectionType(new J2SE5ConnectionTypeDescriptor());
//        jdk5.setServerUrl("service:jmx:rmi://10.0.1.2/jndi/rmi://10.0.1.2:1099/jmxconnectorjcmd");
        EmsConnection jdk5Connection = factory.connect(jdk5);
        jdk5Connection.loadSynchronous(false);
    }


}
