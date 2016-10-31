package com.gitplex.web.component.symboltooltip;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import com.gitplex.web.page.base.BaseDependentCssResourceReference;
import com.gitplex.web.page.base.BaseDependentResourceReference;

public class SymbolTooltipResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public SymbolTooltipResourceReference() {
		super(SymbolTooltipResourceReference.class, "symbol-tooltip.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(SymbolTooltipResourceReference.class, "symbol-tooltip.css")));
		return dependencies;
	}

}
