package io.onedev.server.web.page.user;

import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.avatarupload.AvatarFileSelected;
import io.onedev.server.web.component.avatarupload.AvatarUploadField;
import io.onedev.server.web.component.user.avatar.UserAvatar;
import io.onedev.server.web.util.avatar.AvatarManager;

@SuppressWarnings("serial")
public class AvatarEditPage extends UserPage {
	
	private String uploadedAvatarData;
	
	public AvatarEditPage(PageParameters params) {
		super(params);
	}
	
	private AvatarManager getAvatarManager() {
		return OneDev.getInstance(AvatarManager.class);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new UserAvatar("avatar", getUser()));
		
		add(new Link<Void>("useDefault") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getAvatarManager().getUploaded(getUser().getFacade()).exists());
			}

			@Override
			public void onClick() {
				getAvatarManager().useAvatar(getUser().getFacade(), null);
				setResponsePage(AvatarEditPage.class, AvatarEditPage.paramsOf(getUser()));
			}
			
		});

		Button uploadButton = new Button("upload");
		uploadButton.setVisible(false).setOutputMarkupPlaceholderTag(true);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				AvatarManager avatarManager = OneDev.getInstance(AvatarManager.class);
            	avatarManager.useAvatar(getUser().getFacade(), uploadedAvatarData);
				setResponsePage(AvatarEditPage.class, AvatarEditPage.paramsOf(getUser()));
			}

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof AvatarFileSelected) {
					AvatarFileSelected avatarFileSelected = (AvatarFileSelected) event.getPayload();
					uploadButton.setVisible(true);
					avatarFileSelected.getHandler().add(uploadButton);
				}
			}
		};

		PropertyModel<String> avatarDataModel = new PropertyModel<String>(this, "uploadedAvatarData");
		form.add(new AvatarUploadField("avatar", avatarDataModel));
		form.add(uploadButton);
		
		add(form);
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAdministrate(getUser().getFacade());
	}
	
}
