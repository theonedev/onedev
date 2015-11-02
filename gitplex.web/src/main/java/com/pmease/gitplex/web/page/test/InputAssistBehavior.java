package com.pmease.gitplex.web.page.test;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import static org.apache.wicket.ajax.attributes.CallbackParameter.*;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

import com.pmease.commons.wicket.CommonPage;
import com.pmease.commons.wicket.assets.caret.CaretResourceReference;
import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;

@SuppressWarnings("serial")
public abstract class InputAssistBehavior extends AbstractDefaultAjaxBehavior {

	private DropdownPanel dropdown;
	
	@Override
	protected void onBind() {
		super.onBind();
		
		Component inputField = getComponent();
		inputField.setOutputMarkupId(true);
		
		CommonPage page = (CommonPage) inputField.getPage();
		dropdown = new DropdownPanel(page.getComponents().newChildId()) {

			@Override
			protected Component newContent(String id) {
				return new InputAssistPanel(id);
			}
			
		};
		page.getComponents().add(dropdown);
		
		inputField.add(new DropdownBehavior(dropdown)
				.mode(null)
				.alignWithTrigger(100, 100, 100, 0, 6, true));
	}

	protected abstract InputAssist assist(String input, int cursor);

	@Override
	protected void respond(AjaxRequestTarget target) {
		String script = String.format("gitplex.inputassist.fitWindow('%s', '%s');", 
				getComponent().getMarkupId(), dropdown.getMarkupId());
		target.appendJavaScript(script);
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