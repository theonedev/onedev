package com.gitplex.commons.wicket.behavior.inputassist;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.gitplex.commons.wicket.assets.caret.CaretResourceReference;
import com.gitplex.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.gitplex.commons.wicket.assets.scrollintoview.ScrollIntoViewResourceReference;
import com.gitplex.commons.wicket.assets.textareacaretposition.TextareaCaretPositionResourceReference;
import com.gitplex.commons.wicket.page.CommonDependentCssResourceReference;
import com.gitplex.commons.wicket.page.CommonDependentResourceReference;

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
