package io.onedev.server.web.component.project.choice;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.model.IModel;
import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.Similarities;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.avatar.AvatarManager;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class ProjectChoiceProvider extends ChoiceProvider<Project> {

	private static final long serialVersionUID = 1L;
	
	private final IModel<List<Project>> choicesModel;
	
	public ProjectChoiceProvider(IModel<List<Project>> choicesModel) {
		this.choicesModel = choicesModel;
	}
	
	@Override
	public void toJson(Project choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId());
		writer.key("path");
		writer.value(HtmlEscape.escapeHtml5(choice.getPath()));
		String avatarUrl = OneDev.getInstance(AvatarManager.class).getAvatarUrl(choice.getId());
		writer.key("avatar").value(avatarUrl);
	}
	
	@Override
	public Collection<Project> toChoices(Collection<String> ids) {
		return ids.stream()
				.map(it -> {
						Project project = getProjectManager().load(Long.valueOf(it));
						Hibernate.initialize(project);
						return project;
					}
				).collect(Collectors.toList());
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	@Override
	public void detach() {
		choicesModel.detach();
		super.detach();
	}

	@Override
	public void query(String term, int page, Response<Project> response) {
		List<Project> projects = choicesModel.getObject();
		
		ProjectCache cache = getProjectManager().cloneCache();
		
		List<Project> similarities = new Similarities<Project>(projects) {

			private static final long serialVersionUID = 1L;

			@Override
			public double getSimilarScore(Project object) {
				return cache.getSimilarScore(object, term);
			}
			
		};
		
		new ResponseFiller<Project>(response).fill(similarities, page, WebConstants.PAGE_SIZE);
	}

}