package com.pmease.gitplex.web.component.avatar;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitplex.core.entity.Account;

@SuppressWarnings("serial")
public abstract class RemoveableAvatar extends Panel {

	private final IModel<Account> userModel;
	
	private final String actionName;
	
	public RemoveableAvatar(String id, IModel<Account> userModel, String actionName) {
		super(id);
		
		this.userModel = userModel;
		this.actionName = actionName;
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

		link.add(new Avatar("avatar", userModel.getObject(), null));
		link.add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				// Force nowrap here when display tooltip as otherwise user name will be wrapped inside 
				// an element with position set to relative
				String tooltip;
				if (isEnabled()) {
					if (actionName != null)
						tooltip = actionName + " " + userModel.getObject().getDisplayName();
					else
						tooltip = userModel.getObject().getDisplayName();
				} else {
					tooltip = userModel.getObject().getDisplayName();
				}
				return "<span style='white-space: nowrap;'>" + StringEscapeUtils.escapeHtml4(tooltip) + "</span>";
			}
			
		}));
		
		add(link);
	}
	
	protected Account getUser() {
		return userModel.getObject();
	}
	
	protected abstract void onAvatarRemove(AjaxRequestTarget target);

	@Override
	protected void onDetach() {
		userModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RemoveableAvatar.class, "avatar.css")));
	}
	
}
