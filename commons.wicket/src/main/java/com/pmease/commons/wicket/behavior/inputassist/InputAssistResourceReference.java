package com.pmease.commons.wicket.behavior.inputassist;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.pmease.commons.wicket.assets.caret.CaretResourceReference;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.assets.scrollintoview.ScrollIntoViewResourceReference;
import com.pmease.commons.wicket.assets.textareacaretposition.TextareaCaretPositionResourceReference;
import com.pmease.commons.wicket.page.CommonDependentCssResourceReference;
import com.pmease.commons.wicket.page.CommonDependentResourceReference;

public class InputAssistResourceReference extends CommonDependentResourceReference {

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
		dependencies.add(CssHeaderItem.forReference(new CommonDependentCssResourceReference(InputAssistResourceReference.class, "input-assist.css")));
		return dependencies;
	}

}
