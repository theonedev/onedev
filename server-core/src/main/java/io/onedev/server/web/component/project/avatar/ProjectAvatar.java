package io.onedev.server.web.component.project.avatar;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.web.avatar.AvatarManager;

@SuppressWarnings("serial")
public class ProjectAvatar extends WebComponent {

	private String url;
	
	public ProjectAvatar(String id, Project project) {
		super(id);

		url = getAvatarManager().getAvatarUrl(project);
	}
	
	private AvatarManager getAvatarManager() {
		return OneDev.getInstance(AvatarManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		
		tag.setName("img");
		tag.append("class", "avatar", " ");
		tag.put("src", url);
	}

}
