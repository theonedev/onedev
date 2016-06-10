package com.pmease.gitplex.web.page.test;

import java.util.UUID;

import org.apache.wicket.markup.html.link.Link;

import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						UnitOfWork unitOfWork = GitPlex.getInstance(UnitOfWork.class);
						unitOfWork.begin();
						DepotManager depotManager = GitPlex.getInstance(DepotManager.class);
						Depot depot = depotManager.load(1L);
						depot.getNextPullRequestNumber();
						depotManager.test(depot);
						unitOfWork.end();
					}
					
				};
				new Thread(runnable).start();
				new Thread(runnable).start();
				new Thread(runnable).start();
				new Thread(runnable).start();
				new Thread(runnable).start();
			}
			
		});
		add(new Link<Void>("test2") {

			@Override
			public void onClick() {
				DepotManager depotManager = GitPlex.getInstance(DepotManager.class);
				Depot depot = depotManager.load(1L);
				depot.setDescription(UUID.randomUUID().toString());
				depotManager.persist(depot);
			}
			
		});
	}

}
