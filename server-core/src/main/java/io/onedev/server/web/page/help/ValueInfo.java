package io.onedev.server.web.page.help;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import javax.annotation.Nullable;

class ValueInfo {
	
	public enum Origin {REQUEST_BODY, RESPONSE_BODY, PATH_PLACEHOLDER, QUERY_PARAM};
	
	private final Origin origin;
	
	private final Type declaredType;
	
	private final Field field;
	
	private final boolean createOnly;
	
	public ValueInfo(Origin origin, Type declaredType, @Nullable Field field, boolean createOnly) {
		this.origin = origin;
		this.declaredType = declaredType;
		this.field = field;
		this.createOnly = createOnly;
	}

	public ValueInfo(Origin origin, Type declaredType) {
		this(origin, declaredType, null, false);
	}
	
	public Type getDeclaredType() {
		return declaredType;
	}

	public Origin getOrigin() {
		return origin;
	}

	@Nullable
	public Field getField() {
		return field;
	}

	public boolean isCreateOnly() {
		return createOnly;
	}
}