package io.onedev.server.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import org.jspecify.annotations.Nullable;

@Provider
@Priority(100)
public class EnumParamConverterProvider implements ParamConverterProvider {

	@SuppressWarnings("unchecked")
	@Override
	public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
		if (!rawType.isEnum())
			return null;

		var paramInfo = getParamInfo(annotations);
		var enumClass = rawType.asSubclass(Enum.class);
		var validValues = Arrays.stream(enumClass.getEnumConstants())
				.map(Enum::name)
				.collect(Collectors.joining(", "));

		return new ParamConverter<>() {
			@Override
			public T fromString(String value) {
				if (value == null)
					return null;
				try {
					return (T) Enum.valueOf(enumClass, value.toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new BadRequestException(buildErrorMessage(paramInfo, value, validValues));
				}
			}

			@Override
			public String toString(T value) {
				return value == null ? null : ((Enum<?>) value).name();
			}
		};
	}

	private static String buildErrorMessage(@Nullable ParamInfo paramInfo, String value, String validValues) {
		if (paramInfo != null)
			return "Invalid value '" + value + "' for " + paramInfo.type + " '" + paramInfo.name 
					+ "'. Valid values are: " + validValues;
		return "Invalid enum value '" + value + "'. Valid values are: " + validValues;
	}

	@Nullable
	private static ParamInfo getParamInfo(Annotation[] annotations) {
		for (var annotation : annotations) {
			if (annotation instanceof QueryParam queryParam)
				return new ParamInfo("query parameter", queryParam.value());
		}
		return null;
	}

	private static class ParamInfo {
		
		private final String type;
		
		private final String name;
		
		private ParamInfo(String type, String name) {
			this.type = type;
			this.name = name;
		}
		
	}

}
