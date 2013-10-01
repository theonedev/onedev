package com.pmease.gitop.web.page.home;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import com.pmease.commons.wicket.behavior.confirm.ConfirmBehavior;
import com.pmease.gitop.web.common.component.fileupload.FileUploadResourceBehavior;
import com.pmease.gitop.web.common.component.messenger.MessengerBehavior;
import com.pmease.gitop.web.common.component.vex.VexBehavior;
import com.pmease.gitop.web.page.AbstractLayoutPage;

public class HomePage extends AbstractLayoutPage {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getPageTitle() {
		return "Gitop - Home";
	}

	@SuppressWarnings("serial")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new MessengerBehavior());
		add(new VexBehavior());
		add(new FileUploadResourceBehavior());
//		add(new FileUploadBar("upload"));
		
		add(new AjaxLink<Void>("vex") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				System.out.println("xxx");
			}
			
		}.add(new ConfirmBehavior("Are you sure?")));
	}
}
