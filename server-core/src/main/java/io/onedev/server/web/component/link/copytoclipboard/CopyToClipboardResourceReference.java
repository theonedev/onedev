package io.onedev.server.web.component.link.copytoclipboard;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.clipboard.ClipboardResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class CopyToClipboardResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public CopyToClipboardResourceReference() {
		super(CopyToClipboardResourceReference.class, "copy-to-clipboard.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new ClipboardResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				CopyToClipboardResourceReference.class, "copy-to-clipboard.css")));
		return dependencies;
	}

}
