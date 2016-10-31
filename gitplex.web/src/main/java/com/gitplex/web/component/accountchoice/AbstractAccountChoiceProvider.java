package com.gitplex.web.component.accountchoice;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONWriter;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.Account;
import com.gitplex.core.manager.AccountManager;
import com.gitplex.web.avatar.AvatarManager;
import com.google.common.collect.Lists;
import com.gitplex.commons.wicket.component.select2.ChoiceProvider;

public abstract class AbstractAccountChoiceProvider extends ChoiceProvider<Account> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void toJson(Account choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(StringEscapeUtils.escapeHtml4(choice.getDisplayName()));
		String avatarUrl = GitPlex.getInstance(AvatarManager.class).getAvatarUrl(choice);
		writer.key("avatar").value(avatarUrl);
	}

	@Override
	public Collection<Account> toChoices(Collection<String> ids) {
		List<Account> accounts = Lists.newArrayList();
		AccountManager accountManager = GitPlex.getInstance(AccountManager.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			accounts.add(accountManager.load(id));
		}

		return accounts;
	}

}