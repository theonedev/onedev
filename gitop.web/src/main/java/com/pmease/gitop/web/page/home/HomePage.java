package com.pmease.gitop.web.page.home;

import org.apache.wicket.devutils.stateless.StatelessComponent;

import com.pmease.gitop.web.common.component.dropzone.DropZoneBehavior;
import com.pmease.gitop.web.common.component.vex.VexLinkBehavior.VexIcon;
import com.pmease.gitop.web.page.AbstractLayoutPage;

@StatelessComponent
public class HomePage extends AbstractLayoutPage {

	private static final long serialVersionUID = 1L;
	
	public HomePage() {
		this.setStatelessHint(true);
	}

	@Override
	protected String getPageTitle() {
		return "Gitop - Home";
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new DropZoneBehavior());
	}

	VexIcon vexIcon = VexIcon.INFO;
	boolean displayed = true;
}
