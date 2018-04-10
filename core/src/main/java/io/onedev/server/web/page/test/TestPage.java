package io.onedev.server.web.page.test;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.web.asset.scrollintoview.ScrollIntoViewResourceReference;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Serializable bean = new Bean();
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				target.add(this);
			}
			
		};
		form.add(BeanContext.editBean("editor", bean));
		form.add(new AjaxButton("submit") {});
		form.setOutputMarkupId(true);
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ScrollIntoViewResourceReference()));
	}

}
