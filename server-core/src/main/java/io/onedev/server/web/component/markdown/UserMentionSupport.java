package io.onedev.server.web.component.markdown;

import java.util.List;

import io.onedev.server.util.facade.UserFacade;

public interface UserMentionSupport {

	List<UserFacade> findUsers(String query, int count);

}
