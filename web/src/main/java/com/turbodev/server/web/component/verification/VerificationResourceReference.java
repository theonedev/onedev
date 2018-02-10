package com.turbodev.server.web.component.verification;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import com.turbodev.server.web.page.base.BaseDependentCssResourceReference;
import com.turbodev.server.web.page.base.BaseDependentResourceReference;

public class VerificationResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public VerificationResourceReference() {
		super(VerificationResourceReference.class, "verification.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				VerificationResourceReference.class, "verification.css")));
		return dependencies;
	}

}
