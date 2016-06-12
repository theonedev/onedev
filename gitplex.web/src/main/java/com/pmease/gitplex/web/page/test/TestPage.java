package com.pmease.gitplex.web.page.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.markup.html.link.Link;

import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private List<String> commitIds;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		/*
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				Depot depot = GitPlex.getInstance(DepotManager.class).load(1L);
				for (int i=0; i<1000; i++) {
					GitPlex.getInstance(CodeCommentManager.class).test(depot);
					System.out.println(i);
				}
			}
			
		});
		*/
		
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				commitIds = new ArrayList<>();
				long time = System.currentTimeMillis();
				EntityCriteria<CodeComment> criteria = EntityCriteria.of(CodeComment.class);
				List<CodeComment> comments = GitPlex.getInstance(CodeCommentManager.class).query(criteria, 0, 1000);
				for (CodeComment comment: comments)
					commitIds.add(comment.getCommit());
				System.out.println(System.currentTimeMillis()-time);
			}
			
		});
		
		add(new Link<Void>("test2") {

			@Override
			public void onClick() {
				Depot depot = GitPlex.getInstance(DepotManager.class).load(1L);
				long time = System.currentTimeMillis();
				for (int i=0; i<10000; i++) {
					GitPlex.getInstance(CodeCommentManager.class).query(depot, UUID.randomUUID().toString());
				}
				System.out.println(System.currentTimeMillis()-time);
			}
			
		});
	}

}
