package io.onedev.server.web.page.test;

import java.io.File;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;

import io.onedev.server.web.component.avatarupload.AvatarUploadField;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private AvatarUploadField avatarUpload;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		String avatarData = AvatarUploadField.readFromFile(new File("w:\\temp\\avatar.jpg"));
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				AvatarUploadField.writeToFile(new File("w:\\temp\\avatar.jpg"), avatarUpload.getModelObject());
			}
			
		};
		form.add(avatarUpload = new AvatarUploadField("avatar", Model.of(avatarData)));
		avatarUpload.setRequired(true);
		
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new TestResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.test.onDomReady();"));
	}

}
