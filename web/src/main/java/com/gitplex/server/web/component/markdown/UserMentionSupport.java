package com.gitplex.server.web.component.markdown;

import java.util.List;

import com.gitplex.server.model.Account;

public interface UserMentionSupport {

	List<Account> findUsers(String query, int count);

}
