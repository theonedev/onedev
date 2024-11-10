package io.onedev.server.util;

import javax.mail.internet.InternetAddress;

public class EmailAddressUtils {
	
	public static String describe(InternetAddress internetAddress) {
		if (internetAddress.getPersonal() != null && !internetAddress.getPersonal().equalsIgnoreCase(internetAddress.getAddress()))
			return internetAddress.getAddress() + " (" + internetAddress.getPersonal() + ")";
		else
			return internetAddress.getAddress();
	}
	
}
