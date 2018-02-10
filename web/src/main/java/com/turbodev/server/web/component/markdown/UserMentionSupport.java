package com.turbodev.server.web.component.markdown;

import java.util.List;

import com.turbodev.server.util.facade.UserFacade;

public interface UserMentionSupport {

	List<UserFacade> findUsers(String query, int count);

}
