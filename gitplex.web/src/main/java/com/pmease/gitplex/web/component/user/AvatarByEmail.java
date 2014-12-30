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
import com.pmease.gitplex.web.service.AvatarManager;

@SuppressWarnings("serial")
public class AvatarByEmail extends WebComponent {

	private final IModel<String> avatarUrlModel;
	
	private final boolean withTooltip;

	public AvatarByEmail(String id, IModel<String> emailModel) {
		this(id, emailModel, true);
	}
	
	/**
	 * Display avatar of specified email model.
	 * 
	 * @param id
	 * 			component id
	 * @param userModel
	 * 			model of the user to display avatar for. This model allows to return <tt>null</tt> 
	 * 			to display avatar for unknown user 
	 */
	public AvatarByEmail(String id, IModel<String> emailModel, boolean withTooltip) {
		super(id, emailModel);
		
		avatarUrlModel = new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return GitPlex.getInstance(AvatarManager.class).getAvatarUrl(getEmail());
			}
			
		};
		
		this.withTooltip = withTooltip;
	}

	private String getEmail() {
		return getDefaultModelObjectAsString();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (withTooltip)
			add(new TooltipBehavior(Model.of(getEmail())));
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
