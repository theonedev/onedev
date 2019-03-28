package io.onedev.server.web.component.pullrequest.choice;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class PullRequestChoiceProvider extends ChoiceProvider<PullRequest> {

	private static final long serialVersionUID = 1L;
	
	private final IModel<Project> projectModel;
	
	public PullRequestChoiceProvider(IModel<Project> projectModel) {
		this.projectModel = projectModel;
	}
	
	@Override
	public void detach() {
		projectModel.detach();
		super.detach();
	}

	@Override
	public void toJson(PullRequest choice, JSONWriter writer) throws JSONException {
		writer
			.key("id").value(choice.getId())
			.key("number").value(choice.getNumber())
			.key("title").value(HtmlEscape.escapeHtml5(choice.getTitle()));
	}

	@Override
	public Collection<PullRequest> toChoices(Collection<String> ids) {
		List<PullRequest> requests = Lists.newArrayList();
		for (String id: ids)
			requests.add(OneDev.getInstance(PullRequestManager.class).load(Long.valueOf(id)));
		return requests;
	}

	@Override
	public void query(String term, int page, Response<PullRequest> response) {
		int count = (page+1) * WebConstants.PAGE_SIZE;
		Project project = projectModel.getObject();
		List<PullRequest> requests = OneDev.getInstance(PullRequestManager.class).query(project, term, count);		
		new ResponseFiller<>(response).fill(requests, page, WebConstants.PAGE_SIZE);
	}
	
}