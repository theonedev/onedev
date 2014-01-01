package com.pmease.gitop.web.page.project.source.commit;

import java.util.Date;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Objects;
import com.pmease.commons.git.Commit;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.component.avatar.GitPersonAvatar;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.component.link.GitPersonLink.Mode;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.ProjectCategoryPage;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.util.GitUtils;

@SuppressWarnings("serial")
public class SourceCommitPage extends ProjectCategoryPage {
	public static PageParameters newParams(Project project, String revision) {
		PageParameters params = PageSpec.forProject(project);
		params.add(PageSpec.OBJECT_ID, revision);
		return params;
	}
	
	private IModel<Commit> commitModel;
	
	public SourceCommitPage(PageParameters params) {
		super(params);
		
		this.commitModel = new LoadableDetachableModel<Commit>() {

			@Override
			protected Commit load() {
				String revision = getRevision();
				Project project = getProject();
				return project.code().showRevision(revision);
			}
		};
	}

	@Override
	protected void onUpdateRevision(String rev) {
		// don't update revision in session
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new Label("shortmessage", new PropertyModel<String>(commitModel, "subject")));
		add(new Label("detailedmessage", new PropertyModel<String>(commitModel, "message")) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisibilityAllowed(!Objects.equal(getCommit().getSubject(), getCommit().getMessage()));
			}
		});
		
		IModel<GitPerson> authorModel = new AbstractReadOnlyModel<GitPerson>() {

			@Override
			public GitPerson getObject() {
				return GitPerson.of(getCommit().getAuthor());
			}
		};
		
		add(new GitPersonAvatar("authoravatar", authorModel));

		add(new GitPersonLink("author", authorModel, Mode.NAME_ONLY));
		
		add(new AgeLabel("authorday", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getCommit().getAuthor().getDate();
			}
		}));
		
		add(new GitPersonLink("committer", new AbstractReadOnlyModel<GitPerson>() {

			@Override
			public GitPerson getObject() {
				Commit commit = getCommit();
				return GitPerson.of(commit.getCommitter());
			}
			
		}, Mode.NAME_ONLY) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisibilityAllowed(!Objects.equal(getCommit().getAuthor(), 
													getCommit().getCommitter()));
			}
		});
		
		add(new AgeLabel("committerday", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getCommit().getCommitter().getDate();
			}
			
		}));
		
		add(new Label("commitsha", new PropertyModel<String>(commitModel, "hash")));
		
		IModel<List<String>> parentsModel = new AbstractReadOnlyModel<List<String>>() {

			@Override
			public List<String> getObject() {
				return getCommit().getParentHashes();
			}
		};
		
		ListView<String> parentsView = new ListView<String>("parents", parentsModel) {
			@Override
			protected void populateItem(ListItem<String> item) {
				String sha = item.getModelObject();
				
				AbstractLink link = new BookmarkablePageLink<Void>("link", SourceCommitPage.class,
						SourceCommitPage.newParams(getProject(), sha));
				item.add(link);
				link.add(new Label("sha", GitUtils.abbreviateSHA(sha)));
				WebMarkupContainer connector = new WebMarkupContainer("connector");
				int idx = item.getIndex();
				connector.setVisibilityAllowed(idx > 0);
				item.add(connector);
			}
		};
		add(parentsView);
	}

	protected Commit getCommit() {
		return commitModel.getObject();
	}
	
	@Override
	protected boolean isRevisionAware() {
		return false;
	}
	
	@Override
	public void onDetach() {
		if (commitModel != null) {
			commitModel.detach();
		}
		
		super.onDetach();
	}
	
	@Override
	protected String getPageTitle() {
		return getRevision() + " - " + getProject().getPathName();
	}
}
