package com.pmease.gitplex.web.component.user;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.web.service.AvatarManager;

@SuppressWarnings("serial")
public class AvatarByEmail extends WebComponent {

	private IModel<String> avatarUrlModel;

	/**
	 * Display avatar of specified email model.
	 * 
	 * @param id
	 * 			component id
	 * @param userModel
	 * 			model of the user to display avatar for. This model allows to return <tt>null</tt> 
	 * 			to display avatar for unknown user 
	 */
	public AvatarByEmail(String id, IModel<String> emailModel) {
		super(id, emailModel);
		
		avatarUrlModel = new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return GitPlex.getInstance(AvatarManager.class).getAvatarUrl((String)getDefaultModelObject());
			}
			
		};
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
