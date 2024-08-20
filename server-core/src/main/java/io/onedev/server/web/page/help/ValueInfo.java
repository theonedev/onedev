package io.onedev.server.web.page.help;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

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