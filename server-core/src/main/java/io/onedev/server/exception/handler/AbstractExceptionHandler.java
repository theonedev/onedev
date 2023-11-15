package io.onedev.server.exception.handler;

import io.onedev.server.util.ReflectionUtils;

import java.util.List;

public abstract class AbstractExceptionHandler<T extends Throwable> implements ExceptionHandler<T> {

	private static final long serialVersionUID = 1L;
	
	private final Class<T> exceptionClass;

	@SuppressWarnings("unchecked")
	public AbstractExceptionHandler() {
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(AbstractExceptionHandler.class, getClass());
		if (typeArguments.size() == 1 && Throwable.class.isAssignableFrom(typeArguments.get(0))) {
			exceptionClass = (Class<T>) typeArguments.get(0);
		} else {
			throw new RuntimeException("Super class of exception handler implementation must "
					+ "be AbstractExceptionHandler and must realize the type argument <T>");
		}
	}

	public Class<T> getExceptionClass() {
		return exceptionClass;
	}
	
}
