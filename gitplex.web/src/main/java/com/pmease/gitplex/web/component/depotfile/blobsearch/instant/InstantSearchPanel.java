package com.pmease.gitplex.web.component.depotfile.blobsearch.instant;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.assets.doneevents.DoneEventsResourceReference;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.assets.scrollintoview.ScrollIntoViewResourceReference;
import com.pmease.commons.wicket.behavior.RunTaskBehavior;
import com.pmease.commons.wicket.component.PreventDefaultAjaxLink;
import com.pmease.commons.wicket.component.floating.AlignPlacement;
import com.pmease.commons.wicket.component.floating.ComponentTarget;
import com.pmease.commons.wicket.component.floating.FloatingPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.component.Mark;
import com.pmease.gitplex.search.SearchManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.BlobQuery;
import com.pmease.gitplex.search.query.FileQuery;
import com.pmease.gitplex.search.query.SymbolQuery;
import com.pmease.gitplex.search.query.TextQuery;
import com.pmease.gitplex.search.query.TooGeneralQueryException;
import com.pmease.gitplex.web.component.depotfile.blobsearch.result.SearchResultPanel;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;

@SuppressWarnings("serial")
public abstract class InstantSearchPanel extends Panel {

	private static final int MAX_QUERY_ENTRIES = 15;
	
	final IModel<Depot> depotModel;
	
	private final IModel<PullRequest> requestModel;
	
	final IModel<String> revisionModel;
	
	private TextField<String> searchField;
	
	private String searchInput;
	
	private FloatingPanel searchHint;
	
	private List<QueryHit> symbolHits;
	
	private List<QueryHit> textHits;
	
	private RunTaskBehavior moreSymbolHitsBehavior;
	
	private RunTaskBehavior moreTextHitsBehavior;
	
	private int activeHitIndex;
	
	public InstantSearchPanel(String id, IModel<Depot> depotModel, IModel<PullRequest> requestModel, 
			IModel<String> revisionModel) {
		super(id);
		
		this.depotModel = depotModel;
		this.requestModel = requestModel;
		this.revisionModel = revisionModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		searchField = new TextField<>("input");
		searchField.add(new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setChannel(new AjaxChannel("blob-instant-search-input", AjaxChannel.Type.DROP));
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				String key = params.getParameterValue("key").toString();

				if (key.equals("input")) {
					searchInput = params.getParameterValue("input").toString();
					if (StringUtils.isNotBlank(searchInput)) {
						SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
						ObjectId commit = depotModel.getObject().getRevCommit(revisionModel.getObject());
						try {
							BlobQuery query = new SymbolQuery("*"+searchInput+"*", true, false, 
									null, null, MAX_QUERY_ENTRIES);
							symbolHits = searchManager.search(depotModel.getObject(), commit, query);
							
							if (symbolHits.size() < MAX_QUERY_ENTRIES) {
								query = new FileQuery("*"+searchInput+"*", false, null, 
										MAX_QUERY_ENTRIES-symbolHits.size());
								symbolHits.addAll(searchManager.search(depotModel.getObject(), commit, query));
							}
							
							if (symbolHits.size() < MAX_QUERY_ENTRIES) {
								query = new SymbolQuery("*"+searchInput+"*", false, false, 
										null, null, MAX_QUERY_ENTRIES-symbolHits.size());
								symbolHits.addAll(searchManager.search(depotModel.getObject(), commit, query));
							}
						} catch (TooGeneralQueryException e) {
							symbolHits = new ArrayList<>();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
						
						try {
							BlobQuery query = new TextQuery(searchInput, false, false, false, 
									null, null, MAX_QUERY_ENTRIES);
							textHits = searchManager.search(depotModel.getObject(), commit, query);
						} catch (TooGeneralQueryException e) {
							textHits = new ArrayList<>();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
						
						if (searchHint == null)
							newSearchHint(target);
						else
							target.add(searchHint.getContent());
					} else {
						symbolHits = new ArrayList<>();
						textHits = new ArrayList<>();
						
						if (searchHint != null)
							searchHint.close();
					}
					activeHitIndex = 0;
				} else if (key.equals("return")) {
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
				response.render(JavaScriptHeaderItem.forReference(ScrollIntoViewResourceReference.INSTANCE));
				response.render(JavaScriptHeaderItem.forReference(DoneEventsResourceReference.INSTANCE));
				response.render(JavaScriptHeaderItem.forReference(HotkeysResourceReference.INSTANCE));

				response.render(JavaScriptHeaderItem.forReference(
						new JavaScriptResourceReference(InstantSearchPanel.class, "instant-search.js")));
				
				String script = String.format(
						"gitplex.blobInstantSearch.init('%s', %s);", 
						searchField.getMarkupId(), 
						getCallbackFunction(explicit("key"), explicit("input")));
				
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		searchField.setOutputMarkupId(true);
		add(searchField);
	}
	
	private void newSearchHint(AjaxRequestTarget target) {
		searchHint = new FloatingPanel(target, new ComponentTarget(searchField), AlignPlacement.bottom(0)) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(AttributeAppender.append("class", "instant-search-dropdown"));
			}

			@Override
			protected Component newContent(String id) {
				WebMarkupContainer searchResult = new Fragment(id, "resultFrag", InstantSearchPanel.this);
				searchResult.setOutputMarkupId(true);
				
				searchResult.add(new WebMarkupContainer("symbolsTitle") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!textHits.isEmpty());
					}
					
				});
				searchResult.add(new ListView<QueryHit>("symbolHits", new AbstractReadOnlyModel<List<QueryHit>>() {

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
						QueryHit hit = item.getModelObject();
						PreventDefaultAjaxLink<Void> link = new PreventDefaultAjaxLink<Void>("link") {

							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								super.updateAjaxAttributes(attributes);
								attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
							}
							
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

						DepotFilePage.State state = new DepotFilePage.State();
						state.blobIdent.revision = revisionModel.getObject();
						state.blobIdent.path = hit.getBlobPath();
						state.mark = Mark.of(hit.getTokenPos());
						state.requestId = PullRequest.idOf(requestModel.getObject());
						PageParameters params = DepotFilePage.paramsOf(depotModel.getObject(), state);
						CharSequence url = RequestCycle.get().urlFor(DepotFilePage.class, params);
						link.add(AttributeAppender.replace("href", url.toString()));

						if (item.getIndex() == activeHitIndex)
							item.add(AttributeModifier.append("class", "active"));
					}
					
				});
				searchResult.add(new PreventDefaultAjaxLink<Void>("moreSymbolHits") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(moreSymbolHitsBehavior = new RunTaskBehavior() {
							
							@Override
							protected void runTask(AjaxRequestTarget target) {
								try {
									List<QueryHit> hits = new ArrayList<>();

									BlobQuery query = new SymbolQuery("*"+searchInput+"*", true, false, 
											null, null, SearchResultPanel.MAX_QUERY_ENTRIES);

									SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
									ObjectId commit = depotModel.getObject().getRevCommit(revisionModel.getObject());
									hits.addAll(searchManager.search(depotModel.getObject(), commit, query));
									
									if (hits.size() < SearchResultPanel.MAX_QUERY_ENTRIES) {
										query = new FileQuery("*"+searchInput+"*", false, null, 
												SearchResultPanel.MAX_QUERY_ENTRIES-hits.size());
										hits.addAll(searchManager.search(depotModel.getObject(), commit, query));
									}
									
									if (hits.size() < SearchResultPanel.MAX_QUERY_ENTRIES) {
										query = new SymbolQuery("*"+searchInput+"*", false, false, 
												null, null, SearchResultPanel.MAX_QUERY_ENTRIES-hits.size());
										hits.addAll(searchManager.search(depotModel.getObject(), commit, query));
									}
									onMoreQueried(target, hits);
								} catch (TooGeneralQueryException e) {
									// this is impossible as we already queried part of the result
									throw new IllegalStateException();
								} catch (InterruptedException e) {
									throw new RuntimeException(e);
								}								
								close();
							}
							
						});
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(symbolHits.size() == MAX_QUERY_ENTRIES);
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						moreSymbolHitsBehavior.requestRun(target);
					}
					
				});
				
				searchResult.add(new WebMarkupContainer("textsTitle") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!symbolHits.isEmpty());
					}
					
				});
				searchResult.add(new ListView<QueryHit>("textHits", new AbstractReadOnlyModel<List<QueryHit>>() {

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
						PreventDefaultAjaxLink<Void> link = new PreventDefaultAjaxLink<Void>("link") {

							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								super.updateAjaxAttributes(attributes);
								attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
							}
							
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
						
						DepotFilePage.State state = new DepotFilePage.State();
						state.blobIdent.revision = revisionModel.getObject();
						state.blobIdent.path = hit.getBlobPath();
						state.mark = Mark.of(hit.getTokenPos());
						state.requestId = PullRequest.idOf(requestModel.getObject());
						PageParameters params = DepotFilePage.paramsOf(depotModel.getObject(), state);
						CharSequence url = RequestCycle.get().urlFor(DepotFilePage.class, params);
						link.add(AttributeAppender.replace("href", url.toString()));
					}
					
				});
				searchResult.add(new PreventDefaultAjaxLink<Void>("moreTextHits") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(moreTextHitsBehavior = new RunTaskBehavior() {
							
							@Override
							protected void runTask(AjaxRequestTarget target) {
								TextQuery query = new TextQuery(searchInput, false, false, false, 
										null, null, SearchResultPanel.MAX_QUERY_ENTRIES);
								try {
									SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
									ObjectId commit = depotModel.getObject().getRevCommit(revisionModel.getObject());
									List<QueryHit> hits = searchManager.search(depotModel.getObject(), commit, query);
									onMoreQueried(target, hits);
								} catch (TooGeneralQueryException e) {
									// this is impossible as we already queried part of the result
									throw new IllegalStateException();
								} catch (InterruptedException e) {
									throw new RuntimeException(e);
								}								
								close();
							}
							
						});
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(textHits.size() == MAX_QUERY_ENTRIES);
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						moreTextHitsBehavior.requestRun(target);
					}
					
				});
				
				searchResult.add(new WebMarkupContainer("noMatches") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(symbolHits.isEmpty() && textHits.isEmpty());
					}
					
				});
				
				return searchResult;
			}

			@Override
			protected void onClosed() {
				super.onClosed();
				searchHint = null;
			}
			
		};	
		
		String script = String.format("gitplex.blobInstantSearch.hintOpened('%s', '%s');", 
				searchField.getMarkupId(true), searchHint.getMarkupId(true));
		target.appendJavaScript(script);
	}
	
	private QueryHit getActiveHit() {
		List<QueryHit> hits = new ArrayList<>();
		hits.addAll(symbolHits);
		if (symbolHits.size() == MAX_QUERY_ENTRIES)
			hits.add(new MoreSymbolHit());
		hits.addAll(textHits);
		if (textHits.size() == MAX_QUERY_ENTRIES)
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
				new CssResourceReference(InstantSearchPanel.class, "instant-search.css")));
	}

	@Override
	protected void onDetach() {
		depotModel.detach();
		requestModel.detach();
		revisionModel.detach();
		
		super.onDetach();
	}

	private void selectHit(AjaxRequestTarget target, QueryHit hit) {
		target.appendJavaScript(String.format("$('#%s').hide();", searchField.getMarkupId(true)));
		searchHint.close();
		onSelect(target, hit);
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, QueryHit hit);
	
	protected abstract void onMoreQueried(AjaxRequestTarget target, List<QueryHit> hits);
	
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
