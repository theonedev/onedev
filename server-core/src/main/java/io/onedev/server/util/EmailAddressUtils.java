package io.onedev.server.util;

import org.apache.commons.lang3.StringUtils;

import javax.mail.internet.InternetAddress;

public class EmailAddressUtils {
	
	public static String describe(InternetAddress internetAddress, boolean discloseEmailAddress) {
		var personal = internetAddress.getPersonal();
		var emailAddress = internetAddress.getAddress();
		if (discloseEmailAddress) {
			if (personal != null && !personal.equalsIgnoreCase(emailAddress))
				return personal + " <" + emailAddress + ">";
			else
				return emailAddress;
		} else {
			if (personal != null && !personal.contains("@")) 
				return personal;
			else 
				return StringUtils.substringBefore(emailAddress, "@");
		}
	}
	
}
