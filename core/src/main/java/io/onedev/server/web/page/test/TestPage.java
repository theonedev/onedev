package io.onedev.server.web.page.test;

import org.apache.wicket.markup.html.form.Form;

import com.google.common.collect.Sets;

import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Bean bean = new Bean();
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
			}
			
		};
		form.add(BeanContext.editBean("editor", bean, Sets.newHashSet(), Sets.newHashSet("age")));
		add(form);
	}

}
