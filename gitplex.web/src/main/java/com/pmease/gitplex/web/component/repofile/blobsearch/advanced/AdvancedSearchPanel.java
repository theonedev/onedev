package com.pmease.gitplex.web.component.repofile.blobsearch.advanced;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.codec.Base64;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
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
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.RunTaskBehavior;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.commons.wicket.component.tabbable.AjaxActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.SearchManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.BlobQuery;
import com.pmease.gitplex.search.query.FileQuery;
import com.pmease.gitplex.search.query.SymbolQuery;
import com.pmease.gitplex.search.query.TextQuery;
import com.pmease.gitplex.search.query.TooGeneralQueryException;
import com.pmease.gitplex.web.component.repofile.blobsearch.result.SearchResultPanel;

@SuppressWarnings("serial")
public abstract class AdvancedSearchPanel extends Panel {

	private static final String COOKIE_SEARCH_TYPE = "blob.search.advanced.type";
	
	private static final Logger logger = LoggerFactory.getLogger(AdvancedSearchPanel.class);
	
	private final IModel<Repository> repoModel;
	
	private final IModel<String> revisionModel;
	
	private Form<?> form;
	
	private SearchOption option = new TextSearchOption();
	
	public AdvancedSearchPanel(String id, IModel<Repository> repoModel, IModel<String> revisionModel) {
		super(id);
		
		this.repoModel = repoModel;
		this.revisionModel = revisionModel;
		
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(COOKIE_SEARCH_TYPE);
		if (cookie != null) {
			try {
				option = (SearchOption) Class.forName(cookie.getValue()).newInstance();
			} catch (Exception e) {
				logger.debug("Error restoring search option from cookie", e);
			}
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		add(form);
		
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
		
		form.add(new AjaxSubmitLink("search") {

			private RunTaskBehavior runTaskBehavior;
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(runTaskBehavior = new RunTaskBehavior() {
					
					@Override
					protected void runTask(AjaxRequestTarget target) {
						List<QueryHit> hits;
						try {
							hits = option.query(AdvancedSearchPanel.this);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
						WebResponse response = (WebResponse) RequestCycle.get().getResponse();
						byte[] bytes = SerializationUtils.serialize(option);
						Cookie cookie = new Cookie(option.getClass().getName(), Base64.encodeToString(bytes));
						cookie.setMaxAge(Integer.MAX_VALUE);
						response.addCookie(cookie);
						
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
		WebResponse response = (WebResponse) RequestCycle.get().getResponse();
		Cookie cookie = new Cookie(COOKIE_SEARCH_TYPE, option.getClass().getName());
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);
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
							SymbolQuery query = new SymbolQuery(validatable.getValue(), true, false, null, null, 0);
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
				termContainer.add(new FeedbackPanel("feedback", termInput));
				termContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						if (termInput.hasErrorMessage())
							return " has-error";
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
							FileQuery query = new FileQuery(validatable.getValue(), false, null, 0);
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
				termContainer.add(new FeedbackPanel("feedback", termInput));
				termContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						if (termInput.hasErrorMessage())
							return " has-error";
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
							TextQuery query = new TextQuery(validatable.getValue(), regex, false, false, null, null, 0);
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
				termContainer.add(new FeedbackPanel("feedback", termInput));
				termContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						if (termInput.hasErrorMessage())
							return " has-error";
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
		repoModel.detach();
		revisionModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(AdvancedSearchPanel.class, "advanced-search.css")));
	}

	protected abstract void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits);
	
	protected abstract void onCancel(AjaxRequestTarget target);

	@Nullable
	protected abstract BlobIdent getCurrentBlob();
	
	private class SearchOptionEditor extends Fragment {

		public SearchOptionEditor(String markupId) {
			super("searchOptions", markupId, AdvancedSearchPanel.this);
			
			WebRequest request = (WebRequest) RequestCycle.get().getRequest();
			Cookie cookie = request.getCookie(option.getClass().getName());
			if (cookie != null) {
				try {
					byte[] bytes = Base64.decode(cookie.getValue());
					option = (SearchOption) SerializationUtils.deserialize(bytes);
				} catch (Exception e) {
					logger.debug("Error restoring search option from cookie", e);
				}
			} 
		}
		
		@Override
		protected void onInitialize() {
			super.onInitialize();
			setOutputMarkupId(true);
		}
		
	}
	
	protected String getDirectory(boolean insideDir) {
		BlobIdent blobIdent = getCurrentBlob();
		if (blobIdent == null || blobIdent.path == null || !insideDir) 
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
			SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
			List<QueryHit> hits;
			BlobQuery query = new SymbolQuery(term, true, caseSensitive, 
					context.getDirectory(insideCurrentDir), fileNames, 
					SearchResultPanel.MAX_QUERY_ENTRIES);
			hits = searchManager.search(context.repoModel.getObject(), context.revisionModel.getObject(), query);
			
			if (hits.size() < SearchResultPanel.MAX_QUERY_ENTRIES) {
				query = new SymbolQuery(term, false, caseSensitive, 
						context.getDirectory(insideCurrentDir), fileNames, 
						SearchResultPanel.MAX_QUERY_ENTRIES - hits.size());
				hits.addAll(searchManager.search(context.repoModel.getObject(), context.revisionModel.getObject(), query));
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
			SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
			BlobQuery query = new FileQuery(term, caseSensitive, 
					context.getDirectory(insideCurrentDir), SearchResultPanel.MAX_QUERY_ENTRIES);
			return searchManager.search(context.repoModel.getObject(), context.revisionModel.getObject(), query);
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
			SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
			BlobQuery query = new TextQuery(term, regex, caseSensitive, wholeWord, 
					context.getDirectory(insideCurrentDir), fileNames, SearchResultPanel.MAX_QUERY_ENTRIES);
			return searchManager.search(context.repoModel.getObject(), context.revisionModel.getObject(), query);
		}
		
	}
	
}