package com.turbodev.server.web.behavior.inputassist;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.turbodev.server.web.assets.js.caret.CaretResourceReference;
import com.turbodev.server.web.assets.js.hotkeys.HotkeysResourceReference;
import com.turbodev.server.web.assets.js.scrollintoview.ScrollIntoViewResourceReference;
import com.turbodev.server.web.assets.js.textareacaretposition.TextareaCaretPositionResourceReference;
import com.turbodev.server.web.page.base.BaseDependentCssResourceReference;
import com.turbodev.server.web.page.base.BaseDependentResourceReference;

public class InputAssistResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public InputAssistResourceReference() {
		super(InputAssistResourceReference.class, "input-assist.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new ScrollIntoViewResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CaretResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new TextareaCaretPositionResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				InputAssistResourceReference.class, "input-assist.css")));
		return dependencies;
	}

}
