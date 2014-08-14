package com.pmease.gitplex.web.page;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.git.DiffTreeNode;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.behavior.ScrollBehavior;
import com.pmease.commons.wicket.behavior.StickyBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.diff.BlobDiffInfo;
import com.pmease.gitplex.web.component.diff.BlobDiffPanel;
import com.pmease.gitplex.web.component.diff.DiffTreePanel;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private IModel<Repository> repoModel;
	
	private String comment;
	
	public TestPage(PageParameters params) {
		super(params);
		
		repoModel = new LoadableDetachableModel<Repository>() {

			@Override
			protected Repository load() {
				return GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
			}
		};
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new DiffTreePanel("test", repoModel, "dev~1", "dev") {

			@Override
			protected Link<Void> newFileLink(String id, final DiffTreeNode node) {
				return new Link<Void>(id) {

					@Override
					public void onClick() {
						TestPage.this.replace(new BlobDiffPanel("diff", repoModel, 
								BlobDiffInfo.from(repoModel.getObject().git(), node, "dev~1", "dev")));
					}
					
				};
			}
			
		});
		
		add(new WebMarkupContainer("diff"));
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				System.out.println("comment: " + comment);
			}

			@Override
			protected void onError() {
				System.out.println("error");
			}
			
		};
		form.add(new CommentInput("comment", new PropertyModel<String>(this, "comment")));
		add(form);
		
		WebMarkupContainer head = new WebMarkupContainer("head");
		head.add(new StickyBehavior());
		head.add(new WebMarkupContainer("prev").add(new ScrollBehavior(head, ".diff", 200, false)));
		head.add(new WebMarkupContainer("next").add(new ScrollBehavior(head, ".diff", 200, true)));
		add(head);
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}
	
}
