package com.pmease.gitplex.web.component.diff.revision;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
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
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.util.lang.Objects;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.NullOutputStream;

import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobChange;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.LineProcessor;
import com.pmease.commons.lang.diff.DiffUtils;
import com.pmease.commons.wicket.ajaxlistener.IndicateLoadingListener;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.behavior.menu.CheckItem;
import com.pmease.commons.wicket.behavior.menu.MenuBehavior;
import com.pmease.commons.wicket.behavior.menu.MenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.diff.blob.BlobDiffPanel;
import com.pmease.gitplex.web.component.diff.diffstat.DiffStatBar;
import com.pmease.gitplex.web.component.pathselector.PathSelector;

@SuppressWarnings("serial")
public abstract class RevisionDiffPanel extends Panel {

	private static final String COOKIE_DIFF_MODE = "gitplex.diff.mode";
	
	private final IModel<Repository> repoModel;
	
	@Nullable
	private String path;
	
	private final String oldRev;
	
	private final String newRev;
	
	@Nullable
	private final InlineCommentSupport commentSupport;
	
	private LineProcessor lineProcessor = LineProcessOption.IGNORE_NOTHING;
	
	private DiffMode diffMode;
	
	private IModel<ChangesAndCount> changesAndCountModel = new LoadableDetachableModel<ChangesAndCount>() {

		@Override
		protected ChangesAndCount load() {
			try (	FileRepository jgitRepo = repoModel.getObject().openAsJGitRepo();
					DiffFormatter diffFormatter = new DiffFormatter(NullOutputStream.INSTANCE);) {
		    	diffFormatter.setRepository(jgitRepo);
		    	diffFormatter.setDetectRenames(true);
		    	
		    	if (path != null)
		    		diffFormatter.setPathFilter(PathFilter.create(path));
				AnyObjectId oldCommitId = repoModel.getObject().getObjectId(oldRev);
				AnyObjectId newCommitId = repoModel.getObject().getObjectId(newRev);
				List<DiffEntry> entries = diffFormatter.scan(oldCommitId, newCommitId);
				List<BlobChange> diffableChanges = new ArrayList<>();
		    	for (DiffEntry entry: diffFormatter.scan(oldCommitId, newCommitId)) {
		    		if (diffableChanges.size() < Constants.MAX_DIFF_FILES) {
			    		diffableChanges.add(new BlobChange(oldCommitId.name(), newCommitId.name(), entry) {
	
							@Override
							public Blob getBlob(BlobIdent blobIdent) {
								return repoModel.getObject().getBlob(blobIdent);
							}
	
							@Override
							public LineProcessor getLineProcessor() {
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
		    	for (final BlobChange change: diffableChanges) {
		    		tasks.add(new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							change.getDiffBlocks();
							return null;
						}
		    			
		    		});
		    	}
		    	for (Future<Void> future: GitPlex.getInstance(ForkJoinPool.class).invokeAll(tasks)) {
		    		try {
		    			// call get in order to throw exception if there is any during task execution
						future.get();
					} catch (InterruptedException|ExecutionException e) {
						throw new RuntimeException(e);
					}
		    	}

		    	int totalChanges = entries.size();
		    	if (diffableChanges.size() == totalChanges) { 
			    	// some changes should be removed if content is the same after line processing 
			    	for (Iterator<BlobChange> it = diffableChanges.iterator(); it.hasNext();) {
			    		BlobChange change = it.next();
			    		if (change.getType() == ChangeType.MODIFY 
			    				&& Objects.equal(change.getOldBlobIdent().mode, change.getNewBlobIdent().mode)
			    				&& change.getAdditions() + change.getDeletions() == 0) {
			    			Blob.Text oldText = change.getOldText();
			    			Blob.Text newText = change.getNewText();
			    			if (oldText != null && newText != null 
			    					&& (oldText.getLines().size() + newText.getLines().size()) <= DiffUtils.MAX_DIFF_SIZE) {
				    			it.remove();
			    			}
			    		}
			    	}
			    	totalChanges = diffableChanges.size();
		    	} 

		    	List<BlobChange> displayableChanges = new ArrayList<>();
		    	int totalChangedLines = 0;
		    	for (BlobChange change: diffableChanges) {
		    		int changedLines = change.getAdditions() + change.getDeletions(); 
		    		
		    		// we do not count large diff in a single file in order to 
		    		// display smaller diffs from different files as many as 
		    		// possible. 
		    		if (changedLines <= Constants.MAX_SINGLE_FILE_DIFF_LINES) {
			    		totalChangedLines += changedLines;
			    		if (totalChangedLines <= Constants.MAX_DIFF_LINES)
			    			displayableChanges.add(change);
			    		else
			    			break;
		    		} else {
		    			// large diff in a single file will not be displayed, so 
		    			// adding it to change list will do no harm, and can avoid 
		    			// displaying "too many changes" when some big text file 
		    			// is added/removed without touching too many files
		    			displayableChanges.add(change);
		    		}
		    	}
		    	return new ChangesAndCount(displayableChanges, totalChanges);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	};
	
	public RevisionDiffPanel(String id, IModel<Repository> repoModel, String oldRev, String newRev, 
			@Nullable String path, @Nullable InlineCommentSupport commentSupport) {
		super(id);
		
		this.repoModel = repoModel;
		this.oldRev = oldRev;
		this.newRev = newRev;
		this.path = path;
		this.commentSupport = commentSupport;
		
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(COOKIE_DIFF_MODE);
		if (cookie == null)
			diffMode = DiffMode.UNIFIED;
		else
			diffMode = DiffMode.valueOf(cookie.getValue());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		DropdownPanel pathDropdown = new DropdownPanel("pathDropdown", true) {

			@Override
			protected Component newContent(String id) {
				return new PathSelector(id, repoModel, newRev, FileMode.TYPE_TREE, 
						FileMode.TYPE_FILE, FileMode.TYPE_GITLINK, FileMode.TYPE_SYMLINK) {
					
					@Override
					protected void onSelect(AjaxRequestTarget target, BlobIdent blobIdent) {
						path = blobIdent.path;
						hide(target);
						target.add(RevisionDiffPanel.this);
						onPathChange(target, path);
					}

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						attributes.getAjaxCallListeners().add(new IndicateLoadingListener());
					}
					
				};
			}
			
		};
		add(pathDropdown);
		WebMarkupContainer pathContainer = new WebMarkupContainer("path");
		pathContainer.add(new DropdownBehavior(pathDropdown));
		pathContainer.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (path != null)
					return path;
				else
					return "Filter by";
			}
			
		}));
		add(new AjaxLink<Void>("clear") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				path = null;
				target.add(RevisionDiffPanel.this);
				onPathChange(target, path);
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new IndicateLoadingListener());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(path != null);
			}
			
		});
		add(pathContainer);
		
		MenuPanel lineProcessorMenu = new MenuPanel("lineProcessorMenu") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> menuItems = new ArrayList<>();
				
				for (final LineProcessOption option: LineProcessOption.values()) {
					menuItems.add(new CheckItem() {

						@Override
						protected String getLabel() {
							return option.getName();
						}

						@Override
						protected boolean isChecked() {
							return lineProcessor == option;
						}

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new IndicateLoadingListener());
						}

						@Override
						protected void onClick(AjaxRequestTarget target) {
							lineProcessor = option;
							target.add(RevisionDiffPanel.this);
						}
						
					});
				}

				return menuItems;
			}	
			
		};
		add(lineProcessorMenu);
		add(new WebMarkupContainer("lineProcessor").add(new MenuBehavior(lineProcessorMenu)));
		
		add(new Label("totalChanged", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return getChangesCount();
			}
			
		}));

		for (final DiffMode each: DiffMode.values()) {
			add(new AjaxLink<Void>(each.name().toLowerCase()) {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new IndicateLoadingListener());
				}
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					diffMode = each;
					WebResponse response = (WebResponse) RequestCycle.get().getResponse();
					Cookie cookie = new Cookie(COOKIE_DIFF_MODE, diffMode.name());
					cookie.setMaxAge(Integer.MAX_VALUE);
					response.addCookie(cookie);
					
					target.add(RevisionDiffPanel.this);
					target.focusComponent(null);
				}
				
			}.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

				@Override
				protected String load() {
					return each==diffMode?" active":"";
				}
				
			})));
		}
		
		add(new WebMarkupContainer("tooManyChanges") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getChanges().size() < getChangesCount());
			}
			
		});
		
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
				if (change.getType() == ChangeType.ADD)
					iconClass = " fa-ext fa-diff-added";
				else if (change.getType() == ChangeType.DELETE)
					iconClass = " fa-ext fa-diff-removed";
				else if (change.getType() == ChangeType.MODIFY)
					iconClass = " fa-ext fa-diff-modified";
				else
					iconClass = " fa-ext fa-diff-renamed";
				
				item.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", iconClass)));
				
				WebMarkupContainer pathLink = new WebMarkupContainer("path");
				pathLink.add(AttributeModifier.replace("href", "#diff-" + change.getPath()));
				pathLink.add(new Label("path", change.getPath()));
				
				item.add(pathLink);
				
				item.add(new Label("additions", "+" + change.getAdditions()));
				item.add(new Label("deletions", "-" + change.getDeletions()));
				
				boolean barVisible;
				if (change.getType() == ChangeType.ADD) {
					Blob.Text text = change.getNewText();
					barVisible = (text != null && text.getLines().size() <= DiffUtils.MAX_DIFF_SIZE);
				} else if (change.getType() == ChangeType.DELETE) {
					Blob.Text text = change.getOldText();
					barVisible = (text != null && text.getLines().size() <= DiffUtils.MAX_DIFF_SIZE);
				} else {
					Blob.Text oldText = change.getOldText();
					Blob.Text newText = change.getNewText();
					barVisible = (oldText != null && newText != null 
							&& oldText.getLines().size()+newText.getLines().size() <= DiffUtils.MAX_DIFF_SIZE);
				}
				item.add(new DiffStatBar("bar", change.getAdditions(), change.getDeletions(), false).setVisible(barVisible));
			}
			
		});
		
		add(new ListView<BlobChange>("changes", new AbstractReadOnlyModel<List<BlobChange>>() {

			@Override
			public List<BlobChange> getObject() {
				return getChanges();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<BlobChange> item) {
				BlobChange change = item.getModelObject();
				item.setMarkupId("diff-" + change.getPath());
				item.setOutputMarkupId(true);
				item.add(new BlobDiffPanel("change", repoModel, change, diffMode, commentSupport));
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	protected abstract void onPathChange(AjaxRequestTarget target, String path);
	
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
