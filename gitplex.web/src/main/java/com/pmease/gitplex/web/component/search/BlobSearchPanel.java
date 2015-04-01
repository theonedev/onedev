package com.pmease.gitplex.web.component.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
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

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.pmease.commons.wicket.AjaxEvent;
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
import com.pmease.gitplex.search.hit.SymbolHit;
import com.pmease.gitplex.search.query.BlobQuery;
import com.pmease.gitplex.search.query.SymbolQuery;
import com.pmease.gitplex.search.query.TextQuery;

@SuppressWarnings("serial")
public abstract class BlobSearchPanel extends Panel {

	private static final int MAX_INSTANT_QUERY_ENTRIES = 15;
	
	public static final int MAX_ADVANCED_QUERY_ENTRIES = 1000;
	
	private final IModel<Repository> repoModel;
	
	private TextField<String> instantSearchInput;
	
	private String instantSearchFor;
	
	private DropdownPanel instantSearchResultDropdown;
	
	private WebMarkupContainer instantSearchResultContainer;
	
	private List<QueryHit> symbolHits;
	
	private List<QueryHit> textHits;
	
	private int activeHitIndex;
	
	public BlobSearchPanel(String id, IModel<Repository> repoModel) {
		super(id);
		
		this.repoModel = repoModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		instantSearchResultDropdown = new DropdownPanel("instantSearchResult") {

			@Override
			protected Component newContent(String id) {
				instantSearchResultContainer = new Fragment(id, "instantSearchResultFrag", BlobSearchPanel.this);
				instantSearchResultContainer.setOutputMarkupId(true);
				
				instantSearchResultContainer.add(new ListView<QueryHit>("symbolHits", new AbstractReadOnlyModel<List<QueryHit>>() {

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
						item.add(new Image("icon", hit.getIcon()) {

							@Override
							protected boolean shouldAddAntiCacheParameter() {
								return false;
							}
							
						});
						AjaxLink<Void> link = new AjaxLink<Void>("link") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								selectHit(target, hit);
							}
							
						};
						link.add(hit.render("label"));
						item.add(link);
						
						item.add(new Label("scope", hit.getScope()).setVisible(hit.getScope()!=null));

						if (item.getIndex() == activeHitIndex)
							item.add(AttributeModifier.append("class", "active"));
					}
					
				});
				instantSearchResultContainer.add(new AjaxLink<Void>("moreSymbolHits") {

					private RunTaskBehavior runTaskBehavior;

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(runTaskBehavior = new RunTaskBehavior() {
							
							@Override
							protected void runTask(AjaxRequestTarget target) {
								SymbolQuery query = new SymbolQuery(
										instantSearchFor, false, false, false, MAX_ADVANCED_QUERY_ENTRIES);
								try {
									SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
									List<QueryHit> hits = searchManager.search(repoModel.getObject(), getCurrentCommit(), query);
									sortSymbolHits(hits);
									onCompleteAdvancedSearch(target, hits);
								} catch (InterruptedException e) {
									throw new RuntimeException(e);
								}								
								instantSearchResultDropdown.hide(target);
							}
							
						});
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(symbolHits.size()==MAX_INSTANT_QUERY_ENTRIES);
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						runTaskBehavior.requestRun(target);
					}
					
				});
				instantSearchResultContainer.add(new WebMarkupContainer("noSymbolHits") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(symbolHits.isEmpty());
					}
					
				});
				
				instantSearchResultContainer.add(new ListView<QueryHit>("textHits", new AbstractReadOnlyModel<List<QueryHit>>() {

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
						item.add(new Image("icon", hit.getIcon()) {

							@Override
							protected boolean shouldAddAntiCacheParameter() {
								return false;
							}
							
						});
						AjaxLink<Void> link = new AjaxLink<Void>("link") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								selectHit(target, hit);
							}
							
						};
						link.add(hit.render("label"));
						item.add(link);

						if (item.getIndex() + symbolHits.size() == activeHitIndex)
							item.add(AttributeModifier.append("class", " active"));
					}
					
				});
				instantSearchResultContainer.add(new AjaxLink<Void>("moreTextHits") {

					private RunTaskBehavior runTaskBehavior;

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(runTaskBehavior = new RunTaskBehavior() {
							
							@Override
							protected void runTask(AjaxRequestTarget target) {
								TextQuery query = new TextQuery(
										instantSearchFor, false, false, false, MAX_ADVANCED_QUERY_ENTRIES);
								try {
									SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
									List<QueryHit> hits = searchManager.search(repoModel.getObject(), getCurrentCommit(), query);
									onCompleteAdvancedSearch(target, hits);
								} catch (InterruptedException e) {
									throw new RuntimeException(e);
								}								
								instantSearchResultDropdown.hide(target);
							}
							
						});
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(textHits.size()==MAX_INSTANT_QUERY_ENTRIES);
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						runTaskBehavior.requestRun(target);
					}
					
				});
				instantSearchResultContainer.add(new WebMarkupContainer("noTextHits") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(textHits.isEmpty());
					}
					
				});
				
				return instantSearchResultContainer;
			}
			
		};
		add(instantSearchResultDropdown);
		
		instantSearchInput = new TextField<>("instantSearchInput", Model.of(""));
		instantSearchInput.add(new AjaxFormComponentUpdatingBehavior("inputchange focus click") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (instantSearchResultContainer != null)
					target.add(instantSearchResultContainer);
				
				instantSearchFor = instantSearchInput.getInput();
				if (instantSearchFor != null && instantSearchFor.length() >= IndexConstants.NGRAM_SIZE) {
					SearchManager searchManager = GitPlex.getInstance(SearchManager.class);

					BlobQuery query = new SymbolQuery(instantSearchInput.getInput(), false, false, false, MAX_INSTANT_QUERY_ENTRIES);
					try {
						symbolHits = searchManager.search(repoModel.getObject(), getCurrentCommit(), query);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					
					sortSymbolHits(symbolHits);
					
					query = new TextQuery(instantSearchInput.getInput(), false, false, false, MAX_INSTANT_QUERY_ENTRIES);
					try {
						textHits = searchManager.search(repoModel.getObject(), getCurrentCommit(), query);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					
					instantSearchResultDropdown.show(target);
				} else {
					symbolHits = new ArrayList<>();
					textHits = new ArrayList<>();
					
					instantSearchResultDropdown.hide(target);
				}
				activeHitIndex = 0;
				
				send(BlobSearchPanel.this, Broadcast.BREADTH, new InstantSearchInputUpdated(target));
			}			
			
		});
		instantSearchInput.add(new DropdownBehavior(instantSearchResultDropdown).mode(null));
		instantSearchInput.add(new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				String key = params.getParameterValue("key").toString();
				
				if (key.equals("return")) {
					QueryHit activeHit = getActiveHit();
					if (activeHit != null)
						selectHit(target, activeHit);
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
						"gitplex.blobSearch.initInstantSearch('%s', '%s', %s);", 
						instantSearchInput.getMarkupId(true), instantSearchResultDropdown.getMarkupId(true),
						getCallbackFunction(CallbackParameter.explicit("key")));
				
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		add(instantSearchInput);
		
		ModalPanel advancedSearchDlg = new AdvancedSearchDlg("advancedSearchDlg");
		add(advancedSearchDlg);
		add(new WebMarkupContainer("advancedSearch").add(new ModalBehavior(advancedSearchDlg)));
	}
	
	private List<QueryHit> getHits() {
		List<QueryHit> hits = new ArrayList<>();
		hits.addAll(symbolHits);
		hits.addAll(textHits);
		return hits;
	}
	
	private QueryHit getActiveHit() {
		List<QueryHit> hits = getHits();
		
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
		
		super.onDetach();
	}

	private void selectHit(AjaxRequestTarget target, QueryHit hit) {
		target.appendJavaScript(String.format("$('#%s').hide();", instantSearchInput.getMarkupId(true)));
		instantSearchResultDropdown.hide(target);
		onSelect(target, hit);
	}
	
	protected abstract String getCurrentCommit();
	
	protected abstract void onSelect(AjaxRequestTarget target, QueryHit hit);
	
	protected abstract void onCompleteAdvancedSearch(AjaxRequestTarget target, List<QueryHit> blobs);
	
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
		protected Component newContent(String id) {
			Fragment fragment = new Fragment(id, "advancedSearchFrag", BlobSearchPanel.this);
			Form<?> form = new Form<Void>("form");
			form.setOutputMarkupId(true);
			fragment.add(form);
			
			searchFor = instantSearchFor;
			
			WebMarkupContainer searchForContainer = new WebMarkupContainer("searchFor");
			form.add(searchForContainer);
			final TextField<String> searchForInput = new TextField<String>("input", new PropertyModel<String>(this, "searchFor")) {

				@Override
				public void onEvent(IEvent<?> event) {
					super.onEvent(event);
					if (event.getPayload() instanceof InstantSearchInputUpdated) {
						InstantSearchInputUpdated instantSearchInputUpdated = (InstantSearchInputUpdated) event.getPayload();
						searchFor = instantSearchFor;
						instantSearchInputUpdated.getTarget().add(this);
					}
				}
				
			};
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
										null, pathSuffixes, MAX_ADVANCED_QUERY_ENTRIES);
							} else {
								query = new TextQuery(searchFor, regex, wholeWord, caseSensitive, 
										null, pathSuffixes, MAX_ADVANCED_QUERY_ENTRIES);
							}
							
							try {
								SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
								List<QueryHit> hits = searchManager.search(repoModel.getObject(), getCurrentCommit(), query);
								onCompleteAdvancedSearch(target, hits);
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}								
							
							hide(target);
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
					hide(target);
				}
				
			});
			
			return fragment;
		}
		
	};
	
	private void sortSymbolHits(List<QueryHit> hits) {
		Collections.sort(hits, new Comparator<QueryHit>() {

			@Override
			public int compare(QueryHit hit1, QueryHit hit2) {
				if (hit1 instanceof SymbolHit) {
					if (hit2 instanceof SymbolHit) {
						SymbolHit symbolHit1 = (SymbolHit) hit1;
						SymbolHit symbolHit2 = (SymbolHit) hit2;
						return symbolHit1.getSymbol().getImportance() - symbolHit2.getSymbol().getImportance();
					} else {
						return -1;
					}
				} else {
					if (hit2 instanceof SymbolHit) {
						return 1;
					} else {
						return hit1.getBlobPath().length()-hit2.getBlobPath().length();
					}
				}
			}
			
		});
	}
	
	private static class InstantSearchInputUpdated extends AjaxEvent {

		public InstantSearchInputUpdated(AjaxRequestTarget target) {
			super(target);
		}

	}
}
