package com.pmease.gitplex.web.page.repository.code.commit;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
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
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.OldCommitComment;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.common.wicket.bootstrap.Icon;
import com.pmease.gitplex.web.component.commit.CommitMetaPanel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.PersonLink;
import com.pmease.gitplex.web.git.command.CommitInCommand;
import com.pmease.gitplex.web.git.command.CommitInCommand.RefType;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.code.commit.diff.CommentListPanel;
import com.pmease.gitplex.web.page.repository.code.commit.diff.CommitCommentsAware;
import com.pmease.gitplex.web.page.repository.code.commit.diff.DiffViewPanel;
import com.pmease.gitplex.web.page.repository.code.tree.RepoTreePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class RepoCommitPage extends RepositoryPage implements CommitCommentsAware {
	
	private final IModel<Commit> commitModel;
	
	private final IModel<List<OldCommitComment>> commentsModel;
	
	public RepoCommitPage(PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));

		this.commitModel = new LoadableDetachableModel<Commit>() {

			@Override
			protected Commit load() {
				String revision = getRepository().defaultBranchIfNull(getCurrentRevision());
				Repository repository = getRepository();
				return repository.git().showRevision(revision);
			}
		};
		
		this.commentsModel = new LoadableDetachableModel<List<OldCommitComment>>() {

			@Override
			protected List<OldCommitComment> load() {
				return loadComments();
			}
			
		};
	}

	private List<OldCommitComment> loadComments() {
		return GitPlex.getInstance(Dao.class).query(EntityCriteria.of(OldCommitComment.class)
				.add(Restrictions.eq("repository", getRepository()))
				.add(Restrictions.eq("commit", getUntil())));
	}
	
	private String getSince() {
		Commit commit = getCommit();
		return Iterables.getFirst(commit.getParentHashes(), null);
	}
	
	private String getUntil() {
		return getCommit().getHash();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("shortmessage", new PropertyModel<String>(commitModel, "subject")));
		add(new Label("detailedmessage", new PropertyModel<String>(commitModel, "message")));
		
		add(new BookmarkablePageLink<Void>("treelink",
				RepoTreePage.class,
				RepoTreePage.paramsOf(getRepository(), getCurrentRevision(), null)));
		
		TooltipConfig tooltipConfig = new TooltipConfig().withPlacement(TooltipConfig.Placement.left);
		add(new PersonLink("authoravatar", Model.of(getCommit().getAuthor()), AvatarMode.AVATAR)
				.withTooltipConfig(tooltipConfig));
		
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
				
				AbstractLink link = new BookmarkablePageLink<Void>("link", 
						RepoCommitPage.class, paramsOf(getRepository(), sha, null));
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
		add(new DiffViewPanel("diffs", repoModel, getSince(), getUntil()));
		
		add(new CommentListPanel("comments", repoModel, new AbstractReadOnlyModel<String>() {

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
				CommitInCommand command = new CommitInCommand(getRepository().git().repoDir());
				command.commit(getRepository().defaultBranchIfNull(getCurrentRevision())).in(type);
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
				Fragment frag = new Fragment(markupId, "refsFrag", RepoCommitPage.this);
				frag.add(new Icon("type", Model.of("fa-" + type.name().toLowerCase())));
				frag.add(new ListView<String>("refs", (IModel<List<String>>) getDefaultModel()) {

					@Override
					protected void populateItem(ListItem<String> item) {
						String branchName = item.getModelObject();
						BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("link", RepoTreePage.class,
								RepoTreePage.paramsOf(getRepository(), branchName, null));
						
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
	public List<OldCommitComment> getCommitComments() {
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
		if (commitModel != null) 
			commitModel.detach();
		
		if (commentsModel != null)
			commentsModel.detach();

		super.onDetach();
	}
	
	@Override
	protected String getPageTitle() {
		String revision = getRepository().defaultBranchIfNull(getCurrentRevision());
		if (GitUtils.isHash(revision)) {
			return getCommit().getSubject() + " - " + GitUtils.abbreviateSHA(revision, 8) 
					+ " - " + getRepository().getFQN();
		} else {
			return getCommit().getSubject() + " - " + revision 
					+ " - " + getRepository().getFQN();
		}
	}
}
