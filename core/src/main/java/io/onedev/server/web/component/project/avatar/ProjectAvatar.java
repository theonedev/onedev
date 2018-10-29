package io.onedev.server.web.component.project.avatar;

import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.web.util.avatar.AvatarManager;

@SuppressWarnings("serial")
public class ProjectAvatar extends WebComponent {

	private final Long projectId;
	
	private String url;
	
	public ProjectAvatar(String id, Project project) {
		super(id);

		AvatarManager avatarManager = OneDev.getInstance(AvatarManager.class);
		projectId = project.getId();
		url = avatarManager.getAvatarUrl(project.getFacade());
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
	protected void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectAvatarResourceReference()));
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		
		tag.setName("img");
		tag.append("class", "project-avatar", " ");
		tag.put("src", url);
	}

}
