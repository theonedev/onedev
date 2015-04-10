package com.pmease.gitplex.web.component.sourceview;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.commons.wicket.assets.codemirror.CodeMirrorResourceReference;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.SearchManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.SymbolQuery;

@SuppressWarnings("serial")
public class SourceViewPanel extends Panel {

	private static final int MAX_SYMBOL_QUERY_ENTRIES = 20;
	
	private final IModel<Repository> repoModel;
	
	private final IModel<Source> sourceModel;
	
	private Component codeContainer;
	
	private WebMarkupContainer symbolsContainer;
	
	private List<QueryHit> symbolHits = new ArrayList<>();
	
	public SourceViewPanel(String id, IModel<Repository> repoModel, IModel<Source> sourceModel) {
		super(id);
		
		this.repoModel = repoModel;
		this.sourceModel = sourceModel;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(codeContainer = new WebMarkupContainer("code"));
		codeContainer.setOutputMarkupId(true);
		add(symbolsContainer = new WebMarkupContainer("symbols"));
		symbolsContainer.setOutputMarkupId(true);
		symbolsContainer.add(new ListView<QueryHit>("symbols", new AbstractReadOnlyModel<List<QueryHit>>() {

			@Override
			public List<QueryHit> getObject() {
				return symbolHits;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<QueryHit> item) {
				QueryHit hit = item.getModelObject();
				item.add(hit.render("symbol")); 
			}
			
		});
		
		add(new AbstractDefaultAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				String symbol = params.getParameterValue("symbol").toString();
				SymbolQuery query = new SymbolQuery(symbol, false, true, true, MAX_SYMBOL_QUERY_ENTRIES);
				try {
					SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
					symbolHits = searchManager.search(repoModel.getObject(), sourceModel.getObject().getRevision(), query);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}								
				target.add(symbolsContainer);
				String script = String.format("gitplex.sourceview.symbolsQueried('%s', '%s');", 
						codeContainer.getMarkupId(), symbolsContainer.getMarkupId());
				target.appendJavaScript(script);
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);

				response.render(JavaScriptHeaderItem.forReference(CodeMirrorResourceReference.INSTANCE));
				
				response.render(JavaScriptHeaderItem.forReference(
						new JavaScriptResourceReference(SourceViewPanel.class, "source-view.js")));
				response.render(CssHeaderItem.forReference(
						new CssResourceReference(SourceViewPanel.class, "source-view.css")));
				
				ResourceReference ajaxIndicator =  new PackageResourceReference(SourceViewPanel.class, "ajax-indicator.gif");
				String script = String.format("gitplex.sourceview.init('%s', '%s', '%s', '%s', %s);", 
						codeContainer.getMarkupId(), 
						StringEscapeUtils.escapeEcmaScript(sourceModel.getObject().getContent()),
						sourceModel.getObject().getPath(), 
						RequestCycle.get().urlFor(ajaxIndicator, new PageParameters()), 
						getCallbackFunction(CallbackParameter.explicit("symbol")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});		
	}
	
	@Override
	protected void onDetach() {
		repoModel.detach();
		sourceModel.detach();
		
		super.onDetach();
	}

}
