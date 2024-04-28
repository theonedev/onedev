package io.onedev.server.web.component.issue.choice;

import com.google.common.collect.Lists;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScopedQuery;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public abstract class IssueChoiceProvider extends ChoiceProvider<Issue> {

	private static final long serialVersionUID = 1L;
	
	@Nullable
	protected abstract Project getProject();
	
	@Override
	public void toJson(Issue choice, JSONWriter writer) throws JSONException {
		writer
			.key("id").value(choice.getId())
			.key("reference").value(choice.getReference().toString(getProject()))
			.key("title").value(Emojis.getInstance().apply(HtmlEscape.escapeHtml5(choice.getTitle())));
	}

	@Override
	public Collection<Issue> toChoices(Collection<String> ids) {
		List<Issue> issues = Lists.newArrayList();
		IssueManager issueManager = OneDev.getInstance(IssueManager.class);
		for (String id: ids) {
			Issue issue = issueManager.load(Long.valueOf(id)); 
			Hibernate.initialize(issue);
			issues.add(issue);
		}
		return issues;
	}
	
	@Nullable
	protected EntityQuery<Issue> getScope() {
		return null;
	}
	
	@Override
	public void query(String term, int page, Response<Issue> response) {
		int count = (page+1) * WebConstants.PAGE_SIZE + 1;
		var scopedQuery = ProjectScopedQuery.of(getProject(), term, '#', '-');
		if (scopedQuery != null) {
			List<Issue> issues = OneDev.getInstance(IssueManager.class)
					.query(getScope(), scopedQuery.getProject(), scopedQuery.getQuery(), count);
			new ResponseFiller<>(response).fill(issues, page, WebConstants.PAGE_SIZE);
		} else {
			response.setHasMore(false);
		}
	}
	
}