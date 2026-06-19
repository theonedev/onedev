package io.onedev.server.web.component.pullrequest.choice;

import java.util.Collection;
import java.util.List;

import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.jspecify.annotations.Nullable;
import org.unbescape.html.HtmlEscape;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.pullrequest.FuzzyCriteria;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.util.ProjectScopedQuery;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public abstract class PullRequestChoiceProvider extends ChoiceProvider<PullRequest> {

	private static final long serialVersionUID = 1L;
	
	private final boolean useNumber;

	public PullRequestChoiceProvider(boolean useNumber) {
		this.useNumber = useNumber;
	}

	@Override
	public void toJson(PullRequest choice, JSONWriter writer) throws JSONException {
		writer
			.key("id").value(choice.getId())
			.key("reference").value(choice.getReference().toString(getProject()))
			.key("title").value(Emojis.getInstance().apply(HtmlEscape.escapeHtml5(choice.getTitle())));
	}

	@Override
	public Collection<PullRequest> toChoices(Collection<String> ids) {
		List<PullRequest> requests = Lists.newArrayList();
		PullRequestService pullRequestService = OneDev.getInstance(PullRequestService.class);
		for (String id: ids) {
			PullRequest request = pullRequestService.load(Long.valueOf(id)); 
			Hibernate.initialize(request);
			requests.add(request);
		}
		return requests;
	}

	@Override
	public void query(String term, int page, Response<PullRequest> response) {
		int count = (page+1) * WebConstants.PAGE_SIZE + 1;
		var pullRequestService = OneDev.getInstance(PullRequestService.class);
		var subject = SecurityUtils.getSubject();
		if (useNumber) {
			Preconditions.checkState(getProject() != null);
			List<PullRequest> requests = pullRequestService.query(subject, getProject(), 
					new PullRequestQuery(new FuzzyCriteria(term)), false, 0, count);
			new ResponseFiller<>(response).fill(requests, page, WebConstants.PAGE_SIZE);
		} else {
			var scopedQuery = ProjectScopedQuery.of(getProject(), term, '#', '-');
			if (scopedQuery != null) {
				List<PullRequest> requests = pullRequestService.query(subject, scopedQuery.getProject(), 
						new PullRequestQuery(new FuzzyCriteria(scopedQuery.getQuery())), false, 0, count);
				new ResponseFiller<>(response).fill(requests, page, WebConstants.PAGE_SIZE);
			} else {
				response.setHasMore(false);
			}	
		}
	}
	
	@Nullable
	protected abstract Project getProject();
	
}