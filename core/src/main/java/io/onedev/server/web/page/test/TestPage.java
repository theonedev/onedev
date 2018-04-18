package io.onedev.server.web.page.test;

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;

import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Label("date", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return new Date().toString();
			}
			
		}));
	}

}
