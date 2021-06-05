package io.onedev.server.web.page.project.imports;

import java.io.Serializable;

import com.github.scribejava.core.oauth.OAuth20Service;

public interface OAuthServiceAwareCallable<T> extends Serializable {
	
	T call(OAuth20Service service);
	
}