package com.pmease.commons.jersey;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

public class JerseyUtils {
	
	public static void checkQueryParams(UriInfo uriInfo, String...validParams) {
		Set<String> paramNames = new HashSet<String>(uriInfo.getQueryParameters().keySet());
		for (String param: validParams)
			paramNames.remove(param);
		if (!paramNames.isEmpty()) 
			throw new IllegalArgumentException("Unexpected query parameters: " + paramNames);
	}
	
}
