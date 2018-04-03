package io.onedev.server.web.page.test;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form");
		form.add(new NotificationPanel("feedback", form));
		TextField<String> content = new TextField<String>("content");
		content.setRequired(true);
		content.setLabel(Model.of("Content"));
		form.add(content);
		add(form);
	}

}
