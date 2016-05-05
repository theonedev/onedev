package com.pmease.gitplex.web.component.diff.revision;

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
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.AbstractLink;
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
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pmease.commons.antlr.codeassist.InputCompletion;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobChange;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.WhitespaceOption;
import com.pmease.commons.lang.diff.DiffUtils;
import com.pmease.commons.util.Range;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.util.match.WildcardUtils;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.assets.clearable.ClearableResourceReference;
import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.commons.wicket.assets.uri.URIResourceReference;
import com.pmease.commons.wicket.behavior.inputassist.InputAssistBehavior;
import com.pmease.commons.wicket.component.menu.MenuItem;
import com.pmease.commons.wicket.component.menu.MenuLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.diff.blob.BlobDiffPanel;
import com.pmease.gitplex.web.component.diff.diffstat.DiffStatBar;
import com.pmease.gitplex.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public abstract class RevisionDiffPanel extends Panel {

	private static final String COOKIE_VIEW_MODE = "gitplex.diff.viewmode";
	
	private final IModel<Depot> depotModel;
	
	private final IModel<PullRequest> requestModel;

	private final String oldRev;
	
	private final String newRev;
	
	private String pathFilter;
	
	private WhitespaceOption whitespaceOption;
	
	private DiffViewMode diffMode;
	
	private IModel<List<DiffEntry>> diffEntriesModel = new LoadableDetachableModel<List<DiffEntry>>() {

		@Override
		protected List<DiffEntry> load() {
			String oldCommitHash = depotModel.getObject().getObjectId(oldRev).name();
			String newCommitHash = depotModel.getObject().getObjectId(newRev).name();
			return depotModel.getObject().getDiffs(oldCommitHash, newCommitHash);
		}
		
	};
	
	private IModel<ChangesAndCount> changesAndCountModel = new LoadableDetachableModel<ChangesAndCount>() {

		@Override
		protected ChangesAndCount load() {
			List<DiffEntry> diffEntries = diffEntriesModel.getObject();
			
			List<BlobChange> filterChanges = new ArrayList<>();
	    	for (DiffEntry entry: diffEntries) {
    			BlobChange change = new BlobChange(oldRev, newRev, entry, whitespaceOption) {

					@Override
					public Blob getBlob(BlobIdent blobIdent) {
						return depotModel.getObject().getBlob(blobIdent);
					}

	    		};
	    		if (StringUtils.isNotBlank(pathFilter)) {
		    		String matchWith = pathFilter.toLowerCase().trim();
	    			matchWith = StringUtils.stripStart(matchWith, "/");
	    			matchWith = StringUtils.stripEnd(matchWith, "/");
	    			String path = change.getPath().toLowerCase();
	    			if (matchWith.equals(path)) {
	    				filterChanges.add(change);
	    			} else if (path.startsWith(matchWith + "/")) {
	    				filterChanges.add(change);
	    			} else if (WildcardUtils.matchString(matchWith, path)){
	    				filterChanges.add(change);
	    			}
	    		} else {
	    			filterChanges.add(change);
	    		}
	    	}
	    	
			List<BlobChange> diffChanges = new ArrayList<>();
			if (filterChanges.size() > Constants.MAX_DIFF_FILES)
				diffChanges = filterChanges.subList(0, Constants.MAX_DIFF_FILES);
			else
				diffChanges = filterChanges;
			
	    	// Diff calculation can be slow, so we pre-load diffs of each change 
	    	// concurrently
	    	Collection<Callable<Void>> tasks = new ArrayList<>();
	    	for (BlobChange change: diffChanges) {
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
	    	
	    	int totalChanges = filterChanges.size();
	    	if (diffChanges.size() == totalChanges) { 
		    	// some changes should be removed if content is the same after line processing 
		    	for (Iterator<BlobChange> it = diffChanges.iterator(); it.hasNext();) {
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
		    	totalChanges = diffChanges.size();
	    	} 
	    	
	    	List<BlobChange> displayChanges = new ArrayList<>();
	    	int totalChangedLines = 0;
	    	for (BlobChange change: diffChanges) {
	    		int changedLines = change.getAdditions() + change.getDeletions(); 
	    		/*
	    		 * we do not count large diff in a single file in order to
	    		 * display smaller diffs from different files as many as
	    		 * possible
	    		 */
	    		if (changedLines <= Constants.MAX_SINGLE_FILE_DIFF_LINES) {
		    		totalChangedLines += changedLines;
		    		if (totalChangedLines <= Constants.MAX_DIFF_LINES)
		    			displayChanges.add(change);
		    		else
		    			break;
	    		} else {
	    			/*
	    			 * large diff in a single file will not be displayed, but 
	    			 * we still add it into the list as otherwise we may 
	    			 * incorrectly display the "too many changed files" warning  
	    			 */
	    			displayChanges.add(change);
	    		}
	    	}
	    	return new ChangesAndCount(displayChanges, totalChanges);
		}
	};
	
	public RevisionDiffPanel(String id, IModel<Depot> depotModel, IModel<PullRequest> requestModel, 
			String oldRev, String newRev, @Nullable String pathFilter, 
			WhitespaceOption whitespaceOption) {
		super(id);
		
		this.depotModel = depotModel;
		this.requestModel = requestModel;
		this.oldRev = oldRev;
		this.newRev = newRev;
		this.pathFilter = pathFilter;
		this.whitespaceOption = whitespaceOption;
		
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(COOKIE_VIEW_MODE);
		if (cookie == null)
			diffMode = DiffViewMode.UNIFIED;
		else
			diffMode = DiffViewMode.valueOf(cookie.getValue());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer body = new WebMarkupContainer("body");
		body.setOutputMarkupId(true);
		add(body);
		
		for (DiffViewMode each: DiffViewMode.values()) {
			add(new AjaxLink<Void>(each.name().toLowerCase()) {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(body));
				}
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					diffMode = each;
					WebResponse response = (WebResponse) RequestCycle.get().getResponse();
					Cookie cookie = new Cookie(COOKIE_VIEW_MODE, diffMode.name());
					cookie.setMaxAge(Integer.MAX_VALUE);
					response.addCookie(cookie);
					target.add(RevisionDiffPanel.this);
				}
				
			}.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

				@Override
				protected String load() {
					return each==diffMode?" active":"";
				}
				
			})));
		}
		
		add(new MenuLink("whitespaceOption") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> menuItems = new ArrayList<>();
				
				for (WhitespaceOption each: WhitespaceOption.values()) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return each.getDescription();
						}

						@Override
						public String getIconClass() {
							if (whitespaceOption == each)
								return "fa fa-check";
							else
								return null;
						}

						@Override
						public AbstractLink newLink(String id) {
							return new AjaxLink<Void>(id) {

								@Override
								public void onClick(AjaxRequestTarget target) {
									close();
									whitespaceOption = each;
									target.add(body);
									onWhitespaceOptionChange(target, whitespaceOption);
								}

								@Override
								protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
									super.updateAjaxAttributes(attributes);
									attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(body));
								}
								
							};
						}
						
					});
				}

				return menuItems;
			}
			
		});
		
		Form<?> form = new Form<Void>("pathFilter");
		TextField<String> filterInput;
		form.add(filterInput = new TextField<String>("input", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return pathFilter;
			}

			@Override
			public void setObject(String object) {
				pathFilter = object;
			}
			
		}));
		
		List<String> diffPaths = new ArrayList<>();
		for (DiffEntry diffEntry: diffEntriesModel.getObject()) {
			if (diffEntry.getChangeType() == DiffEntry.ChangeType.ADD) {
				diffPaths.add(diffEntry.getNewPath());
			} else if (diffEntry.getChangeType() == DiffEntry.ChangeType.COPY) {
				diffPaths.add(diffEntry.getNewPath());
				diffPaths.add(diffEntry.getOldPath());
			} else if (diffEntry.getChangeType() == DiffEntry.ChangeType.DELETE) {
				diffPaths.add(diffEntry.getOldPath());
			} else if (diffEntry.getChangeType() == DiffEntry.ChangeType.MODIFY) {
				diffPaths.add(diffEntry.getNewPath());
			} else if (diffEntry.getChangeType() == DiffEntry.ChangeType.RENAME) {
				diffPaths.add(diffEntry.getNewPath());
				diffPaths.add(diffEntry.getOldPath());
			} else {
				throw new IllegalStateException();
			}
		}
		
		filterInput.add(new InputAssistBehavior() {
			
			@Override
			protected List<InputCompletion> getSuggestions(InputStatus inputStatus, int count) {
				List<InputCompletion> completions = new ArrayList<>();
				for (InputSuggestion suggestion: SuggestionUtils.suggestPath(diffPaths, 
						inputStatus.getContentBeforeCaret().trim(), count)) {
					int caret = suggestion.getCaret();
					if (caret == -1)
						caret = suggestion.getContent().length();
					InputCompletion completion = new InputCompletion(0, inputStatus.getContent().length(), 
							suggestion.getContent(), caret, suggestion.getLabel(), 
							null, suggestion.getMatchRange());
					completions.add(completion);
				}
				return completions;
			}
			
			@Override
			protected List<String> getHints(InputStatus inputStatus) {
				return Lists.newArrayList("Use * to match any string in the path");
			}

			@Override
			protected List<Range> getErrors(String inputContent) {
				return null;
			}
			
			@Override
			protected int getAnchor(String content) {
				for (int i=0; i<content.length(); i++) {
					if (!Character.isWhitespace(content.charAt(i)))
						return i;
				}
				return content.length();
			}
			
		});
		
		form.add(new AjaxButton("submit") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(body));
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				Preconditions.checkNotNull(target);
				target.add(body);
				onPathFilterChange(target, pathFilter);
			}
			
		});
		add(form);
 		
		Component totalChangedLink;
		body.add(totalChangedLink = new Label("totalChanged", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return changesAndCountModel.getObject().getChanges().size() + " changed files ";
			}
			
		}));

		body.add(new WebMarkupContainer("tooManyChanges") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				ChangesAndCount changesAndCount = changesAndCountModel.getObject();
				setVisible(changesAndCount.getChanges().size() < changesAndCount.getCount());
			}
			
		});
		
		WebMarkupContainer diffStats = new WebMarkupContainer("diffStats");
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie("revisionDiff.showDiffStats");
		if (cookie == null || !"yes".equals(cookie.getValue())) {
			diffStats.add(AttributeAppender.append("style", "display:none;"));
		} else {
			totalChangedLink.add(AttributeAppender.append("class", "expanded"));			
		}
		body.add(diffStats);
		diffStats.add(new ListView<BlobChange>("diffStats", new AbstractReadOnlyModel<List<BlobChange>>() {

			@Override
			public List<BlobChange> getObject() {
				return changesAndCountModel.getObject().getChanges();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<BlobChange> item) {
				BlobChange change = item.getModelObject();
				String iconClass;
				if (change.getType() == ChangeType.ADD || change.getType() == ChangeType.COPY)
					iconClass = " fa-ext fa-diff-added";
				else if (change.getType() == ChangeType.DELETE)
					iconClass = " fa-ext fa-diff-removed";
				else if (change.getType() == ChangeType.MODIFY)
					iconClass = " fa-ext fa-diff-modified";
				else
					iconClass = " fa-ext fa-diff-renamed";
				
				item.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", iconClass)));
				
				WebMarkupContainer fileLink = new WebMarkupContainer("file");
				fileLink.add(AttributeModifier.replace("data-file", change.getPath()));
				fileLink.add(new Label("name", change.getPath()));
				
				item.add(fileLink);
				
				item.add(new Label("additions", "+" + change.getAdditions()));
				item.add(new Label("deletions", "-" + change.getDeletions()));
				
				boolean barVisible;
				if (change.getType() == ChangeType.ADD || change.getType() == ChangeType.COPY) {
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
		
		body.add(new ListView<BlobChange>("changes", new AbstractReadOnlyModel<List<BlobChange>>() {

			@Override
			public List<BlobChange> getObject() {
				return changesAndCountModel.getObject().getChanges();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<BlobChange> item) {
				BlobChange change = item.getModelObject();
				item.add(AttributeAppender.append("data-file", change.getPath()));
				item.add(new BlobDiffPanel("change", depotModel, requestModel, change, diffMode));
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	@Override
	protected void onDetach() {
		diffEntriesModel.detach();
		changesAndCountModel.detach();
		depotModel.detach();
		requestModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(URIResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(ClearableResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(CookiesResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(RevisionDiffPanel.class, "revision-diff.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RevisionDiffPanel.class, "revision-diff.css")));

		String script = String.format("gitplex.revisionDiff.init(%s);", 
				RequestCycle.get().find(AjaxRequestTarget.class) == null);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	protected abstract void onPathFilterChange(AjaxRequestTarget target, String pathFilter);
	
	protected abstract void onWhitespaceOptionChange(AjaxRequestTarget target, 
			WhitespaceOption whitespaceOption);
	
	private static class ChangesAndCount {
		
		private final List<BlobChange> changes;
		
		private final int count;
		
		public ChangesAndCount(List<BlobChange> changes, int count) {
			this.changes = changes;
			this.count = count;
		}
		
		/**
		 * Get list of changes we are capable to handle, note that size of this list 
		 * might be less than total number of changes in order not to put heavy 
		 * burden on the system and browser
		 * 
		 * @return
		 * 			list of changes we are capable to handle
		 */
		public List<BlobChange> getChanges() {
			return changes;
		}

		/**
		 * Get total number of changes detected
		 * 
		 * @return
		 * 			total number of changes detected
		 */
		public int getCount() {
			return count;
		}
		
	}
}
