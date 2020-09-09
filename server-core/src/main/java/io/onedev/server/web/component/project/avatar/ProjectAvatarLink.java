package io.onedev.server.web.component.project.avatar;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.web.avatar.AvatarManager;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;

@SuppressWarnings("serial")
public class ProjectAvatarLink extends ViewStateAwarePageLink<Void> {

	private final PageParameters params;
	
	private String url;
	
	private String tooltip;
	
	public ProjectAvatarLink(String id, Project project) {
		super(id, ProjectBlobPage.class);

		AvatarManager avatarManager = OneDev.getInstance(AvatarManager.class);
		params = ProjectPage.paramsOf(project);
		url = avatarManager.getAvatarUrl(project);
		tooltip = project.getName();
	}
	
	@Override
	public PageParameters getPageParameters() {
		return params;
	}

	@Override
	public IModel<?> getBody() {
		return Model.of("<img src='" + url + "' class='avatar'></img>");
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		configure();
		if (!isEnabled())
			tag.setName("span");
		tag.put("title", tooltip);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setEnabled(!params.isEmpty());
		setEscapeModelStrings(false);
		
		setOutputMarkupId(true);
	}

}
