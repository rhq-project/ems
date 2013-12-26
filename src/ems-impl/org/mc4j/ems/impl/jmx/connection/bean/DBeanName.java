package org.mc4j.ems.impl.jmx.connection.bean;

import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.mc4j.ems.connection.EmsMalformedObjectNameException;
import org.mc4j.ems.connection.bean.EmsBeanName;

/**
 * Created by IntelliJ IDEA.
 * User: ghinkle
 * Date: Oct 25, 2005
 * Time: 1:18:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class DBeanName implements EmsBeanName {

    private ObjectName objectName;

    public DBeanName(ObjectName objectName) {
        this.objectName = objectName;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public String getDomain() {
        return objectName.getDomain();
    }

    public String getCanonicalName() {
        return objectName.getCanonicalName();
    }

    public Map<String,String> getKeyProperties() {
        // TODO: Build an ordered map out of the properties?

        return (Map<String, String>) objectName.getKeyPropertyList();
    }

    public String getKeyProperty(String name) {
        return objectName.getKeyProperty(name);
    }

    
    public boolean apply(String objectNameFilterString) {
        try {
            return this.objectName.apply(new ObjectName(objectNameFilterString));
        } catch (MalformedObjectNameException e) {
            throw new EmsMalformedObjectNameException("Invalid object name filter [" + objectNameFilterString + "]",e);
        }
    }

    /**
     * <p>Returns a string representation of this name.</p>
     *
     * @return a string representation of this name
     */
    public String toString() {
        return this.objectName.toString();
    }

    public int compareTo(Object o) {
        if (this == o) return 0;

        return toString().compareTo(((EmsBeanName) o).toString());
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DBeanName dBeanName = (DBeanName) o;

        return objectName.equals(dBeanName.objectName);

    }

    public int hashCode() {
        return objectName.hashCode();
    }
}
