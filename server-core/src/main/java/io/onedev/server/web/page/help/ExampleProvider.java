package io.onedev.server.web.page.help;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

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
	public Serializable getExample() {
		if (api != null) {
			if (api.example().length() != 0) {
				return api.example();
			} else if (api.exampleProvider().length() != 0) {
				return (Serializable) Preconditions.checkNotNull(ReflectionUtils.invokeStaticMethod(
						apiDeclaringClass, api.exampleProvider()));
			} 
		} 
		return null;
	}
}