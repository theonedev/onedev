package com.gitplex.server.web.component.markdown;

import java.util.List;

import com.gitplex.server.model.User;

public interface UserMentionSupport {

	List<User> findUsers(String query, int count);

}
