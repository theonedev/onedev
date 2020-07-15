package io.onedev.server.web.page.project.blob.search.advanced;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.search.code.SearchManager;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.query.BlobQuery;
import io.onedev.server.search.code.query.FileQuery;
import io.onedev.server.search.code.query.SymbolQuery;
import io.onedev.server.search.code.query.TextQuery;
import io.onedev.server.search.code.query.TooGeneralQueryException;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.RunTaskBehavior;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.blob.search.result.SearchResultPanel;

@SuppressWarnings("serial")
public abstract class AdvancedSearchPanel extends Panel {

	private static final MetaDataKey<Class<? extends SearchOption>> ACTIVE_TAB = 
			new MetaDataKey<Class<? extends SearchOption>>(){};
	
	private static final MetaDataKey<HashMap<Class<?>, SearchOption>> SEARCH_OPTIONS = 
			new MetaDataKey<HashMap<Class<?>, SearchOption>>(){};
	
	private final IModel<Project> projectModel;
	
	private final IModel<String> revisionModel;
	
	private Form<?> form;
	
	private SearchOption option = new TextSearchOption();
	
	public AdvancedSearchPanel(String id, IModel<Project> projectModel, IModel<String> revisionModel) {
		super(id);
		
		this.projectModel = projectModel;
		this.revisionModel = revisionModel;
		
		Class<? extends SearchOption> activeTab = WebSession.get().getMetaData(ACTIVE_TAB);
		if (activeTab != null) {
			try {
				option = activeTab.newInstance();
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		add(form);
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		List<Tab> tabs = new ArrayList<Tab>();
		tabs.add(new AjaxActionTab(Model.of("Text occurrences")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				option = new TextSearchOption();
				onSelectTab(target);
			}
			
		}.setSelected(option instanceof TextSearchOption));
		
		tabs.add(new AjaxActionTab(Model.of("File names")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				option = new FileSearchOption();
				onSelectTab(target);
			}
			
		}.setSelected(option instanceof FileSearchOption));
		
		tabs.add(new AjaxActionTab(Model.of("Symbol names")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				option = new SymbolSearchOption();
				onSelectTab(target);
			}
			
		}.setSelected(option instanceof SymbolSearchOption));
		
		form.add(new Tabbable("tabs", tabs));

		form.add(newSearchOptionEditor(option));
		
		form.add(new AjaxButton("search") {

			private RunTaskBehavior runTaskBehavior;
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(runTaskBehavior = new RunTaskBehavior() {
					
					@Override
					protected void runTask(AjaxRequestTarget target) {
						List<QueryHit> hits;
						if (revisionModel.getObject() != null) {
							try {
								hits = option.query(AdvancedSearchPanel.this);
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
						} else {
							hits = new ArrayList<>();
						}
						
						HashMap<Class<?>, SearchOption> savedOptions = getSavedOptions();
						savedOptions.put(option.getClass(), option);
						WebSession.get().setMetaData(SEARCH_OPTIONS, savedOptions);
						
						onSearchComplete(target, hits);
					}
					
				});
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				runTaskBehavior.requestRun(target);
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
	}
	
	private void onSelectTab(AjaxRequestTarget target) {
		WebSession.get().setMetaData(ACTIVE_TAB, option.getClass());
		SearchOptionEditor editor = newSearchOptionEditor(option);
		form.replace(editor);
		target.add(editor);
	}
	
	private SearchOptionEditor newSearchOptionEditor(SearchOption option) {
		if (option instanceof SymbolSearchOption)
			return newSymbolSearchOptionEditor();
		else if (option instanceof FileSearchOption)
			return newFileSearchOptionEditor();
		else
			return newTextSearchOptionEditor();
	}
	
	private HashMap<Class<?>, SearchOption> getSavedOptions() {
		HashMap<Class<?>, SearchOption> savedOptions = WebSession.get().getMetaData(SEARCH_OPTIONS);
		if (savedOptions == null)
			savedOptions = new HashMap<>();
		return savedOptions;
	}
	
	private SearchOptionEditor newSymbolSearchOptionEditor() {
		return new SearchOptionEditor("symbolSearchFrag") {

			private TextField<String> termInput;
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				WebMarkupContainer termContainer = new WebMarkupContainer("term");
				add(termContainer);
				termInput = new TextField<String>("term", new PropertyModel<String>(option, "term"));
				termInput.add(new INullAcceptingValidator<String>() {

					@Override
					public void validate(IValidatable<String> validatable) {
						if (StringUtils.isBlank(validatable.getValue())) {
							validatable.error(new IValidationError() {
								
								@Override
								public Serializable getErrorMessage(IErrorMessageSource messageSource) {
									return "This field is required";
								}
								
							});
						} else {
							SymbolQuery query = new SymbolQuery.Builder().term(validatable.getValue()).count(1).build();
							try {
								query.asLuceneQuery();
							} catch (TooGeneralQueryException e) {
								validatable.error(new IValidationError() {
									
									@Override
									public Serializable getErrorMessage(IErrorMessageSource messageSource) {
										return "Search is too general";
									}
									
								});
							}
						}
					}
					
				});
				termContainer.add(termInput);
				termContainer.add(new FencedFeedbackPanel("feedback", termInput));
				termContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						if (termInput.hasErrorMessage())
							return " is-invalid";
						else
							return "";
					}
					
				}));
				
				add(new CheckBox("caseSensitive", new PropertyModel<Boolean>(option, "caseSensitive")));
				
				add(new TextField<String>("fileNames", new PropertyModel<String>(option, "fileNames")));

				add(new CheckBox("insideCurrentDir", new PropertyModel<Boolean>(option, "insideCurrentDir")) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						setVisible(getCurrentBlob().path != null);
					}
					
				});
				
			}

		};
	}
	
	private SearchOptionEditor newFileSearchOptionEditor() {
		return new SearchOptionEditor("fileSearchFrag") {

			private TextField<String> termInput;
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				WebMarkupContainer termContainer = new WebMarkupContainer("term");
				add(termContainer);
				termInput = new TextField<String>("term", new PropertyModel<String>(option, "term"));
				termInput.add(new INullAcceptingValidator<String>() {

					@Override
					public void validate(IValidatable<String> validatable) {
						if (StringUtils.isBlank(validatable.getValue())) {
							validatable.error(new IValidationError() {
								
								@Override
								public Serializable getErrorMessage(IErrorMessageSource messageSource) {
									return "This field is required";
								}
								
							});
						} else {
							FileQuery query = new FileQuery.Builder()
									.fileNames(validatable.getValue())
									.count(1)
									.build();
							try {
								query.asLuceneQuery();
							} catch (TooGeneralQueryException e) {
								validatable.error(new IValidationError() {
									
									@Override
									public Serializable getErrorMessage(IErrorMessageSource messageSource) {
										return "Search is too general";
									}
									
								});
							}
						}
					}
					
				});
				
				termContainer.add(termInput);
				termContainer.add(new FencedFeedbackPanel("feedback", termInput));
				termContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						if (termInput.hasErrorMessage())
							return " is-invalid";
						else
							return "";
					}
					
				}));
				
				add(new CheckBox("caseSensitive", new PropertyModel<Boolean>(option, "caseSensitive")));
				
				add(new CheckBox("insideCurrentDir", new PropertyModel<Boolean>(option, "insideCurrentDir")) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						setVisible(getCurrentBlob().path != null);
					}
					
				});
				
			}

		};
	}
	
	private SearchOptionEditor newTextSearchOptionEditor() {
		return new SearchOptionEditor("textSearchFrag") {

			private TextField<String> termInput;
			
			private CheckBox regexCheck;
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				WebMarkupContainer termContainer = new WebMarkupContainer("term");
				add(termContainer);
				termInput = new TextField<String>("term", new PropertyModel<String>(option, "term"));
				termInput.add(new INullAcceptingValidator<String>() {

					@Override
					public void validate(IValidatable<String> validatable) {
						if (StringUtils.isBlank(validatable.getValue())) {
							validatable.error(new IValidationError() {
								
								@Override
								public Serializable getErrorMessage(IErrorMessageSource messageSource) {
									return "This field is required";
								}
								
							});
						} else {
							boolean regex = regexCheck.getInput()!=null?true:false;
							TextQuery query = new TextQuery.Builder()
									.term(validatable.getValue()).regex(regex)
									.count(1)
									.build();
							try {
								if (regex)
									Pattern.compile(validatable.getValue());
								query.asLuceneQuery();
							} catch (PatternSyntaxException e) {
								validatable.error(new IValidationError() {
									
									@Override
									public Serializable getErrorMessage(IErrorMessageSource messageSource) {
										return "Invalid PCRE syntax";
									}
									
								});
							} catch (TooGeneralQueryException e) {
								validatable.error(new IValidationError() {
									
									@Override
									public Serializable getErrorMessage(IErrorMessageSource messageSource) {
										return "Search is too general";
									}
									
								});
							}
						}
					}
					
				});
				
				termContainer.add(termInput);
				termContainer.add(new FencedFeedbackPanel("feedback", termInput));
				termContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						if (termInput.hasErrorMessage())
							return " is-invalid";
						else
							return "";
					}
					
				}));
				
				add(regexCheck = new CheckBox("regex", new PropertyModel<Boolean>(option, "regex")));
				
				add(new CheckBox("wholeWord", new PropertyModel<Boolean>(option, "wholeWord")));
				
				add(new CheckBox("caseSensitive", new PropertyModel<Boolean>(option, "caseSensitive")));
				
				add(new TextField<String>("fileNames", new PropertyModel<String>(option, "fileNames")));
				
				add(new CheckBox("insideCurrentDir", new PropertyModel<Boolean>(option, "insideCurrentDir")) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						setVisible(getCurrentBlob().path != null);
					}
					
				});
				
			}

		};
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		revisionModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new AdvancedSearchResourceReference()));
	}

	protected abstract void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits);
	
	protected abstract void onCancel(AjaxRequestTarget target);

	@Nullable
	protected abstract BlobIdent getCurrentBlob();
	
	private class SearchOptionEditor extends Fragment {

		public SearchOptionEditor(String markupId) {
			super("searchOptions", markupId, AdvancedSearchPanel.this);

			Map<Class<?>, SearchOption> savedOptions = getSavedOptions();
			if (savedOptions.containsKey(option.getClass()))
				option = savedOptions.get(option.getClass());
		}
		
		@Override
		protected void onInitialize() {
			super.onInitialize();
			setOutputMarkupId(true);
		}
		
	}
	
	protected String getDirectory(boolean insideDir) {
		BlobIdent blobIdent = getCurrentBlob();
		if (blobIdent == null || blobIdent.path == null || !blobIdent.path.contains("/") || !insideDir) 
			return null;
		else if (blobIdent.isTree()) 
			return blobIdent.path;
		else 
			return StringUtils.substringBeforeLast(blobIdent.path, "/");
	}

	static interface SearchOption extends Serializable {
		List<QueryHit> query(AdvancedSearchPanel context) throws InterruptedException;
	}
	
	static class SymbolSearchOption implements SearchOption {
		private String term;
		
		private String fileNames;
		
		private boolean caseSensitive;
		
		private boolean insideCurrentDir;

		@Override
		public List<QueryHit> query(AdvancedSearchPanel context) throws InterruptedException {
			SearchManager searchManager = OneDev.getInstance(SearchManager.class);
			List<QueryHit> hits;
			BlobQuery query = new SymbolQuery.Builder()
					.term(term)
					.primary(true)
					.caseSensitive(caseSensitive)
					.directory(context.getDirectory(insideCurrentDir))
					.fileNames(fileNames)
					.count(SearchResultPanel.MAX_QUERY_ENTRIES)
					.build();
			ObjectId commit = context.projectModel.getObject().getRevCommit(context.revisionModel.getObject(), true);
			hits = searchManager.search(context.projectModel.getObject(), commit, query);
			
			if (hits.size() < SearchResultPanel.MAX_QUERY_ENTRIES) {
				query = new SymbolQuery.Builder()
						.term(term)
						.primary(false)
						.caseSensitive(caseSensitive)
						.directory(context.getDirectory(insideCurrentDir))
						.fileNames(fileNames)
						.count(SearchResultPanel.MAX_QUERY_ENTRIES - hits.size())
						.build();
				hits.addAll(searchManager.search(context.projectModel.getObject(), commit, query));
			}
			
			return hits;
		}
		
	}
	
	static class FileSearchOption implements SearchOption {
		
		private String term;
		
		private boolean caseSensitive;
		
		private boolean insideCurrentDir;

		@Override
		public List<QueryHit> query(AdvancedSearchPanel context) throws InterruptedException {
			SearchManager searchManager = OneDev.getInstance(SearchManager.class);
			BlobQuery query = new FileQuery.Builder()
					.fileNames(term)
					.caseSensitive(caseSensitive) 
					.directory(context.getDirectory(insideCurrentDir))
					.count(SearchResultPanel.MAX_QUERY_ENTRIES)
					.build();
			ObjectId commit = context.projectModel.getObject().getRevCommit(context.revisionModel.getObject(), true);
			return searchManager.search(context.projectModel.getObject(), commit, query);
		}
		
	}
	
	static class TextSearchOption implements SearchOption {
		private String term;
		
		private boolean regex;
		
		private boolean caseSensitive;
		
		private boolean wholeWord;
		
		private String fileNames;
		
		private boolean insideCurrentDir;

		@Override
		public List<QueryHit> query(AdvancedSearchPanel context) throws InterruptedException {
			SearchManager searchManager = OneDev.getInstance(SearchManager.class);
			BlobQuery query = new TextQuery.Builder()
					.term(term)
					.regex(regex)
					.caseSensitive(caseSensitive)
					.wholeWord(wholeWord)
					.directory(context.getDirectory(insideCurrentDir))
					.fileNames(fileNames)
					.count(SearchResultPanel.MAX_QUERY_ENTRIES)
					.build();
			ObjectId commit = context.projectModel.getObject().getRevCommit(context.revisionModel.getObject(), true);
			return searchManager.search(context.projectModel.getObject(), commit, query);
		}
		
	}
	
}