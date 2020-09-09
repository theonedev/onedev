package io.onedev.server.web.page.project.setting.avatar;

import org.apache.wicket.Component;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.web.avatar.AvatarManager;
import io.onedev.server.web.component.avatarupload.AvatarFileSelected;
import io.onedev.server.web.component.avatarupload.AvatarUploadField;
import io.onedev.server.web.component.project.avatar.ProjectAvatar;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class AvatarEditPage extends ProjectSettingPage {
	
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

		add(new ProjectAvatar("avatar", getProject()));
		
		add(new Link<Void>("useDefault") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getAvatarManager().getUploaded(getProject()).exists());
			}

			@Override
			public void onClick() {
				getAvatarManager().useAvatar(getProject(), null);
				setResponsePage(AvatarEditPage.class, AvatarEditPage.paramsOf(getProject()));
			}
			
		});

		Button uploadButton = new Button("upload");
		uploadButton.setVisible(false).setOutputMarkupPlaceholderTag(true);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				AvatarManager avatarManager = OneDev.getInstance(AvatarManager.class);
            	avatarManager.useAvatar(getProject(), uploadedAvatarData);
				setResponsePage(AvatarEditPage.class, AvatarEditPage.paramsOf(getProject()));
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
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Edit Avatar");
	}
	
}
