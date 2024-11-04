package io.onedev.server.web.editable.secret;

import io.onedev.server.web.asset.caret.CaretResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import java.util.List;

public class SecretEditorResourceReference extends BaseDependentResourceReference {
	public SecretEditorResourceReference() {
		super(SecretEditorResourceReference.class, "secret-editor.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		var dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CaretResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				SecretEditorResourceReference.class, "secret-editor.css")));
		return dependencies;
	}
}
