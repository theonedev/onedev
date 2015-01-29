package com.pmease.gitplex.web.component.user;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.avatar.AvatarManager;

@SuppressWarnings("serial")
public class AvatarByUser extends WebComponent {

	private final IModel<String> avatarUrlModel;
	
	private final boolean withTooltip;
	
	public AvatarByUser(String id, IModel<User> userModel) {
		this(id, userModel, true);
	}
	
	/**
	 * Display avatar of specified user model.
	 * 
	 * @param id
	 * 			component id
	 * @param userModel
	 * 			model of the user to display avatar for. If <tt>userModel.getObject()</tt>
	 * 			returns <tt>null</tt>, avatar of unknown user will be displayed
	 */
	public AvatarByUser(String id, IModel<User> userModel, boolean withTooltip) {
		super(id, userModel);
		
		avatarUrlModel = new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return GitPlex.getInstance(AvatarManager.class).getAvatarUrl(getUser());
			}
			
		};
		
		this.withTooltip = withTooltip;
	}
	
	private User getUser() {
		return (User) getDefaultModelObject();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		if (withTooltip)
			add(new TooltipBehavior(Model.of(getUser().getDisplayName())));
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		
		tag.setName("img");
		tag.put("src", avatarUrlModel.getObject());
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof AvatarChanged) {
			AjaxRequestTarget target = ((AvatarChanged) event.getPayload()).getTarget();
			target.add(this);
		}
	}

	@Override
	protected void onDetach() {
		avatarUrlModel.detach();
		
		super.onDetach();
	}

}
