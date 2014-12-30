package com.pmease.gitplex.web.component.pullrequest;

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
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.service.AvatarManager;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

public class ReviewerProvider extends ChoiceProvider<User> {

	private static final long serialVersionUID = 1L;

	private final IModel<PullRequest> requestModel;
	
	public ReviewerProvider(IModel<PullRequest> requestModel) {
		this.requestModel = requestModel;
	}
	
	@Override
	public void query(String term, int page, Response<User> response) {
		List<User> reviewers = requestModel.getObject().getPotentialReviewers();

		if (StringUtils.isNotBlank(term)) {
			for (Iterator<User> it = reviewers.iterator(); it.hasNext();) {
				User user = it.next();
				if (!user.getName().startsWith(term) && !user.getDisplayName().startsWith(term))
					it.remove();
			}
		}
		
		Collections.sort(reviewers, new Comparator<User>() {

			@Override
			public int compare(User user1, User user2) {
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
	public void toJson(User choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId())
			.key("name").value(StringEscapeUtils.escapeHtml4(choice.getName()));
		if (choice.getFullName() != null)
			writer.key("fullName").value(StringEscapeUtils.escapeHtml4(choice.getFullName()));
		writer.key("email").value(StringEscapeUtils.escapeHtml4(choice.getEmail()));
		writer.key("avatar").value(GitPlex.getInstance(AvatarManager.class).getAvatarUrl(choice));
	}

	@Override
	public Collection<User> toChoices(Collection<String> ids) {
		List<User> reviewers = Lists.newArrayList();
		Dao dao = GitPlex.getInstance(Dao.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			reviewers.add(dao.load(User.class, id));
		}

		return reviewers;
	}

	@Override
	public void detach() {
		requestModel.detach();
		
		super.detach();
	}

}