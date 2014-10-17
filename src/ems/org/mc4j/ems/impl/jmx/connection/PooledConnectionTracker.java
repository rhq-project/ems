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

package org.mc4j.ems.impl.jmx.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mc4j.ems.connection.ConnectionTracker;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.attribute.EmsAttribute;
import org.mc4j.ems.impl.jmx.connection.bean.DMBean;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 12, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class PooledConnectionTracker implements ConnectionTracker {

    private static Log log = LogFactory.getLog(PooledConnectionTracker.class);

    protected DConnection connection;

    protected List<RefreshItem> refreshItems = new ArrayList<RefreshItem>();

    protected ScheduledThreadPoolExecutor executor;

    private static int POOL_SIZE = 2;

    public PooledConnectionTracker(DConnection connection) {
        this.connection = connection;

        initTracker();
    }

    protected void initTracker() {
        // TODO GH: Build registration system
        RefreshItem connectionRefresh = new ConnectionRefresh(20000, connection);
        refreshItems.add(connectionRefresh);

        RefreshItem attributeRefresh = new MBeanRefresh(20000);
        refreshItems.add(attributeRefresh);


        // Give names to the threads with a custom thread factory
        executor = new ScheduledThreadPoolExecutor(POOL_SIZE, new ThreadFactory() {
            private AtomicInteger index = new AtomicInteger(1);
            public Thread newThread(Runnable r) {
                return new Thread(r, "EMS-ConnectionTracker-"+index.getAndIncrement());
            }
        });

        executor.scheduleAtFixedRate(connectionRefresh,1000,connectionRefresh.getUpdateDelay(), TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(attributeRefresh,1500,attributeRefresh.getUpdateDelay(), TimeUnit.MILLISECONDS);

    }

    public void stopTracker() {
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.shutdown();
    }

    public void registerUpdateRequest(String objectName, long delay) {
        throw new UnsupportedOperationException();
    }
    public void removeUpdateRequest(String objectName) {
        throw new UnsupportedOperationException();
    }

    private interface RefreshItem extends Runnable {
        long getUpdateDelay();
    }

    private abstract class AbstractConnectionRefresh implements RefreshItem{
        long updateDelay;
        public AbstractConnectionRefresh(long updateDelay) {
            this.updateDelay = updateDelay;
        }
        public long getUpdateDelay() {
            return updateDelay;
        }
    }

    private class ConnectionRefresh extends AbstractConnectionRefresh{
        private DConnection connection;

        public ConnectionRefresh(long updateDelay, DConnection connection) {
            super(updateDelay);
            this.connection = connection;
        }

        public void run() {
            connection.loadSynchronous(false);
        }
    }

    private class MBeanRefresh extends AbstractConnectionRefresh {
        private DMBean mbean;
        Set<String> attributes;

        public MBeanRefresh(long updateDelay) {
            super(updateDelay);
        }

        public MBeanRefresh(long updateDelay, DMBean mbean) {
            super(updateDelay);
            this.mbean = mbean;
        }

        public MBeanRefresh(long updateDelay, DMBean mbean, Set<String> attributes) {
            super(updateDelay);
            this.mbean = mbean;
            this.attributes = attributes;
        }
        boolean first = true;
        public void run() {
            if (mbean == null) {
                System.gc();
                long beforeMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
//                System.out.println("BeforeMem: " + );
//                Map m = new TreeMap(new Comparator() {
//                    public int compare(Object o, Object o1) {
//                        return ((Comparable)o1).compareTo(o);
//                    }
//                });
                long total = 0; long count = 0;
                for (EmsBean bean : connection.getBeans()) {
                    bean.refreshAttributes();
                    //if (first) {
                        //System.out.println(bean.getObjectName().getCanonicalName());
                        for (EmsAttribute attribute : bean.getAttributes()) {
                            int size = attribute.getValueSize();
                            total += size;
                            if (size > 0) count++;
                            //m.put(new Integer(size),/*bean.getObjectName() + "::" +*/attribute.getName());
                            //System.out.println("\t"+attribute.getName() + " - " + attribute.getValueSize());
                            if (attribute.getName().equalsIgnoreCase("stats")) {
//                                log.debug("Stats size: " + com.vladium.utils.ObjectProfiler.sizeof(attribute.getValue()));
                            }
                        }
                   // }
                }
                log.debug("Total size of " + count + " attributes: " + total);
                // Expensive
//                log.debug("Total connection size: " + com.vladium.utils.ObjectProfiler.sizeof(connection));
                /*Iterator<Map.Entry> iter = m.entrySet().iterator();
//                for (int i = 0;i<50;i++) {
                for (;iter.hasNext();) {
                    Map.Entry entry = iter.next();
                    log.debug(entry.getKey() + " " + entry.getValue());
                }

                first = false;*/
                System.gc();

                long afterMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());

                log.debug("Memory difference for update: " + (afterMem - beforeMem));

//                log.debug("AfterMem: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
            } else if (attributes == null) {
                // reload all attributes
                mbean.refreshAttributes();
            } else {
                // TODO GH: Implement mbean.refreshAttributes(set attributes)
                for (String attributeName :attributes) {
                    mbean.getAttribute(attributeName).refresh();
                }
            }
        }
    }
}
