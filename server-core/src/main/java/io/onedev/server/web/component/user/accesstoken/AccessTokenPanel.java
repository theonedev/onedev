package io.onedev.server.web.component.user.accesstoken;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.util.ConfirmClickModifier;

@SuppressWarnings("serial")
public abstract class AccessTokenPanel extends Panel {

	public AccessTokenPanel(String id) {
		super(id);
	}

	protected abstract User getUser();
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		IModel<String> valueModel = new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getUser().getAccessToken();
			}
			
		};
		add(new TextField<String>("value", valueModel) {

			@Override
			protected String[] getInputTypes() {
				return new String[] {"password"};
			}
			
		});
		
		add(new CopyToClipboardLink("copy", valueModel));
		
		add(new Link<Void>("regenerate") {

			@Override
			public void onClick() {
				getUser().setAccessToken(RandomStringUtils.randomAlphanumeric(User.ACCESS_TOKEN_LEN));
				OneDev.getInstance(UserManager.class).save(getUser());
				Session.get().success("Access token regenerated");
				setResponsePage(getPage());
			}
			
		}.add(new ConfirmClickModifier("This will invalidate current token and generate a new one, do you want to continue?")));
	}

}
