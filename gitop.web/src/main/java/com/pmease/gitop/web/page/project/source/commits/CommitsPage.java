package com.pmease.gitop.web.page.project.source.commits;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.parboiled.common.Preconditions;

import com.google.common.base.Strings;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.ProjectCategoryPage;
import com.pmease.gitop.web.page.project.api.IRevisionAware;
import com.pmease.gitop.web.page.project.source.component.RevisionSelector;
import com.pmease.gitop.web.page.project.source.tree.SourceTreePage;

@SuppressWarnings("serial")
public class CommitsPage extends ProjectCategoryPage implements IRevisionAware {

	public static PageParameters newParams(Project project, String revision, int page) {
		Preconditions.checkNotNull(project);
		PageParameters params = PageSpec.forProject(project);
		if (!Strings.isNullOrEmpty(revision)) {
			params.set("objectId", revision);
		}
		
		if (page > 0) {
			params.set("page", page);
		}
		
		return params;
	}
	
	private int page;
	
	public CommitsPage(PageParameters params) {
		super(params);
		
		page = params.get("page").toInt(1);
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new RevisionSelector("revselector", projectModel, revisionModel, null));
		BookmarkablePageLink<Void> homeLink = new BookmarkablePageLink<Void>("home", 
				SourceTreePage.class, 
				PageSpec.forProject(getProject()).add(PageSpec.OBJECT_ID, getRevision()));
		add(homeLink);
		homeLink.add(new Label("name", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getProject().getName();
			}
		}));
		
		IModel<List<Commit>> model = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				Git git = getProject().code();
				
				List<Commit> commits = new LogCommand(git.repoDir())
										.toRev(getRevision())
										.skip((page - 1) * 35)
										.maxCount(35)
										.call();
				return commits;
			}
		};
		
		add(new CommitsPanel("commits", model, projectModel));
		add(new BookmarkablePageLink<Void>("newer", CommitsPage.class,
				newParams(getProject(), getRevision(), page - 1)).setEnabled(page > 1));
		add(new BookmarkablePageLink<Void>("older", CommitsPage.class,
				newParams(getProject(), getRevision(), page + 1)) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				Git git = getProject().code();
				List<Commit> commits = new LogCommand(git.repoDir())
										.toRev(getRevision())
										.skip(page * 35)
										.maxCount(1)
										.call();
				this.setEnabled(!commits.isEmpty());
			}
		});
	}
	
	@Override
	protected String getPageTitle() {
		return "Commits - " + getProject();
	}

}
