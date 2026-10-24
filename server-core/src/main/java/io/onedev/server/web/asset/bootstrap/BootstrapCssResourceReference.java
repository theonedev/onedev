package io.onedev.server.web.asset.bootstrap;

import java.io.Serial;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

public class BootstrapCssResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public BootstrapCssResourceReference() {
		super(BootstrapCssResourceReference.class, "css/bootstrap-custom.css");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				BootstrapCssResourceReference.class, "css/bootstrap.min.css")));
		return dependencies;
	}

}
