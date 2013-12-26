package org.mc4j.ems.test;

import org.mc4j.ems.connection.settings.ConnectionSettings;
import org.mc4j.ems.connection.support.ConnectionProvider;
import org.mc4j.ems.connection.support.classloader.ClassLoaderFactory;
import org.mc4j.ems.connection.support.metadata.LocalVMTypeDescriptor;

/**
 * Created by IntelliJ IDEA.
 * User: ghinkle
 * Date: Sep 21, 2005
 * Time: 11:04:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoaderTest {


    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ConnectionSettings connectionSettings = new ConnectionSettings();
        connectionSettings.setConnectionType(new LocalVMTypeDescriptor()); //new J2SE5ConnectionTypeDescriptor());
        String className = connectionSettings.getConnectionType().getConnectionNodeClassName();

        // TODO GH: Does this need to be configurable per connection?
        ClassLoader loader = ClassLoaderFactory.getInstance().buildClassLoader(connectionSettings);

        // TODO GH: Add intelligent classloader layer here that can either work
        // directly against current classloader or build a non-delegating child
        // to override with connection specific classes
        Class clazz = Class.forName(className, true, loader);
        ConnectionProvider connectionProvider =
            (ConnectionProvider) clazz.newInstance();
        System.out.println("ConnectionProvider: " + connectionProvider);
    }

}
