package io.onedev.server.web.component.build.choice;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class BuildChoiceProvider extends ChoiceProvider<Build> {

	private static final long serialVersionUID = 1L;
	
	private final IModel<Project> projectModel;
	
	public BuildChoiceProvider(IModel<Project> projectModel) {
		this.projectModel = projectModel;
	}
	
	@Override
	public void detach() {
		projectModel.detach();
		super.detach();
	}

	@Override
	public void toJson(Build choice, JSONWriter writer) throws JSONException {
		writer
			.key("id").value(choice.getId())
			.key("number").value(choice.getNumber())
			.key("version").value(HtmlEscape.escapeHtml5(choice.getVersion()))
			.key("jobName").value(HtmlEscape.escapeHtml5(choice.getJobName()));
	}

	@Override
	public Collection<Build> toChoices(Collection<String> ids) {
		List<Build> builds = Lists.newArrayList();
		for (String id: ids)
			builds.add(OneDev.getInstance(BuildManager.class).load(Long.valueOf(id)));
		return builds;
	}

	@Override
	public void query(String term, int page, Response<Build> response) {
		int count = (page+1) * WebConstants.PAGE_SIZE;
		Project project = projectModel.getObject();
		List<Build> builds = OneDev.getInstance(BuildManager.class).query(project, term, count);		
		new ResponseFiller<>(response).fill(builds, page, WebConstants.PAGE_SIZE);
	}
	
}