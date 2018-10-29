package io.onedev.server.web.component.project.avatar;

import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.util.avatar.AvatarManager;

@SuppressWarnings("serial")
public class ProjectAvatarLink extends ViewStateAwarePageLink<Void> {

	private final Long projectId;
	
	private final PageParameters params;
	
	private String url;
	
	private String tooltip;
	
	public ProjectAvatarLink(String id, Project project) {
		super(id, ProjectBlobPage.class);

		AvatarManager avatarManager = OneDev.getInstance(AvatarManager.class);
		projectId = project.getId();
		params = ProjectPage.paramsOf(project);
		url = avatarManager.getAvatarUrl(project.getFacade());
		tooltip = project.getName();
	}
	
	@Override
	public PageParameters getPageParameters() {
		return params;
	}

	@Override
	public IModel<?> getBody() {
		return Model.of("<img src='" + url + "' class='project-avatar'></img>");
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof ProjectAvatarChanged) {
			ProjectAvatarChanged avatarChanged = (ProjectAvatarChanged) event.getPayload();
			if (avatarChanged.getProject().getId().equals(projectId)) {
				AvatarManager avatarManager = OneDev.getInstance(AvatarManager.class);
				url = avatarManager.getAvatarUrl(avatarChanged.getProject().getFacade());
				avatarChanged.getHandler().add(this);
			}
		}
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
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectAvatarResourceReference()));
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setEnabled(!params.isEmpty());
		setEscapeModelStrings(false);
		
		setOutputMarkupId(true);
	}

}
