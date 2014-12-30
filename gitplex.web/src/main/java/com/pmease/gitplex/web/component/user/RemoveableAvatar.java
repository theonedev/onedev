package com.pmease.gitplex.web.component.user;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public abstract class RemoveableAvatar extends Panel {

	private final IModel<User> userModel;
	
	public RemoveableAvatar(String id, IModel<User> userModel) {
		super(id);
		
		this.userModel = userModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		AjaxLink<Void> link = new AjaxLink<Void>("remove") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onAvatarRemove(target);
			}

		};

		link.add(new AvatarByUser("avatar", userModel, false));
		link.add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				// Force nowrap here when display tooltip as otherwise user name will be wrapped inside 
				// an element with position set to relative
				String escapedDisplayName = StringEscapeUtils.escapeHtml4(userModel.getObject().getDisplayName());
				return "<span style='white-space: nowrap;'>" + escapedDisplayName + "</span>";
			}
			
		}));
		
		add(link);
	}
	
	protected User getUser() {
		return userModel.getObject();
	}
	
	protected abstract void onAvatarRemove(AjaxRequestTarget target);

	@Override
	protected void onDetach() {
		userModel.detach();
		
		super.onDetach();
	}
	
}
