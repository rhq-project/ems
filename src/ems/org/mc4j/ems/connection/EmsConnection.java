/*
 * Copyright 2002-2011 Greg Hinkle
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
package org.mc4j.ems.connection;

import java.util.List;
import java.util.SortedSet;

import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.support.ConnectionProvider;

/**
 * TODO GH: Decide exception handling strategy (runtime?)
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 4, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public interface EmsConnection extends Refreshable {

    ConnectionTracker getTracker();

    void close();

    /**
     * Does a *:* load of all MBean names and caches them as EmsBeans.
     *
     * @param deep if true, also loads the MBeanInfo for each MBean and caches it in the corresponding EmsBean
     */
    void loadSynchronous(boolean deep);

    /**
     * Unloads all cached metadata associated with this connection - MBeanInfos, etc.
     *
     * @since 1.3
     */
    void unload();

    void addRegistrationListener(MBeanRegistrationListener registrationListener);

    void removeRegistrationListener(MBeanRegistrationListener registrationListener);

    /**
     * This will register a new MBean, but that may not be immediately recognized
     * @param className
     * @param objectName
     */
    void createMBean(String className, String objectName) throws EmsException;

    void removeMBean(String objectName) throws EmsException;

    SortedSet<EmsBean> getBeans();

    EmsBean getBean(String objectName);

    List<EmsBean> queryBeans(String objectName);

    EmsBean registerBean(String className, String objectName);

    EmsBean registerBean(String className, String objectName, Object[] params, String[] signature);

    /**
     *
     * @param objectName
     * @return
     * @throws EmsMalformedObjectNameException when an invalid object name is provided
     */
    Object buildObjectName(String objectName)  throws EmsMalformedObjectNameException ;

    /**
     *
     * @return
     * @since 1.0.5
     */
    long getRoundTrips();

    /**
     *
     * @return
     * @since 1.0.5
     */
    long getFailures();

    /**
     *
     * @since 1.0.6
     * @return
     */
    public ConnectionProvider getConnectionProvider();
}
