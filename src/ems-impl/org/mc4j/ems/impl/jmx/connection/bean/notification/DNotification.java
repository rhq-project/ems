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

package org.mc4j.ems.impl.jmx.connection.bean.notification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

import org.mc4j.ems.connection.EmsBeanNotFoundException;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.notification.EmsNotification;
import org.mc4j.ems.connection.bean.notification.EmsNotificationEvent;
import org.mc4j.ems.connection.bean.notification.EmsNotificationListener;
import org.mc4j.ems.impl.jmx.connection.bean.DBeanName;
import org.mc4j.ems.impl.jmx.connection.bean.DMBean;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 4, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class DNotification implements EmsNotification {

    protected MBeanNotificationInfo info;
    protected DMBean bean;

    protected List<String> notifTypes;

    protected List<EmsNotificationEvent> events = new ArrayList<EmsNotificationEvent>();

    protected Set<EmsNotificationListener> listeners = new HashSet<EmsNotificationListener>();

    protected NotificationListenerImpl notificationListener;
    protected NotificationFilterImpl notificationFilter;



    public DNotification(MBeanNotificationInfo info, DMBean bean) {
        this.info = info;
        this.bean = bean;
        notifTypes = Arrays.asList(getTypes());
    }

    public String getName() {
        return info.getName();
    }

    public String getDescription() {
        return info.getDescription();
    }

    public String[] getTypes() {
        return info.getNotifTypes();
    }

    public int compareTo(Object o) {
        DNotification otherNotification = (DNotification) o;
        return this.info.getName().compareTo(
            otherNotification.getName());
    }

    public void addNotificationListener(EmsNotificationListener listener) {
        listeners.add(listener);
    }

    public boolean removeNotificationListener(EmsNotificationListener listener) {
        return listeners.remove(listener);
    }

    public boolean isListening() {
        return notificationListener != null;
    }

    public void startListening() {

        if (isListening()) {
            // Already listening
            return;
        }
        try {
            notificationListener = new NotificationListenerImpl(this);
            notificationFilter = new NotificationFilterImpl(notifTypes);
            bean.getConnectionProvider().getMBeanServer().
                    addNotificationListener(
                            ((DBeanName)bean.getBeanName()).getObjectName(),
                            notificationListener,
                            null,
                            null);
        } catch (InstanceNotFoundException e) {
            throw new EmsBeanNotFoundException("Could not register notification listener", e);
        }

    }

    public void stopListening() {
        if (!isListening()) {
            // Not current listening
            return;
        }

        try {
            bean.getConnectionProvider().getMBeanServer().
                    removeNotificationListener(
                            ((DBeanName)bean.getBeanName()).getObjectName(),
                            notificationListener,
                            null,
                            null);
            notificationListener = null;
            notificationFilter = null;
        } catch (InstanceNotFoundException e) {
            throw new EmsBeanNotFoundException("Could not deregister notification listener, bean missing", e);
        } catch (ListenerNotFoundException e) {
            // That's ok
        }
    }

    protected EmsBean getBean() {
        return bean;
    }

    public List<EmsNotificationEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    protected void fireNotifications(EmsNotificationEvent event) {
        events.add(event);

        for (EmsNotificationListener listener : listeners) {
            listener.handleNotification(event);
        }

    }


    private static class NotificationListenerImpl implements NotificationListener, Serializable {
        DNotification n;

        public NotificationListenerImpl(DNotification n) {
            this.n = n;
        }

        public void handleNotification(Notification notification, Object object) {
            // This will do client side filter
            if (Arrays.asList(n.getTypes()).contains(notification.getType())) {

                EmsNotificationEvent event =
                        new EmsNotificationEvent(
                                n.getBean(),
                                notification.getMessage(),
                                notification.getSequenceNumber(),
                                notification.getType(),
                                notification.getTimeStamp(),
                                notification.getUserData(),
                                notification.getSource());
                n.fireNotifications(event);
            }
        }
    }


   /*  private List<NotificationFilter> getNotificationFilters()
    {
        List<NotificationFilter> filters = new ArrayList<NotificationFilter>();
        boolean uncrecognisedNotifiationFound 	= false;
        try
        {
            Vector raw 							= new Vector(10);
            MBeanNotificationInfo [] notifyInfo 	= (MBeanNotificationInfo[]) server.getMBeanInfo(objectName).getNotifications();
            for(int i = 0; i < notifyInfo.length && !uncrecognisedNotifiationFound; i++)
            {
                if(notifyInfo[i].getName().equals(getName())) //fetch notification specific to this node...
                {
                    String [] notifyTypes 				= notifyInfo[i].getNotifTypes();

                    NotificationFilter filter 			= null;
                    for(int j = 0; j < notifyTypes.length && !uncrecognisedNotifiationFound; j++)
                    {
                        //TODO in the future add more filter support here, if nessesary....
                        if(notifyTypes[j].equals(AttributeChangeNotification.ATTRIBUTE_CHANGE))
                        {
                            filter = new AttributeChangeNotificationFilter();
                            ((AttributeChangeNotificationFilter) filter).enableAttribute(getName());
                        }
                        else if(notifyTypes[j].equals(MBeanServerNotification.REGISTRATION_NOTIFICATION))
                        {
                            filter = new MBeanServerNotificationFilter();
                            ((MBeanServerNotificationFilter) filter).disableAllTypes();
                            ((MBeanServerNotificationFilter) filter).enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
                            ((MBeanServerNotificationFilter) filter).enableAllObjectNames();
                        }
                        else if(notifyTypes[j].equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION))
                        {
                            filter = new MBeanServerNotificationFilter();
                            ((MBeanServerNotificationFilter) filter).disableAllTypes();
                            ((MBeanServerNotificationFilter) filter).enableType(MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
                            ((MBeanServerNotificationFilter) filter).enableAllObjectNames();
                        }
                        else //unrecognised notification type, set filter to null....
                        {
                            filter = null;
                            uncrecognisedNotifiationFound = true; //break the loop...
                            //once we have a null filter there is no point of
                            //having any other filters as all of the notifications will have to be
                            //coming through (in other words we don't know how to filer, so dont' filter at all).
                            raw.removeAllElements();
                        }
                        raw.add(filter);
                    }
                }
            }
            if(raw.size() > 0) //do we have anything other than a default result?
            {
                result = new NotificationFilter[raw.size()];
                result = (NotificationFilter[]) raw.toArray(result);
            }
        }
        catch(ReflectionException e)
        {
            //DO NOTHING object is not a Notification Broadcaster...
        }
        catch (javax.management.IntrospectionException e)
        {
            //DO NOTHING object is not a Notification Broadcaster...
        }
        catch(InstanceNotFoundException e)
        {
            ErrorManager.getDefault().notify(e);
        }

        return result;
    }
*/
    
    private static class NotificationFilterImpl implements NotificationFilter {
        List<String> notifTypes;

        public NotificationFilterImpl(List<String> notifTypes) {
            this.notifTypes = notifTypes;
        }

        public boolean isNotificationEnabled(Notification notification) {
            return (notifTypes.contains(notification.getType()));
        }
    }

}
