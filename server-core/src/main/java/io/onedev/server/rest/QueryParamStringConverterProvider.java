package io.onedev.server.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

@Provider
public class QueryParamStringConverterProvider implements ParamConverterProvider {

	@SuppressWarnings("unchecked")
	@Override
	public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
		if (rawType != String.class)
			return null;
		for (Annotation annotation : annotations) {
			if (annotation instanceof QueryParam) {
				return (ParamConverter<T>) new ParamConverter<String>() {
					@Override
					public String fromString(String value) {
						return StringUtils.trimToNull(value);
					}

					@Override
					public String toString(String value) {
						return value;
					}
				};
			}
		}
		return null;
	}

}
