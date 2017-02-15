package com.gitplex.server.web.component.branchpicker;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.gitplex.server.web.assets.hotkeys.HotkeysResourceReference;
import com.gitplex.server.web.page.base.BaseDependentResourceReference;

public class BranchSelectorResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public BranchSelectorResourceReference() {
		super(BranchSelectorResourceReference.class, "branch-selector.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(BranchSelectorResourceReference.class, "branch-selector.css")));
		return dependencies;
	}

}
