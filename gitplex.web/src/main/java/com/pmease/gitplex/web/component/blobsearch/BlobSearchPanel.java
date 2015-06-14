package com.pmease.gitplex.web.component.blobsearch;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.behavior.RunTaskBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.behavior.modal.ModalBehavior;
import com.pmease.commons.wicket.behavior.modal.ModalPanel;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.IndexConstants;
import com.pmease.gitplex.search.SearchManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.BlobQuery;
import com.pmease.gitplex.search.query.SymbolQuery;
import com.pmease.gitplex.search.query.TextQuery;
import com.pmease.gitplex.search.query.TooGeneralQueryException;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;

@SuppressWarnings("serial")
public abstract class BlobSearchPanel extends Panel {

	public static final int ADVANCED_QUERY_ENTRIES = 1000;
	
	/*
	 * Entries to query is much more than entries to be displayed in order to 
	 * cover important symbols (for instant types are important than fields...)
	 * so that they can be sorted to be displayed first  
	 */
	private static final int SYMBOL_QUERY_ENTRIES = 300;
	
	private static final int SYMBOL_DISPLAY_ENTRIES = 15;
	
	private static final int TEXT_QUERY_ENTRIES = 15;
	
	private final IModel<Repository> repoModel;
	
	private final IModel<String> revisionModel;
	
	private TextField<String> instantSearchField;
	
	private String instantSearchInput;
	
	private DropdownPanel instantSearchDropdown;
	
	private WebMarkupContainer instantSearchResult;
	
	private List<QueryHit> symbolHits;
	
	private List<QueryHit> textHits;
	
	private RunTaskBehavior moreSymbolHitsBehavior;
	
	private RunTaskBehavior moreTextHitsBehavior;
	
	private int activeHitIndex;
	
	public BlobSearchPanel(String id, IModel<Repository> repoModel, IModel<String> revisionModel) {
		super(id);
		
		this.repoModel = repoModel;
		this.revisionModel = revisionModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		instantSearchDropdown = new DropdownPanel("instantSearchDropdown") {

			@Override
			protected Component newContent(String id) {
				instantSearchResult = new Fragment(id, "instantSearchResultFrag", BlobSearchPanel.this);
				instantSearchResult.setOutputMarkupId(true);
				
				instantSearchResult.add(new ListView<QueryHit>("symbolHits", new AbstractReadOnlyModel<List<QueryHit>>() {

					@Override
					public List<QueryHit> getObject() {
						return symbolHits;
					}
					
				}) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!symbolHits.isEmpty());
					}

					@Override
					protected void populateItem(ListItem<QueryHit> item) {
						final QueryHit hit = item.getModelObject();
						AjaxLink<Void> link = new AjaxLink<Void>("link") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								selectHit(target, hit);
							}
							
						};
						link.add(new Image("icon", hit.getIcon()) {

							@Override
							protected boolean shouldAddAntiCacheParameter() {
								return false;
							}
							
						});
						link.add(hit.render("label"));
						link.add(new Label("scope", hit.getScope()).setVisible(hit.getScope()!=null));
						item.add(link);

						if (item.getIndex() == activeHitIndex)
							item.add(AttributeModifier.append("class", "active"));
					}
					
				});
				instantSearchResult.add(new AjaxLink<Void>("moreSymbolHits") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(moreSymbolHitsBehavior = new RunTaskBehavior() {
							
							@Override
							protected void runTask(AjaxRequestTarget target) {
								SymbolQuery query = new SymbolQuery(
										instantSearchInput, false, false, false, RepoFilePage.MAX_QUERY_ENTRIES);
								try {
									SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
									List<QueryHit> hits = searchManager.search(repoModel.getObject(), revisionModel.getObject(), query);
									renderQueryHits(target, hits);
								} catch (TooGeneralQueryException e) {
									// this is impossible as we already queried part of the result
									throw new IllegalStateException();
								} catch (InterruptedException e) {
									throw new RuntimeException(e);
								}								
								instantSearchDropdown.hide(target);
							}
							
						});
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(symbolHits.size()==SYMBOL_DISPLAY_ENTRIES);
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						moreSymbolHitsBehavior.requestRun(target);
					}
					
				});
				instantSearchResult.add(new WebMarkupContainer("noSymbolHits") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(symbolHits.isEmpty());
					}
					
				});
				
				WebMarkupContainer textSection = new WebMarkupContainer("texts") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(instantSearchInput != null && instantSearchInput.length() >= IndexConstants.NGRAM_SIZE);
					}
					
				};
				instantSearchResult.add(textSection);
				textSection.add(new ListView<QueryHit>("textHits", new AbstractReadOnlyModel<List<QueryHit>>() {

					@Override
					public List<QueryHit> getObject() {
						return textHits;
					}
					
				}) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!textHits.isEmpty());
					}

					@Override
					protected void populateItem(ListItem<QueryHit> item) {
						final QueryHit hit = item.getModelObject();
						AjaxLink<Void> link = new AjaxLink<Void>("link") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								selectHit(target, hit);
							}
							
						};
						link.add(new Image("icon", hit.getIcon()) {

							@Override
							protected boolean shouldAddAntiCacheParameter() {
								return false;
							}
							
						});
						link.add(hit.render("label"));
						link.add(new Label("scope", hit.getScope()).setVisible(hit.getScope()!=null));
						item.add(link);

						if (item.getIndex() + symbolHits.size() == activeHitIndex)
							item.add(AttributeModifier.append("class", " active"));
					}
					
				});
				textSection.add(new AjaxLink<Void>("moreTextHits") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(moreTextHitsBehavior = new RunTaskBehavior() {
							
							@Override
							protected void runTask(AjaxRequestTarget target) {
								TextQuery query = new TextQuery(
										instantSearchInput, false, false, false, RepoFilePage.MAX_QUERY_ENTRIES);
								try {
									SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
									List<QueryHit> hits = searchManager.search(repoModel.getObject(), revisionModel.getObject(), query);
									renderQueryHits(target, hits);
								} catch (TooGeneralQueryException e) {
									// this is impossible as we already queried part of the result
									throw new IllegalStateException();
								} catch (InterruptedException e) {
									throw new RuntimeException(e);
								}								
								instantSearchDropdown.hide(target);
							}
							
						});
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(textHits.size()==TEXT_QUERY_ENTRIES);
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						moreTextHitsBehavior.requestRun(target);
					}
					
				});
				textSection.add(new WebMarkupContainer("noTextHits") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(textHits.isEmpty());
					}
					
				});
				
				return instantSearchResult;
			}
			
		};
		add(instantSearchDropdown);
		
		instantSearchField = new TextField<>("instantSearchInput", Model.of(""));
		instantSearchField.add(new AjaxFormComponentUpdatingBehavior("inputchange focus click") {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setChannel(new AjaxChannel("blob-instant-search", AjaxChannel.Type.DROP));
			}

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (instantSearchResult != null)
					target.add(instantSearchResult);
				
				instantSearchInput = instantSearchField.getInput();

				if (StringUtils.isNotBlank(instantSearchInput)) {
					SearchManager searchManager = GitPlex.getInstance(SearchManager.class);

					BlobQuery blobQuery = new SymbolQuery(instantSearchInput, false, false, false, SYMBOL_QUERY_ENTRIES);
					
					try {
						symbolHits = searchManager.search(repoModel.getObject(), revisionModel.getObject(), blobQuery);
					} catch (TooGeneralQueryException e) {
						symbolHits = new ArrayList<>();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					
					if (symbolHits.size() > SYMBOL_DISPLAY_ENTRIES) 
						symbolHits = new ArrayList<>(symbolHits.subList(0, SYMBOL_DISPLAY_ENTRIES));

					if (instantSearchInput.length() >= IndexConstants.NGRAM_SIZE) {
						TextQuery textQuery = new TextQuery(instantSearchInput, false, false, false, TEXT_QUERY_ENTRIES);
						try {
							textHits = searchManager.search(repoModel.getObject(), revisionModel.getObject(), textQuery);
						} catch (TooGeneralQueryException e) {
							symbolHits = new ArrayList<>();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					} else {
						textHits = new ArrayList<>();
					}
					
					instantSearchDropdown.show(target);
				} else {
					symbolHits = new ArrayList<>();
					textHits = new ArrayList<>();
					
					instantSearchDropdown.hide(target);
				}
				activeHitIndex = 0;
			}			
			
		});
		instantSearchField.add(new DropdownBehavior(instantSearchDropdown)
				.mode(null)
				.alignWithTrigger(100, 100, 100, 0, 6, true));
		instantSearchField.add(new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				String key = params.getParameterValue("key").toString();
				
				if (key.equals("return")) {
					QueryHit activeHit = getActiveHit();
					if (activeHit != null) {
						if (activeHit instanceof MoreSymbolHit) 
							moreSymbolHitsBehavior.requestRun(target);
						else if (activeHit instanceof MoreTextHit) 
							moreTextHitsBehavior.requestRun(target);
						else 
							selectHit(target, activeHit);
					}
				} else if (key.equals("up")) {
					activeHitIndex--;
				} else if (key.equals("down")) {
					activeHitIndex++;
				} else {
					throw new IllegalStateException("Unrecognized key: " + key);
				}
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				response.render(JavaScriptHeaderItem.forReference(HotkeysResourceReference.INSTANCE));

				response.render(JavaScriptHeaderItem.forReference(
						new JavaScriptResourceReference(BlobSearchPanel.class, "blob-search.js")));
				
				String script = String.format(
						"gitplex.blobSearch.init('%s', '%s', %s);", 
						instantSearchField.getMarkupId(true), instantSearchDropdown.getMarkupId(true),
						getCallbackFunction(CallbackParameter.explicit("key")));
				
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		add(instantSearchField);
		
		ModalPanel advancedSearchDlg = new AdvancedSearchDlg("advancedSearchDlg");
		add(advancedSearchDlg);
		add(new WebMarkupContainer("advancedSearch").add(new ModalBehavior(advancedSearchDlg)));
	}
	
	private QueryHit getActiveHit() {
		List<QueryHit> hits = new ArrayList<>();
		hits.addAll(symbolHits);
		if (symbolHits.size() == SYMBOL_DISPLAY_ENTRIES)
			hits.add(new MoreSymbolHit());
		hits.addAll(textHits);
		if (textHits.size() == TEXT_QUERY_ENTRIES)
			hits.add(new MoreTextHit());
		
		if (activeHitIndex >=0 && activeHitIndex<hits.size())
			return hits.get(activeHitIndex);
		else if (!hits.isEmpty())
			return hits.get(0);
		else
			return null;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(BlobSearchPanel.class, "blob-search.css")));
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		revisionModel.detach();
		
		super.onDetach();
	}

	private void selectHit(AjaxRequestTarget target, QueryHit hit) {
		target.appendJavaScript(String.format("$('#%s').hide();", instantSearchField.getMarkupId(true)));
		instantSearchDropdown.hide(target);
		onSelect(target, hit);
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, QueryHit hit);
	
	protected abstract void renderQueryHits(AjaxRequestTarget target, List<QueryHit> hits);
	
	private class AdvancedSearchDlg extends ModalPanel {

		private static final String SEARCH_TEXTS = "texts";
		
		private static final String SEARCH_SYMBOLS = "symbols";
		
		private String searchFor;
		
		private boolean regex;
		
		private boolean wholeWord;
		
		private boolean caseSensitive;
		
		private String searchType = SEARCH_TEXTS;
		
		private String fileTypes;
		
		public AdvancedSearchDlg(String id) {
			super(id);
		}
		
		@Override
		protected Component newContent(String id, ModalBehavior behavior) {
			Fragment fragment = new Fragment(id, "advancedSearchFrag", BlobSearchPanel.this);
			final Form<?> form = new Form<Void>("form");
			form.setOutputMarkupId(true);
			fragment.add(form);
			
			WebMarkupContainer searchForContainer = new WebMarkupContainer("searchFor");
			form.add(searchForContainer);
			final TextField<String> searchForInput = new TextField<String>("input", new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return searchFor;
				}

				@Override
				public void setObject(String object) {
					searchFor = object;
				}
						
			});
			searchForInput.setOutputMarkupId(true);
			searchForInput.setRequired(true);
			searchForContainer.add(searchForInput);
			searchForContainer.add(new FeedbackPanel("feedback", searchForInput));
			searchForContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

				@Override
				protected String load() {
					if (searchForInput.hasErrorMessage())
						return " has-error";
					else
						return "";
				}
				
			}));
			
			form.add(new CheckBox("regex", new PropertyModel<Boolean>(this, "regex")));
			form.add(new CheckBox("wholeWord", new PropertyModel<Boolean>(this, "wholeWord")));
			form.add(new CheckBox("caseSensitive", new PropertyModel<Boolean>(this, "caseSensitive")));
			
			form.add(new RadioGroup<String>("searchType", new PropertyModel<String>(this, "searchType")) {

				@Override
				protected void onInitialize() {
					super.onInitialize();
					
					add(new Radio<String>("texts", Model.of(SEARCH_TEXTS)));
					add(new Radio<String>("symbols", Model.of(SEARCH_SYMBOLS)));
				}
				
			});
			
			form.add(new TextField<String>("fileTypes", new PropertyModel<String>(this, "fileTypes")));
			
			form.add(new AjaxSubmitLink("search") {

				private RunTaskBehavior runTaskBehavior;
				
				@Override
				protected void onInitialize() {
					super.onInitialize();
					
					add(runTaskBehavior = new RunTaskBehavior() {
						
						@Override
						protected void runTask(AjaxRequestTarget target) {
							List<String> pathSuffixes = new ArrayList<>();
							if (fileTypes != null) {
								for (String fileType: Splitter.on(CharMatcher.anyOf(", \t")).trimResults().omitEmptyStrings().split(fileTypes)) {
									if (fileType.startsWith("*"))
										fileType = fileType.substring(1);
									if (!fileType.startsWith("."))
										fileType = "." + fileType;
									pathSuffixes.add(fileType);
								}
							}
							
							BlobQuery query;
							if (searchType.equals(SEARCH_SYMBOLS)) {
								query = new SymbolQuery(searchFor, regex, wholeWord, caseSensitive, 
										null, pathSuffixes, ADVANCED_QUERY_ENTRIES);
							} else {
								query = new TextQuery(searchFor, regex, wholeWord, caseSensitive, 
										null, pathSuffixes, ADVANCED_QUERY_ENTRIES);
							}

							try {
								SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
								List<QueryHit> hits = searchManager.search(repoModel.getObject(), revisionModel.getObject(), query);
								renderQueryHits(target, hits);
								close(target);
							} catch (TooGeneralQueryException e) {
								searchForInput.error("Query term is too general.");
								target.add(form);
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}								
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
					close(target);
				}
				
			});
			
			return fragment;
		}
		
	};
	
	private static class MoreSymbolHit extends QueryHit {

		public MoreSymbolHit() {
			super(null, null);
		}

		@Override
		public Component render(String componentId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getScope() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ResourceReference getIcon() {
			throw new UnsupportedOperationException();
		}

		@Override
		protected int score() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	private static class MoreTextHit extends QueryHit {

		public MoreTextHit() {
			super(null, null);
		}

		@Override
		public Component render(String componentId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getScope() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ResourceReference getIcon() {
			throw new UnsupportedOperationException();
		}

		@Override
		protected int score() {
			throw new UnsupportedOperationException();
		}
		
	}
	
}
