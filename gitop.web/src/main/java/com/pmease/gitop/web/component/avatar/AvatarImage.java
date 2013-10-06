package com.pmease.gitop.web.component.avatar;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.devutils.stateless.StatelessComponent;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Strings;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.User;

@StatelessComponent
public class AvatarImage extends Panel {

	private static final long serialVersionUID = 1L;

	public static enum AvatarImageType {
		USER, REPOSITORY
	}

	private final AvatarImageType imageType;

	public AvatarImage(String id, IModel<User> model) {
		this(id, model, AvatarImageType.USER);
	}

	public AvatarImage(String id, IModel<?> model, AvatarImageType imageType) {
		super(id, model);
		this.imageType = imageType;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		add(createAvatarImage());

	}

	private Component createAvatarImage() {
		if (imageType == AvatarImageType.USER) {
			User user = (User) getDefaultModelObject();
			if (Strings.isNullOrEmpty(user.getAvatarUrl())) {
				return (new GravatarImage("avatar", Model.of(user)));
			} else {
				PageParameters params = new PageParameters();
				params.set("type", AvatarImageType.USER.name().toLowerCase());
				params.set("id", user.getId());
				return (new StatelessAvatarImage("avatar", params));
			}
		} else {
			Project project = (Project) getDefaultModelObject();
			PageParameters params = new PageParameters();
			params.set("type", AvatarImageType.REPOSITORY.name().toLowerCase());
			params.set("id", project.getId());
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
