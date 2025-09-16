package io.onedev.server.util.usermatch;

import java.io.Serializable;

import io.onedev.server.annotation.Editable;
import io.onedev.server.model.User;

@Editable
public interface UserMatchCriteria extends Serializable {
	
	boolean matches(User user);
	
}
