package com.pmease.gitop.web.page.home;

import com.pmease.gitop.web.common.component.dropzone.DropZoneBehavior;
import com.pmease.gitop.web.common.component.vex.VexLinkBehavior.VexIcon;
import com.pmease.gitop.web.page.AbstractLayoutPage;

public class HomePage extends AbstractLayoutPage {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getPageTitle() {
		return "Gitop - Home";
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new DropZoneBehavior());
	}

	VexIcon vexIcon = VexIcon.INFO;
	boolean displayed = true;
}
