package io.onedev.server.web.component.issue.choice;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class IssueChoiceProvider extends ChoiceProvider<Issue> {

	private static final long serialVersionUID = 1L;
	
	private final IModel<Project> projectModel;
	
	public IssueChoiceProvider(IModel<Project> projectModel) {
		this.projectModel = projectModel;
	}
	
	@Override
	public void detach() {
		projectModel.detach();
		super.detach();
	}

	@Override
	public void toJson(Issue choice, JSONWriter writer) throws JSONException {
		writer
			.key("id").value(choice.getId())
			.key("number").value(choice.getNumber())
			.key("title").value(HtmlEscape.escapeHtml5(choice.getTitle()));
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

	@Override
	public void query(String term, int page, Response<Issue> response) {
		int count = (page+1) * WebConstants.PAGE_SIZE + 1;
		Project project = projectModel.getObject();
		List<Issue> issues = OneDev.getInstance(IssueManager.class).query(project, term, count);		
		new ResponseFiller<>(response).fill(issues, page, WebConstants.PAGE_SIZE);
	}
	
}