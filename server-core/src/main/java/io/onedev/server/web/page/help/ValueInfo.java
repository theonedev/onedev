package io.onedev.server.web.page.help;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class ValueInfo {
	
	public enum Origin {REQUEST_BODY, RESPONSE_BODY, PATH_PLACEHOLDER, QUERY_PARAM};
	
	private final Origin origin;
	
	private final Type declaredType;
	
	private final Field field;
	
	public ValueInfo(Origin origin, Type declaredType, @Nullable Field field) {
		this.origin = origin;
		this.declaredType = declaredType;
		this.field = field;
	}

	public ValueInfo(Origin origin, Type declaredType) {
		this(origin, declaredType, null);
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

}