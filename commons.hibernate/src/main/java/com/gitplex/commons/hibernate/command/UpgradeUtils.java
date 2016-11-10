package com.gitplex.commons.hibernate.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class UpgradeUtils {

	public static String copyRunAs(String from, String to) {
		Matcher matcher = Pattern.compile("(?m)^(#|\\s)*RUN_AS_USER\\s*=.*$").matcher(from);
		if (matcher.find()) {
			String runAs = matcher.group();
			to = StringUtils.replace(to, "#RUN_AS_USER=", runAs);
		}
		return to;
	}
	
}
