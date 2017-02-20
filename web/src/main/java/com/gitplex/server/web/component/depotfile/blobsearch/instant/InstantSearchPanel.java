package com.gitplex.server.web.component.depotfile.blobsearch.instant;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.AttributeAppender;
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
import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.GitPlex;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.support.TextRange;
import com.gitplex.server.search.SearchManager;
import com.gitplex.server.search.hit.QueryHit;
import com.gitplex.server.search.query.BlobQuery;
import com.gitplex.server.search.query.FileQuery;
import com.gitplex.server.search.query.SymbolQuery;
import com.gitplex.server.search.query.TooGeneralQueryException;
import com.gitplex.server.util.StringUtils;
import com.gitplex.server.web.behavior.AbstractPostAjaxBehavior;
import com.gitplex.server.web.behavior.RunTaskBehavior;
import com.gitplex.server.web.component.PreventDefaultAjaxLink;
import com.gitplex.server.web.component.depotfile.blobsearch.result.SearchResultPanel;
import com.gitplex.server.web.component.floating.AlignPlacement;
import com.gitplex.server.web.component.floating.ComponentTarget;
import com.gitplex.server.web.component.floating.FloatingPanel;
import com.gitplex.server.web.page.depot.file.DepotFilePage;
import com.gitplex.server.web.util.ajaxlistener.ConfirmLeaveListener;

@SuppressWarnings("serial")
public abstract class InstantSearchPanel extends Panel {

	private static final int MAX_QUERY_ENTRIES = 15;
	
	final IModel<Depot> depotModel;
	
	final IModel<String> revisionModel;
	
	private TextField<String> searchField;
	
	private String searchInput;
	
	private FloatingPanel searchHint;
	
	private List<QueryHit> symbolHits;
	
	private RunTaskBehavior moreSymbolHitsBehavior;
	
	private int activeHitIndex;
	
	public InstantSearchPanel(String id, IModel<Depot> depotModel, IModel<String> revisionModel) {
		super(id);
		
		this.depotModel = depotModel;
		this.revisionModel = revisionModel;
	}
	
	private List<QueryHit> querySymbols(String searchInput, int count) {
		SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
		ObjectId commit = depotModel.getObject().getRevCommit(revisionModel.getObject());		
		List<QueryHit> symbolHits = new ArrayList<>();
		try {
			// first try an exact search against primary symbol to make sure the result 
			// always contains exact match if exists
			BlobQuery query = new SymbolQuery.Builder()
					.term(searchInput)
					.primary(true)
					.count(count)
					.build();
			symbolHits.addAll(searchManager.search(depotModel.getObject(), commit, query));
			
			// now do wildcard search but exclude the exact match returned above 
			if (symbolHits.size() < count) {
				query = new SymbolQuery.Builder().term(searchInput+"*")
						.excludeTerm(searchInput)
						.primary(true)
						.count(count-symbolHits.size())
						.build();
				symbolHits.addAll(searchManager.search(depotModel.getObject(), commit, query));
			}

			// do the same for file names
			if (symbolHits.size() < count) {
				query = new FileQuery.Builder()
						.fileNames(searchInput)
						.count(count-symbolHits.size())
						.build();
				symbolHits.addAll(searchManager.search(depotModel.getObject(), commit, query));
			}
			
			if (symbolHits.size() < count) {
				query = new FileQuery.Builder().fileNames(searchInput+"*")
						.excludeFileName(searchInput)
						.count(count-symbolHits.size())
						.build();
				symbolHits.addAll(searchManager.search(depotModel.getObject(), commit, query));
			}
			
			// do the same for secondary symbols
			if (symbolHits.size() < count) {
				query = new SymbolQuery.Builder()
						.term(searchInput)
						.primary(false)
						.count(count-symbolHits.size())
						.build();
				symbolHits.addAll(searchManager.search(depotModel.getObject(), commit, query));
			}
			
			if (symbolHits.size() < count) {
				query = new SymbolQuery.Builder().term(searchInput+"*")
						.excludeTerm(searchInput)
						.primary(false)
						.count(count-symbolHits.size())
						.build();
				symbolHits.addAll(searchManager.search(depotModel.getObject(), commit, query));
			}
			
		} catch (TooGeneralQueryException e) {
			symbolHits = new ArrayList<>();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return symbolHits;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		searchField = new TextField<>("input");
		searchField.add(new AbstractPostAjaxBehavior() {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setChannel(new AjaxChannel("blob-instant-search-input", AjaxChannel.Type.DROP));
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String key = params.getParameterValue("key").toString();

				if (key.equals("input")) {
					searchInput = params.getParameterValue("input").toString();
					if (StringUtils.isNotBlank(searchInput)) {
						symbolHits = querySymbols(searchInput, MAX_QUERY_ENTRIES);
						if (searchHint == null)
							newSearchHint(target);
						else
							target.add(searchHint.getContent());
					} else {
						symbolHits = new ArrayList<>();
						
						if (searchHint != null)
							searchHint.close();
					}
					activeHitIndex = 0;
				} else if (key.equals("return")) {
					QueryHit activeHit = getActiveHit();
					if (activeHit != null) {
						if (activeHit instanceof MoreSymbolHit) 
							moreSymbolHitsBehavior.requestRun(target);
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
				String script = String.format(
						"gitplex.server.blobInstantSearch.init('%s', %s);", 
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
						link.add(hit.renderIcon("icon"));
						link.add(hit.render("label"));
						link.add(new Label("scope", hit.getNamespace()).setVisible(hit.getNamespace()!=null));
						item.add(link);

						DepotFilePage.State state = new DepotFilePage.State();
						state.blobIdent.revision = revisionModel.getObject();
						state.blobIdent.path = hit.getBlobPath();
						state.mark = TextRange.of(hit.getTokenPos());
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
								List<QueryHit> hits = querySymbols(searchInput, SearchResultPanel.MAX_QUERY_ENTRIES);
								onMoreQueried(target, hits);
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
				
				searchResult.add(new WebMarkupContainer("noMatches") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(symbolHits.isEmpty());
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
		
		String script = String.format("gitplex.server.blobInstantSearch.hintOpened('%s', '%s');", 
				searchField.getMarkupId(true), searchHint.getMarkupId(true));
		target.appendJavaScript(script);
	}
	
	private QueryHit getActiveHit() {
		List<QueryHit> hits = new ArrayList<>();
		hits.addAll(symbolHits);
		if (symbolHits.size() == MAX_QUERY_ENTRIES)
			hits.add(new MoreSymbolHit());
		
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
		response.render(JavaScriptHeaderItem.forReference(new InstantSearchResourceReference()));
	}

	@Override
	protected void onDetach() {
		depotModel.detach();
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
		public String getNamespace() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Image renderIcon(String componentId) {
			throw new UnsupportedOperationException();
		}
		
	}
	
}
