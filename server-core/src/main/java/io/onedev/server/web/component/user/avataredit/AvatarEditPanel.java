package io.onedev.server.web.component.user.avataredit;

import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import io.onedev.server.OneDev;
import io.onedev.server.service.AuditService;
import io.onedev.server.model.User;
import io.onedev.server.web.avatar.AvatarService;
import io.onedev.server.web.component.avatarupload.AvatarFileSelected;
import io.onedev.server.web.component.avatarupload.AvatarUploadField;
import io.onedev.server.web.component.user.UserAvatar;
import io.onedev.server.web.page.user.UserPage;

public class AvatarEditPanel extends GenericPanel<User> {
	
	private String uploadedAvatarData;
	
	public AvatarEditPanel(String id, IModel<User> model) {
		super(id, model);
	}
	
	private AvatarService getAvatarService() {
		return OneDev.getInstance(AvatarService.class);
	}

	private User getUser() {
		return getModelObject();
	}
	
	private AuditService getAuditService() {
		return OneDev.getInstance(AuditService.class);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new UserAvatar("avatar", getUser()));
		
		add(new Link<Void>("useDefault") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getAvatarService().getUserUploadedFile(getUser().getId(), null).exists());
			}

			@Override
			public void onClick() {
				getAvatarService().useUserAvatar(getUser().getId(), null);
				if (getPage() instanceof UserPage)
					getAuditService().audit(null, "specified to use default avatar in account \"" + getUser().getName() + "\"", null, null);
				setResponsePage(getPage().getClass(), getPage().getPageParameters());
			}
			
		});

		Button uploadButton = new Button("upload");
		uploadButton.setVisible(false).setOutputMarkupPlaceholderTag(true);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				AvatarService avatarService = OneDev.getInstance(AvatarService.class);
            	avatarService.useUserAvatar(getUser().getId(), uploadedAvatarData);
				if (getPage() instanceof UserPage)
					getAuditService().audit(null, "specified to use uploaded avatar in account \"" + getUser().getName() + "\"", null, null);
				setResponsePage(getPage().getClass(), getPage().getPageParameters());
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
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new AvatarEditCssResourceReference()));
	}
	
}
