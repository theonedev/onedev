package com.pmease.gitop.web.component.avatar;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.service.AvatarManager;

@SuppressWarnings("serial")
public class AvatarImage extends WebComponent {

	public AvatarImage(String id, User user) {
		super(id);
		
		final Long userId = user.getId();
		
		setDefaultModel(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				User user = Gitop.getInstance(Dao.class).load(User.class, userId);
				return Gitop.getInstance(AvatarManager.class).getAvatarUrl(user);
			}
			
		});
		
		setOutputMarkupId(true);
	}

	public AvatarImage(String id, final String email) {
		super(id);
		
		setDefaultModel(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return Gitop.getInstance(AvatarManager.class).getAvatarUrl(email);
			}
			
		});
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		
		tag.setName("img");
		tag.put("src", getDefaultModelObjectAsString());
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof AvatarChanged) {
			AjaxRequestTarget target = ((AvatarChanged) event.getPayload()).getTarget();
			target.add(this);
		}
	}

}
