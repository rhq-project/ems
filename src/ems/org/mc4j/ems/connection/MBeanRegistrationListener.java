package org.mc4j.ems.connection;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Apr 4, 2005
 * @version $Revision: 629 $($Author: ianpspringer $ / $Date: 2011-10-28 23:44:26 +0200 (Fr, 28 Okt 2011) $)
 */
public interface MBeanRegistrationListener {

    void registrationChanged(MBeanRegistrationEvent event);
}
