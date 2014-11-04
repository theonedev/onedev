package com.pmease.gitplex.web.page;

import java.io.File;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.git.Git;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	public TestPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				Git git = new Git(new File("W:\\temp\\gitplex_storage\\repositories\\1"));
				for (int i=1; i<100; i++)
					git.createBranch("dev" + i, "master~" + i*10);
			}
			
		});
	}
}
