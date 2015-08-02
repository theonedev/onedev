package com.pmease.gitplex.web.component.diff.revision;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.util.io.NullOutputStream;

import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobChange;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.LineProcessor;
import com.pmease.commons.wicket.behavior.menu.CheckItem;
import com.pmease.commons.wicket.behavior.menu.MenuBehavior;
import com.pmease.commons.wicket.behavior.menu.MenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.diff.diffstat.DiffStatBar;

@SuppressWarnings("serial")
public class RevisionDiffPanel extends Panel {

	private static final int MAX_DISPLAY_CHANGES = 500;
	
	private final IModel<Repository> repoModel;
	
	private final String oldRev;
	
	private final String newRev;
	
	private final InlineCommentSupport commentSupport;
	
	private LineProcessOption lineProcessor = LineProcessOption.IGNORE_NOTHING;

	private IModel<ChangesAndCount> changesAndCountModel = new LoadableDetachableModel<ChangesAndCount>() {

		@Override
		protected ChangesAndCount load() {
			try (	FileRepository jgitRepo = repoModel.getObject().openAsJGitRepo();
					DiffFormatter diffFormatter = new DiffFormatter(NullOutputStream.INSTANCE);) {
		    	diffFormatter.setRepository(jgitRepo);
				AnyObjectId oldCommitId = repoModel.getObject().getObjectId(oldRev);
				AnyObjectId newCommitId = repoModel.getObject().getObjectId(newRev);
				List<DiffEntry> entries = diffFormatter.scan(oldCommitId, newCommitId);
				List<BlobChange> changes = new ArrayList<>();
		    	for (DiffEntry entry: diffFormatter.scan(oldCommitId, newCommitId)) {
		    		if (changes.size() < MAX_DISPLAY_CHANGES) {
			    		changes.add(new BlobChange(oldCommitId.name(), newCommitId.name(), entry) {
	
							@Override
							protected Blob getBlob(BlobIdent blobIdent) {
								return repoModel.getObject().getBlob(blobIdent);
							}
	
							@Override
							protected LineProcessor getLineProcessor() {
								return lineProcessor;
							}
			    			
			    		});
		    		} else {
		    			break;
		    		}
		    	}

		    	// Diff calculation can be slow, so we pre-load diffs of each change 
		    	// concurrently
		    	Collection<Callable<Void>> tasks = new ArrayList<>();
		    	for (final BlobChange change: changes) {
		    		// to avoid race conditions during concurrent diff calculation, we
		    		// pre-populate blob data in repository 
		    		/*
		    		if (change.getOldBlobIdent().path != null)
		    			repoModel.getObject().getBlob(change.getOldBlobIdent());
		    		if (change.getNewBlobIdent().path != null)
		    			repoModel.getObject().getBlob(change.getNewBlobIdent());
		    		*/
		    		
		    		tasks.add(new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							change.getDiffs();
							return null;
						}
		    			
		    		});
		    	}
		    	
		    	GitPlex.getInstance(ForkJoinPool.class).invokeAll(tasks);
		    	
		    	if (changes.size() == entries.size()) { 
			    	// some changes should be removed if content is the same after line processing 
			    	for (Iterator<BlobChange> it = changes.iterator(); it.hasNext();) {
			    		BlobChange change = it.next();
			    		if (change.getChangeType() == ChangeType.MODIFY 
			    				&& repoModel.getObject().getBlob(change.getOldBlobIdent()).getText() != null
			    				&& repoModel.getObject().getBlob(change.getNewBlobIdent()).getText() != null
			    				&& change.getDiffs().isEmpty()) {
			    			it.remove();
			    		}
			    	}
			    	return new ChangesAndCount(changes, changes.size());
		    	} else {
		    		/*
		    		 * line processing will not apply if we have too many changes as:
		    		 * 1. we do not want to diff all changes for line processing
		    		 * 2. even for the first MAX_DISPLAY_CHANGES, we do not want to apply 
		    		 * 	  line processing as we can not determine the total changes count 
		    		 * 	  after line processing
		    		 */
		    		return new ChangesAndCount(changes, entries.size());
		    	}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	};
	
	public RevisionDiffPanel(String id, IModel<Repository> repoModel, String oldRev, String newRev, 
			InlineCommentSupport commentSupport) {
		super(id);
		
		this.oldRev = oldRev;
		this.newRev = newRev;
		this.repoModel = repoModel;
		this.commentSupport = commentSupport;
		
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("totalChanged", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return getChangesCount();
			}
			
		}));
		
		add(new Label("totalAdditions", new LoadableDetachableModel<Integer>() {

			@Override
			protected Integer load() {
				int totalAdditions = 0;
				for (BlobChange change: getChanges())
					totalAdditions += change.getAdditions();
				return totalAdditions;
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getChanges().size() == getChangesCount());
			}
			
		});
		
		add(new Label("totalDeletions", new LoadableDetachableModel<Integer>() {

			@Override
			protected Integer load() {
				int totalDeletions = 0;
				for (BlobChange change: getChanges())
					totalDeletions += change.getDeletions();
				return totalDeletions;
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getChanges().size() == getChangesCount());
			}
			
		});
		
		MenuPanel diffOptionMenuPanel = new MenuPanel("diffOptions") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> menuItems = new ArrayList<>();
				
				for (final LineProcessOption option: LineProcessOption.values()) {
					menuItems.add(new CheckItem() {

						@Override
						protected String getLabel() {
							return option.toString();
						}

						@Override
						protected boolean isTicked() {
							return lineProcessor == option;
						}

						@Override
						protected void onClick(AjaxRequestTarget target) {
							lineProcessor = option;
							hide(target);
							target.add(RevisionDiffPanel.this);
						}
						
					});
				}

				return menuItems;
			}
			
		};
		
		add(diffOptionMenuPanel);
		
		add(new WebMarkupContainer("diffOptionsTrigger") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getChanges().size() == getChangesCount());
			}
			
		}.add(new MenuBehavior(diffOptionMenuPanel)));
		
		add(new ListView<BlobChange>("diffStats", new AbstractReadOnlyModel<List<BlobChange>>() {

			@Override
			public List<BlobChange> getObject() {
				return getChanges();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<BlobChange> item) {
				BlobChange change = item.getModelObject();
				String iconClass;
				if (change.getChangeType() == ChangeType.ADD)
					iconClass = " fa-ext fa-diff-added";
				else if (change.getChangeType() == ChangeType.DELETE)
					iconClass = " fa-ext fa-diff-removed";
				else if (change.getChangeType() == ChangeType.MODIFY)
					iconClass = " fa-ext fa-diff-modified";
				else
					iconClass = " fa-ext fa-diff-renamed";
				
				item.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", iconClass)));
				
				WebMarkupContainer pathLink = new WebMarkupContainer("path");
				pathLink.add(AttributeModifier.replace("href", "#" + change.getPath()));
				pathLink.add(new Label("path", change.getPath()));
				
				item.add(pathLink);
				
				item.add(new Label("additions", change.getAdditions()));
				item.add(new Label("deletions", change.getDeletions()));
				item.add(new DiffStatBar("bar", change.getAdditions(), change.getDeletions()));
			}
			
		});
	}

	private List<BlobChange> getChanges() {
		return changesAndCountModel.getObject().getChanges();
	}
	
	private int getChangesCount() {
		return changesAndCountModel.getObject().getCount();
	}
	
	@Override
	protected void onDetach() {
		changesAndCountModel.detach();
		repoModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(RevisionDiffPanel.class, "revision-diff.css")));
	}

	private static class ChangesAndCount {
		
		private final List<BlobChange> changes;
		
		private final int count;
		
		public ChangesAndCount(List<BlobChange> changes, int count) {
			this.changes = changes;
			this.count =  count;
		}

		public List<BlobChange> getChanges() {
			return changes;
		}

		public int getCount() {
			return count;
		}
		
	}
}
