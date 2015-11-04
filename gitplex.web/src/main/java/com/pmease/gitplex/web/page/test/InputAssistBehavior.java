package com.pmease.gitplex.web.page.test;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

import com.pmease.commons.wicket.assets.caret.CaretResourceReference;
import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;

@SuppressWarnings("serial")
public abstract class InputAssistBehavior extends AbstractDefaultAjaxBehavior {

	@Override
	protected void onBind() {
		super.onBind();
		
		Component inputField = getComponent();
		inputField.setOutputMarkupId(true);
	}

	protected abstract InputAssist assist(String input, int cursor);

	@Override
	protected void respond(AjaxRequestTarget target) {
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forReference(CaretResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(HotkeysResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(CookiesResourceReference.INSTANCE));
		
		String script = String.format("gitplex.inputassist.init('#%s');", 
				getComponent().getMarkupId(true), 
				getCallbackFunction(explicit("input"), explicit("cursor")));
		
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}