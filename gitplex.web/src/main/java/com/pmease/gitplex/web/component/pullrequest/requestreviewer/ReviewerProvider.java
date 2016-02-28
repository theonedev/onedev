package com.pmease.gitplex.web.component.pullrequest.requestreviewer;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.model.IModel;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.avatar.AvatarManager;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

public class ReviewerProvider extends ChoiceProvider<Account> {

	private static final long serialVersionUID = 1L;

	private final IModel<PullRequest> requestModel;
	
	public ReviewerProvider(IModel<PullRequest> requestModel) {
		this.requestModel = requestModel;
	}
	
	@Override
	public void query(String term, int page, Response<Account> response) {
		List<Account> reviewers = requestModel.getObject().getPotentialReviewers();

		if (StringUtils.isNotBlank(term)) {
			for (Iterator<Account> it = reviewers.iterator(); it.hasNext();) {
				Account user = it.next();
				if (!user.getName().startsWith(term) && !user.getDisplayName().startsWith(term))
					it.remove();
			}
		}
		
		Collections.sort(reviewers, new Comparator<Account>() {

			@Override
			public int compare(Account user1, Account user2) {
				return user1.getDisplayName().compareTo(user2.getDisplayName());
			}
			
		});

		int first = page * Constants.DEFAULT_SELECT2_PAGE_SIZE;
		int last = first + Constants.DEFAULT_SELECT2_PAGE_SIZE;
		if (last > reviewers.size()) {
			response.addAll(reviewers.subList(first, reviewers.size()));
		} else {
			response.addAll(reviewers.subList(first, last));
			response.setHasMore(last < reviewers.size());
		}
	}

	@Override
	public void toJson(Account choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId())
			.key("name").value(StringEscapeUtils.escapeHtml4(choice.getName()));
		if (choice.getFullName() != null)
			writer.key("fullName").value(StringEscapeUtils.escapeHtml4(choice.getFullName()));
		writer.key("email").value(StringEscapeUtils.escapeHtml4(choice.getEmail()));
		String avatarUrl = GitPlex.getInstance(AvatarManager.class).getAvatarUrl(choice);
		writer.key("avatar").value(avatarUrl);
	}

	@Override
	public Collection<Account> toChoices(Collection<String> ids) {
		List<Account> reviewers = Lists.newArrayList();
		Dao dao = GitPlex.getInstance(Dao.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			reviewers.add(dao.load(Account.class, id));
		}

		return reviewers;
	}

	@Override
	public void detach() {
		requestModel.detach();
		
		super.detach();
	}

}