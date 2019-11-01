package io.onedev.server.web.component.markdown;

import java.util.List;

import io.onedev.server.model.User;

public interface UserMentionSupport {

	List<User> findUsers(String query, int count);

}
