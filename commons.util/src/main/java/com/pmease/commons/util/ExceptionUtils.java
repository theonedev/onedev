package com.pmease.commons.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.pmease.commons.bootstrap.BootstrapUtils;

public class ExceptionUtils extends org.apache.commons.lang3.exception.ExceptionUtils {
	
	@SuppressWarnings("unchecked")
	public static <T extends Throwable> T find(Throwable throwable, Class<T> exceptionClass) {
        if (exceptionClass.isAssignableFrom(throwable.getClass()))
            return (T) throwable;
        Set<Throwable> examinedCauses = new HashSet<Throwable>(); 
        examinedCauses.add(throwable);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            if (exceptionClass.isAssignableFrom(cause.getClass()))
                return (T) cause;
            examinedCauses.add(cause);
            
            // cause.getCause() may return a previously examined cause
            if (examinedCauses.contains(cause.getCause()))
            	break;
            cause = cause.getCause();
        }
        return null;
    }

	public static RuntimeException unchecked(Exception e) {
		return BootstrapUtils.unchecked(e);
	}
	
	public static String buildMessage(String cause, Object...factors) {
		return buildMessage(cause, EasyMap.ofOrdered(factors));
	}
	
	public static String buildMessage(String cause, Map<String, String> factors) {
		String message = cause;
		message += " (";
		
		List<String> factorMessages = new ArrayList<String>();
		for (Iterator<Map.Entry<String, String>> it = factors.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = (Entry<String, String>) it.next();
			factorMessages.add(entry.getKey().toString() + ":" + entry.getValue().toString());
		}
		
		message += StringUtils.join(factorMessages.iterator(), ", ");
		message += ")";
		
		return message;		
	}

}
