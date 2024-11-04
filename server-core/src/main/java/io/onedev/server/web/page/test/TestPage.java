package io.onedev.server.web.page.test;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.base.BasePage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.Serializable;

public class TestPage extends BasePage {
	
	public TestPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
			}
			
		});
		var bean = new Bean();
		var form = new Form<Void>("form") {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				System.out.println("password:" + bean.getPassword());
			}

			@Override
			protected void onError() {
				super.onError();
			}
		};
		form.add(BeanContext.edit("editor", bean));
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new TestResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.test.onDomReady();"));
	}		

	@Editable
	public static class Bean implements Serializable {
		
		private String password;

		@Editable
		@Password
		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}
}
