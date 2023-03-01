package io.onedev.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Map;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RefreshToken {

	String value();
	
	interface Callback {
		
		String getAuthorizeEndpoint();
		
		Map<String, String> getAuthorizeParams();
		
		String getClientId();
		
		String getClientSecret();
		
		String getTokenEndpoint();
		
		Collection<String> getScopes();
		
	}
}
