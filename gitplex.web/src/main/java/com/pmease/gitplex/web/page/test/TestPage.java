package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.html.link.Link;

import com.pmease.commons.util.concurrent.PrioritizedRunnable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.WorkManager;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				GitPlex.getInstance(WorkManager.class).execute(new PrioritizedRunnable(0) {
					
					@Override
					public void run() {
						try {
							System.out.println("begin");
							Thread.sleep(30000);
							System.out.println("end");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
			
		});
	}

}
