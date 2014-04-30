package com.pmease.gitop.web.page.repository.source.commit;

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
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.common.wicket.bootstrap.Icon;
import com.pmease.gitop.web.component.commit.CommitMetaPanel;
import com.pmease.gitop.web.component.link.AvatarLink.Mode;
import com.pmease.gitop.web.component.link.PersonLink;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.git.command.CommitInCommand;
import com.pmease.gitop.web.git.command.CommitInCommand.RefType;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.repository.RepositoryPage;
import com.pmease.gitop.web.page.repository.source.commit.diff.CommentListPanel;
import com.pmease.gitop.web.page.repository.source.commit.diff.CommitCommentsAware;
import com.pmease.gitop.web.page.repository.source.commit.diff.DiffViewPanel;
import com.pmease.gitop.web.page.repository.source.tree.SourceTreePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class SourceCommitPage extends RepositoryPage implements CommitCommentsAware {
	
	public static PageParameters newParams(Repository repository, String revision) {
		PageParameters params = PageSpec.forRepository(repository);
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
				Repository repository = getRepository();
				return repository.git().showRevision(revision);
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
				.query(Restrictions.eq("repository", getRepository()),
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
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("shortmessage", new PropertyModel<String>(commitModel, "subject")));
		add(new Label("detailedmessage", new PropertyModel<String>(commitModel, "message")));
		
		add(new BookmarkablePageLink<Void>("treelink",
				SourceTreePage.class,
				SourceTreePage.newParams(getRepository(), getRevision())));
		
		TooltipConfig tooltipConfig = new TooltipConfig().withPlacement(TooltipConfig.Placement.left);
		add(new PersonLink("authoravatar", getCommit().getAuthor().getPerson(), Mode.AVATAR)
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
				
				AbstractLink link = new BookmarkablePageLink<Void>("link", SourceCommitPage.class,
						SourceCommitPage.newParams(getRepository(), sha));
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
		add(new DiffViewPanel("diffs", repositoryModel, Model.of(getSince()), Model.of(getUntil())));
		
		add(new CommentListPanel("comments", repositoryModel, new AbstractReadOnlyModel<String>() {

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
								SourceTreePage.newParams(getRepository(), branchName));
						
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
				+ " - " + getRepository().getPathName();
	}
}
