package io.onedev.server.web.page.project.blob.search.quick;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.search.code.SearchManager;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.query.BlobQuery;
import io.onedev.server.search.code.query.FileQuery;
import io.onedev.server.search.code.query.SymbolQuery;
import io.onedev.server.search.code.query.TooGeneralQueryException;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.RunTaskBehavior;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.renderers.source.SourceRendererProvider;
import io.onedev.server.web.page.project.blob.search.result.SearchResultPanel;

@SuppressWarnings("serial")
public abstract class QuickSearchPanel extends Panel {

	private static final int MAX_QUERY_ENTRIES = 15;
	
	private final IModel<Project> projectModel;
	
	private final IModel<String> revisionModel;
	
	private String searchInput;
	
	private List<QueryHit> symbolHits = new ArrayList<>();
	
	private RunTaskBehavior moreSymbolHitsBehavior;
	
	public QuickSearchPanel(String id, IModel<Project> projectModel, IModel<String> revisionModel) {
		super(id);
		
		this.projectModel = projectModel;
		this.revisionModel = revisionModel;
	}

	private List<QueryHit> querySymbols(String searchInput, int count) {
		SearchManager searchManager = OneDev.getInstance(SearchManager.class);
		ObjectId commit = projectModel.getObject().getRevCommit(revisionModel.getObject(), true);		
		List<QueryHit> symbolHits = new ArrayList<>();
		try {
			// first try an exact search against primary symbol to make sure the result 
			// always contains exact match if exists
			BlobQuery query = new SymbolQuery.Builder()
					.term(searchInput)
					.primary(true)
					.count(count)
					.build();
			symbolHits.addAll(searchManager.search(projectModel.getObject(), commit, query));
			
			// now do wildcard search but exclude the exact match returned above 
			if (symbolHits.size() < count) {
				query = new SymbolQuery.Builder().term("*"+searchInput+"*")
						.excludeTerm(searchInput)
						.primary(true)
						.count(count-symbolHits.size())
						.build();
				symbolHits.addAll(searchManager.search(projectModel.getObject(), commit, query));
			}

			// do the same for file names
			if (symbolHits.size() < count) {
				query = new FileQuery.Builder()
						.fileNames(searchInput)
						.count(count-symbolHits.size())
						.build();
				symbolHits.addAll(searchManager.search(projectModel.getObject(), commit, query));
			}
			
			if (symbolHits.size() < count) {
				query = new FileQuery.Builder().fileNames("*"+searchInput+"*")
						.excludeFileName(searchInput)
						.count(count-symbolHits.size())
						.build();
				symbolHits.addAll(searchManager.search(projectModel.getObject(), commit, query));
			}
			
			// do the same for secondary symbols
			if (symbolHits.size() < count) {
				query = new SymbolQuery.Builder()
						.term(searchInput)
						.primary(false)
						.count(count-symbolHits.size())
						.build();
				symbolHits.addAll(searchManager.search(projectModel.getObject(), commit, query));
			}
			
			if (symbolHits.size() < count) {
				query = new SymbolQuery.Builder().term("*"+searchInput+"*")
						.excludeTerm(searchInput)
						.primary(false)
						.count(count-symbolHits.size())
						.build();
				symbolHits.addAll(searchManager.search(projectModel.getObject(), commit, query));
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

		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		TextField<String> searchField = new TextField<>("input");
		add(searchField);
		newSearchResult(null);
		
		add(new AbstractPostAjaxBehavior() {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setChannel(new AjaxChannel("blob-quick-search-input", AjaxChannel.Type.DROP));
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String key = params.getParameterValue("key").toString();

				if (key.equals("input")) {
					searchInput = params.getParameterValue("param").toString();
					if (StringUtils.isNotBlank(searchInput) && revisionModel.getObject() != null) {
						symbolHits = querySymbols(searchInput, MAX_QUERY_ENTRIES);
					} else {
						symbolHits = new ArrayList<>();
					}
					newSearchResult(target);
				} else if (key.equals("return")) {
					int activeHitIndex = params.getParameterValue("param").toInt();
					QueryHit activeHit = getActiveHit(activeHitIndex);
					if (activeHit != null) {
						if (activeHit instanceof MoreSymbolHit) { 
							moreSymbolHitsBehavior.requestRun(target);
						} else {
							onSelect(target, activeHit);
						}
					}
				} else {
					throw new IllegalStateException("Unrecognized key: " + key);
				}
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				String script = String.format(
						"onedev.server.onQuickSearchDomReady('%s', %s);", 
						getMarkupId(), 
						getCallbackFunction(explicit("key"), explicit("param")));
				
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	private void newSearchResult(@Nullable AjaxRequestTarget target) {
		WebMarkupContainer result = new WebMarkupContainer("result") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!StringUtils.isBlank(searchInput));			
			}
			
		};
		result.setOutputMarkupPlaceholderTag(true);
		
		result.add(new ListView<QueryHit>("symbolHits", new AbstractReadOnlyModel<List<QueryHit>>() {

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
				AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, hit);
					}
					
				};
				link.add(hit.renderIcon("icon"));
				link.add(hit.render("label"));
				link.add(new Label("scope", hit.getNamespace()).setVisible(hit.getNamespace()!=null));
				item.add(link);

				BlobIdent blobIdent = new BlobIdent(revisionModel.getObject(), hit.getBlobPath(), 
						FileMode.REGULAR_FILE.getBits());
				ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
				state.position = SourceRendererProvider.getPosition(hit.getTokenPos());
				PageParameters params = ProjectBlobPage.paramsOf(projectModel.getObject(), state);
				CharSequence url = RequestCycle.get().urlFor(ProjectBlobPage.class, params);
				link.add(AttributeAppender.replace("href", url.toString()));

				if (item.getIndex() == 0)
					item.add(AttributeModifier.append("class", "active"));
			}
			
		});
		result.add(new AjaxLink<Void>("moreSymbolHits") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(moreSymbolHitsBehavior = new RunTaskBehavior() {
					
					@Override
					protected void runTask(AjaxRequestTarget target) {
						List<QueryHit> hits = querySymbols(searchInput, SearchResultPanel.MAX_QUERY_ENTRIES);
						onMoreQueried(target, hits);
					}
					
				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(searchInput != null && symbolHits.size() == MAX_QUERY_ENTRIES);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				moreSymbolHitsBehavior.requestRun(target);
			}
			
		});
		
		result.add(new WebMarkupContainer("noMatches") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(StringUtils.isNotBlank(searchInput) && symbolHits.isEmpty());
			}
			
		});

		if (target != null) {
			replace(result);
			target.add(result);
		} else {
			add(result);
		}
	}
	
	private QueryHit getActiveHit(int activeHitIndex) {
		List<QueryHit> hits = new ArrayList<>();
		hits.addAll(symbolHits);
		if (symbolHits.size() == MAX_QUERY_ENTRIES)
			hits.add(new MoreSymbolHit());

		if (hits.isEmpty())
			return null;
		else if (activeHitIndex <0) 
			return hits.get(0);
		else if (activeHitIndex>=hits.size())
			return hits.get(hits.size()-1);
		else
			return hits.get(activeHitIndex);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new QuickSearchResourceReference()));
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		revisionModel.detach();
		
		super.onDetach();
	}

	protected abstract void onCancel(AjaxRequestTarget target);
	
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
