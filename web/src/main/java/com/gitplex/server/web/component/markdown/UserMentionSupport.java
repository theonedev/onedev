package com.gitplex.server.web.component.markdown;

import java.util.List;

import com.gitplex.server.util.facade.UserFacade;

public interface UserMentionSupport {

	List<UserFacade> findUsers(String query, int count);

}
