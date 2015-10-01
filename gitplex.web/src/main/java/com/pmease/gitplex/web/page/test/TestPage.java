package com.pmease.gitplex.web.page.test;

import java.util.Date;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.core.model.InlineInfo;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private int line = 1;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("test1") {

			@Override
			public void onClick() {
				Comment comment = GitPlex.getInstance(Dao.class).load(Comment.class, 1L);
				InlineInfo inlineInfo = new InlineInfo();
				inlineInfo.setLine(line++);
				inlineInfo.setBlobIdent(new BlobIdent("rev", "path", 0));
				inlineInfo.setCompareWith(new BlobIdent("rev", "path", 0));
				comment.setInlineInfo(inlineInfo);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				GitPlex.getInstance(Dao.class).persist(comment);
			}
			
		});
		add(new Link<Void>("test2") {

			@Override
			public void onClick() {
				Comment comment = GitPlex.getInstance(Dao.class).load(Comment.class, 1L);
				comment.setContent(new Date().toString());
				GitPlex.getInstance(Dao.class).persist(comment);
			}
			
		});
		add(new Link<Void>("test3") {

			@Override
			public void onClick() {
				Comment comment = GitPlex.getInstance(Dao.class).load(Comment.class, 1L);
				System.out.println("==============================");
				System.out.println("content: " + comment.getContent());
				System.out.println("line: " + comment.getInlineInfo().getLine());
				System.out.println(comment.getVersion());
			}
			
		});
	}		

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}
	
}
