package com.pmease.gitop.web.page.project.source.commit;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Restrictions;

import com.google.common.collect.Iterables;
import com.pmease.commons.git.Commit;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.CommitCommentManager;
import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.common.wicket.bootstrap.Icon;
import com.pmease.gitop.web.component.commit.CommitMetaPanel;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.component.link.GitPersonLink.Mode;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.git.command.CommitInCommand;
import com.pmease.gitop.web.git.command.CommitInCommand.RefType;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.ProjectCategoryPage;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.page.project.source.commit.diff.CommentListPanel;
import com.pmease.gitop.web.page.project.source.commit.diff.CommitCommentsAware;
import com.pmease.gitop.web.page.project.source.commit.diff.DiffViewPanel;
import com.pmease.gitop.web.page.project.source.tree.SourceTreePage;

@SuppressWarnings("serial")
public class SourceCommitPage extends ProjectCategoryPage implements CommitCommentsAware {
	
	public static PageParameters newParams(Project project, String revision) {
		PageParameters params = PageSpec.forProject(project);
		params.add(PageSpec.OBJECT_ID, revision);
		return params;
	}
	
	private final IModel<Commit> commitModel;
	private final IModel<List<CommitComment>> commentsModel;
	
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
		
		this.commentsModel = new LoadableDetachableModel<List<CommitComment>>() {

			@Override
			protected List<CommitComment> load() {
				return loadComments();
			}
			
		};
	}

	private List<CommitComment> loadComments() {
		return Gitop.getInstance(CommitCommentManager.class)
				.query(Restrictions.eq("project", getProject()),
						Restrictions.eq("commit", getUntil()));
	}
	
	private String getSince() {
		Commit commit = getCommit();
		return Iterables.getFirst(commit.getParentHashes(), null);
	}
	
	private String getUntil() {
		return getCommit().getHash();
	}
	
	@Override
	protected void onUpdateRevision(String rev) {
		// don't update revision in session
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new Label("shortmessage", new PropertyModel<String>(commitModel, "subject")));
		add(new Label("detailedmessage", new PropertyModel<String>(commitModel, "message")));
		
		IModel<GitPerson> authorModel = new AbstractReadOnlyModel<GitPerson>() {

			@Override
			public GitPerson getObject() {
				return GitPerson.of(getCommit().getAuthor());
			}
		};
		
		add(new BookmarkablePageLink<Void>("treelink",
				SourceTreePage.class,
				SourceTreePage.newParams(getProject(), getRevision())));
		
		add(new GitPersonLink("authoravatar", authorModel, Mode.AVATAR).enableTooltip("left"));
		
		add(new CommitMetaPanel("meta", commitModel));
		add(new Label("commitsha", new PropertyModel<String>(commitModel, "hash")));
		
		IModel<List<String>> parentsModel = new AbstractReadOnlyModel<List<String>>() {

			@Override
			public List<String> getObject() {
				return getCommit().getParentHashes();
			}
		};
		
		add(new Label("parentslabel", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				int parents = getCommit().getParentHashes().size();
				if (parents == 0) {
					return "no parent";
				} else if (parents == 1) {
					return "1 parent";
				} else {
					return parents + " parents";
				}
			}
			
		}));
		
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
		
		add(createInRefListView("branches", RefType.BRANCH));
		add(createInRefListView("tags", RefType.TAG));
		add(new DiffViewPanel("diffs", projectModel, Model.of(getSince()), Model.of(getUntil())));
		
		add(new CommentListPanel("comments", projectModel, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getCommit().getHash();
			}
			
		}));
	}

	private Component createInRefListView(String id, final RefType type) {
		return new AjaxLazyLoadPanel(id, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				CommitInCommand command = new CommitInCommand(getProject().code().repoDir());
				command.commit(getRevision()).in(type);
				return command.call();
			}
			
		}) {
			
			@SuppressWarnings("unchecked")
			List<String> getRefs() {
				return (List<String>) getDefaultModelObject();
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				this.setVisibilityAllowed(!getRefs().isEmpty());
			}
			
			@Override
			public Component getLoadingComponent(final String markupId) {
				return new WebMarkupContainer(markupId).setVisibilityAllowed(false);
			}

			@SuppressWarnings("unchecked")
			@Override
			public Component getLazyLoadComponent(String markupId) {
				Fragment frag = new Fragment(markupId, "refsFrag", SourceCommitPage.this);
				frag.add(new Icon("type", Model.of("icon-git-" + type.name().toLowerCase())));
				frag.add(new ListView<String>("refs", (IModel<List<String>>) getDefaultModel()) {

					@Override
					protected void populateItem(ListItem<String> item) {
						String branchName = item.getModelObject();
						BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("link", SourceTreePage.class,
								SourceTreePage.newParams(getProject(), branchName));
						
						link.add(new Label("name", branchName));
						item.add(link);
					}
					
				});
				return frag;
			}
			
		};
	}
	
	protected Commit getCommit() {
		return commitModel.getObject();
	}
	
	@Override
	protected boolean isRevisionAware() {
		return false;
	}
	
	@Override
	public List<CommitComment> getCommitComments() {
		return commentsModel.getObject();
	}

	@Override
	public boolean isShowInlineComments() {
		return true;
	}

	@Override
	public boolean canAddComments() {
		return true;
	}

	@Override
	public void onDetach() {
		if (commitModel != null) {
			commitModel.detach();
		}
		if (commentsModel != null) {
			commentsModel.detach();
		}

		super.onDetach();
	}
	
	@Override
	protected String getPageTitle() {
		return getCommit().getSubject() + " - "
				+ GitUtils.abbreviateSHA(getRevision(), 8) 
				+ " - " + getProject().getPathName();
	}
}
