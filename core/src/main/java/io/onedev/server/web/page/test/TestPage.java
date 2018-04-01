package io.onedev.server.web.page.test;

import org.apache.wicket.markup.html.form.Form;

import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Bean bean = new Bean();
		Form<?> form = new Form<Void>("form") {
			
		};
		form.add(BeanContext.editBean("editor", bean));
		add(form);
		
		bean.setScript("public class Test {}");
		
		add(BeanContext.viewBean("viewer", bean));
	}

}
