package io.onedev.server.web.component.wizard;

import java.io.Serial;

import org.apache.wicket.request.resource.CssResourceReference;

public class WizardResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public WizardResourceReference() {
		super(WizardResourceReference.class, "wizard.css");
	}

}
