package io.onedev.server.web.page.help;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import io.onedev.server.rest.annotation.Api;
import io.onedev.server.util.ReflectionUtils;

class ExampleProvider {
	
	private final Class<?> apiDeclaringClass;
	
	private final Api api;
	
	public ExampleProvider(Class<?> apiDeclaringClass, @Nullable Api api) {
		this.apiDeclaringClass = apiDeclaringClass;
		this.api = api;
	}
	
	@Nullable
	public Object getExample() {
		if (api != null && api.exampleProvider().length() != 0) { 
			return Preconditions.checkNotNull(ReflectionUtils.invokeStaticMethod(
					apiDeclaringClass, api.exampleProvider()));
		} else {
			return null;
		}
	}
}