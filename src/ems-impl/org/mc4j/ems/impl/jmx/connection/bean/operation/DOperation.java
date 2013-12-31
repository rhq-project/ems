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

package org.mc4j.ems.impl.jmx.connection.bean.operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import org.mc4j.ems.connection.EmsInvocationException;
import org.mc4j.ems.connection.bean.operation.EmsOperation;
import org.mc4j.ems.connection.bean.parameter.EmsParameter;
import org.mc4j.ems.impl.jmx.connection.bean.DMBean;
import org.mc4j.ems.impl.jmx.connection.bean.parameter.DParameter;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 4, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public class DOperation implements EmsOperation {

    protected MBeanOperationInfo info;
    protected DMBean bean;
    public static final int MAX_EXECUTION_TIME = 10000;

    protected List<EmsParameter> parameters;// = new ArrayList<EmsParameter>();
    protected Impact impact;

    public DOperation(MBeanOperationInfo info, DMBean bean) {
        this.info = info;
        this.bean = bean;

        MBeanParameterInfo[] params = info.getSignature();
        if (params.length>0) {
            parameters=new ArrayList<EmsParameter>(params.length);
        }
        for (MBeanParameterInfo param : params) {
            parameters.add(new DParameter(param));
        }

        switch (info.getImpact()) {
            case MBeanOperationInfo.ACTION:
                impact = Impact.ACTION;
                break;
            case MBeanOperationInfo.INFO:
                impact = Impact.INFO;
                break;
            case MBeanOperationInfo.ACTION_INFO:
                impact = Impact.ACTION_INFO;
                break;
            default:
                impact = Impact.UNKNOWN;
        }
    }

    public String getName() {
        return info.getName();
    }

    public String getDescription() {
        return info.getDescription();
    }

    public List<EmsParameter> getParameters() {
        if (parameters==null) {
            return Collections.emptyList();
        }
        return parameters;
    }

    public Impact getImpact() {
        return impact;
    }

    public String getReturnType() {
        return info.getReturnType();
    }

    @SuppressWarnings({"EmptyCatchBlock"})
    public Object invoke(Object... parameters) throws EmsInvocationException {


        MBeanParameterInfo[] parameterInfos = info.getSignature();

        final Object[] parameterValues = new Object[parameterInfos.length];
        final String[] parameterTypes = new String[parameterInfos.length];


        int i = 0;
        for (Object param : parameters) {
            String name = parameterInfos[i].getName();
            parameterValues[i] = param;
            parameterTypes[i] = parameterInfos[i].getType();
            i++;
        }

        // TODO GH: get rid of the asynchronous bits and the timeouts for now (not good for RHQ JMX plugin)
        // Add a way to let the caller decide if they want async
        try {
            Object results =
                    bean.getConnectionProvider().getMBeanServer().invoke(
                            bean.getObjectName(),
                            getName(),
                            parameterValues,
                            parameterTypes);

            return results;
        } catch (ReflectionException re) {
            Exception cause = re.getTargetException();
            if (cause != null) {
                throw new EmsInvocationException("Exception on invocation of [" + getName() + "]" + cause.toString(), cause);
            } else {
                throw new EmsInvocationException("Exception on invocation of [" + getName() + "]" + re.toString(), re);
            }
        } catch (Exception e) {
            throw new EmsInvocationException("Exception on invocation of [" + getName() + "]" + e.toString(), e);
        }

        /*

        class Future {
            boolean done = false;
            Object results;
            Exception e;
        }

        final Future f = new Future();

        Runnable execution = new Runnable() {
            public void run() {
                try {
                    f.results =
                            bean.getConnectionProvider().getMBeanServer().invoke(
                                    bean.getObjectName(),
                                    getName(),
                                    parameterValues,
                                    parameterTypes);

                } catch (ReflectionException re) {
                    Exception cause = re.getTargetException();
                    if (cause != null) {
                        f.e = cause;
                    } else {
                        f.e = re;
                    }
                } catch (Exception e) {
                    f.e = e;
                } finally {
                    f.done = true;
                }
            }
        };

        // Need to run this outside the AWT update thread
        // so as not to freeze the UI in case of failed connection.
        Thread t = new Thread(execution, "MC4J Operation Execution [" + getName() + "]");
        t.start();

        // TODO: The timeout is nice, but providing a method that actually returns a future would be nice
        try {
            t.join(MAX_EXECUTION_TIME);
        } catch (InterruptedException e) { }
        if (!f.done) {
            // TODO: Write exceptions
            throw new EmsInvocationException("Operation timed out.");
        }

        if (f.e != null) {
            throw new EmsInvocationException("Exception on invocation of [" + getName() + "]" + f.e.toString(),f.e);
        } else {
            return f.results;
        }*/
    }

    public int compareTo(Object o) {
        DOperation otherOperation = (DOperation) o;
        int i = this.getName().compareTo(
                otherOperation.getName());
        if (i == 0) {

            i = ((Integer)parameters.size()).compareTo(otherOperation.getParameters().size());
            if (i == 0) {
                if (parameters==null) {
                    i = (otherOperation.parameters==null ? 0 : 1);
                }
                else {
                    for (int j = 0; j < parameters.size();j++) {
                        i = parameters.get(j).compareTo(otherOperation.getParameters().get(j));
                        if (i != 0) {
                            break;
                        }
                    }
                }
            }
        }
        return i;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DOperation)) return false;

        DOperation that = (DOperation) o;

        if (info != null ? !info.getName().equals(that.info.getName()) : that.info.getName() != null) return false;
        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (info != null ? info.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }
}
