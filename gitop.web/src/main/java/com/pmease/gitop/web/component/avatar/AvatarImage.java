package com.pmease.gitop.web.component.avatar;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.devutils.stateless.StatelessComponent;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ConfigManager;
import com.pmease.gitop.model.User;

@StatelessComponent
public class AvatarImage extends Panel {

	private static final long serialVersionUID = 1L;
	
	private User user;

	public AvatarImage(String id, User user) {
		super(id);
		this.user = user;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		add(createAvatarImage());

	}

	private Component createAvatarImage() {
		boolean gravatarEnabled = Gitop.getInstance(ConfigManager.class).getSystemSetting().isGravatarEnabled();
		if (gravatarEnabled && !user.getLocalAvatar().exists()) {
			return (new GravatarImage("avatar", Model.of(user.getEmail())));
		} else {
			PageParameters params = new PageParameters();
			params.set("id", user.getId());
			return (new StatelessAvatarImage("avatar", params));
		}
	}

	private class StatelessAvatarImage extends NonCachingImage {
		private static final long serialVersionUID = 1L;

		public StatelessAvatarImage(String id, PageParameters params) {
			super(id, new AvatarImageResourceReference(), params);
		}

		@Override
		protected boolean getStatelessHint() {
			return true;
		}
	}

	@Override
	public void onEvent(IEvent<?> event) {
		if (event.getPayload() instanceof AvatarChanged) {
			AjaxRequestTarget target = ((AvatarChanged) event.getPayload())
					.getTarget();
			this.addOrReplace(createAvatarImage());
			target.add(this);
		}
	}
}
