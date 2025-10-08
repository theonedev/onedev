package io.onedev.server.web.page.help;

import java.lang.reflect.Type;

import org.jspecify.annotations.Nullable;

public class ValueInfo {
	
	public enum Origin {CREATE_BODY, UPDATE_BODY, READ_BODY, PATH_PLACEHOLDER, QUERY_PARAM};
	
	private final Origin origin;
	
	private final Type declaredType;
	
	private final JsonMember member;
	
	public ValueInfo(Origin origin, Type declaredType, @Nullable JsonMember member) {
		this.origin = origin;
		this.declaredType = declaredType;
		this.member = member;
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
	public JsonMember getMember() {
		return member;
	}

}