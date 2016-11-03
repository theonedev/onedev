package com.gitplex.web.component.avatar;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.Account;
import com.gitplex.web.avatar.AvatarManager;
import com.gitplex.commons.wicket.behavior.TooltipBehavior;

@SuppressWarnings("serial")
public abstract class RemoveableAvatar extends AjaxLink<Account> {

	public RemoveableAvatar(String id, IModel<Account> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		configure();
		
		Account user = getModelObject();
		String tooltip;
		// Force nowrap here when display tooltip as otherwise user name will be wrapped inside 
		// an element with position set to relative
		if (isEnabled()) {
			tooltip = "Remove reviewer " + user.getDisplayName();
		} else {
			tooltip = user.getDisplayName();
		}
		add(new TooltipBehavior(Model.of("<span style='white-space: nowrap;'>" 
				+ StringEscapeUtils.escapeHtml4(tooltip) + "</span>")));
		add(AttributeAppender.append("class", "removeable-avatar"));
		add(AttributeAppender.append("data-html", "true"));
		
		setEscapeModelStrings(false);
	}
	
	@Override
	public IModel<?> getBody() {
		String url = GitPlex.getInstance(AvatarManager.class).getAvatarUrl(getModelObject());
		return Model.of("<img src='" + url + "' class='avatar'></img> <i class='fa fa-times'></i>");
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		if (!isEnabled())
			tag.setName("span");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new AvatarResourceReference()));
	}
	
}
