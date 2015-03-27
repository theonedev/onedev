package com.pmease.gitplex.web.component.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.IndexConstants;
import com.pmease.gitplex.search.SearchManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.BlobQuery;
import com.pmease.gitplex.search.query.SymbolQuery;
import com.pmease.gitplex.search.query.TextQuery;

@SuppressWarnings("serial")
public abstract class BlobSearcher extends Panel {

	private static final int MAX_QUERY_ENTRIES = 20;
	
	private final IModel<Repository> repoModel;
	
	private final String commitHash;
	
	private final boolean caseSensitive;
	
	private TextField<String> input;
	
	private DropdownPanel dropdown;
	
	private WebMarkupContainer results;
	
	private List<QueryHit> symbolHits;
	
	private List<QueryHit> textHits;
	
	private int activeHitIndex;
	
	public BlobSearcher(String id, IModel<Repository> repoModel, String commitHash, boolean caseSensitive) {
		super(id);
		
		this.repoModel = repoModel;
		this.commitHash = commitHash;
		this.caseSensitive = caseSensitive;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		dropdown = new DropdownPanel("dropdown") {

			@Override
			protected Component newContent(String id) {
				results = new Fragment(id, "resultsFrag", BlobSearcher.this);
				results.setOutputMarkupId(true);
				
				results.add(new ListView<QueryHit>("symbolHits", new AbstractReadOnlyModel<List<QueryHit>>() {

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
								onSelect(target, hit);
								dropdown.hide(target);
							}
							
						};
						link.add(hit.render("label"));
						item.add(link);

						if (item.getIndex() == activeHitIndex)
							item.add(AttributeModifier.append("class", "active"));
					}
					
				});
				results.add(new WebMarkupContainer("noSymbolHits") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(symbolHits.isEmpty());
					}
					
				});
				
				results.add(new ListView<QueryHit>("textHits", new AbstractReadOnlyModel<List<QueryHit>>() {

					@Override
					public List<QueryHit> getObject() {
						return textHits;
					}
					
				}) {

					@Override
					protected void populateItem(ListItem<QueryHit> item) {
						final QueryHit hit = item.getModelObject();
						AjaxLink<Void> link = new AjaxLink<Void>("link") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								onSelect(target, hit);
								dropdown.hide(target);
							}
							
						};
						link.add(hit.render("label"));
						item.add(link);

						if (item.getIndex() + symbolHits.size() == activeHitIndex)
							item.add(AttributeModifier.append("class", " active"));
					}
					
				});
				results.add(new WebMarkupContainer("noTextHits") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(textHits.isEmpty());
					}
					
				});
				
				return results;
			}
			
		};
		add(dropdown);
		
		input = new TextField<>("input", Model.of(""));
		input.add(new AjaxFormComponentUpdatingBehavior("inputchange focus click") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (results != null)
					target.add(results);
				String inputValue = input.getInput();
				if (inputValue != null && inputValue.length() >= IndexConstants.NGRAM_SIZE) {
					SearchManager searchManager = GitPlex.getInstance(SearchManager.class);

					BlobQuery query = new SymbolQuery(input.getInput(), false, caseSensitive, false, MAX_QUERY_ENTRIES);
					try {
						symbolHits = searchManager.search(repoModel.getObject(), commitHash, query);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					
					query = new TextQuery(input.getInput(), caseSensitive, false, MAX_QUERY_ENTRIES);
					try {
						textHits = searchManager.search(repoModel.getObject(), commitHash, query);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					
					dropdown.show(target);
				} else {
					symbolHits = new ArrayList<>();
					textHits = new ArrayList<>();
					
					dropdown.hide(target);
				}
				activeHitIndex = 0;
			}			
			
		});
		input.add(new DropdownBehavior(dropdown).mode(null));
		input.add(new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				String key = params.getParameterValue("key").toString();
				
				if (key.equals("return")) {
					QueryHit activeHit = getActiveHit();
					if (activeHit != null) {
						onSelect(target, activeHit);
						dropdown.hide(target);
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
						new JavaScriptResourceReference(BlobSearcher.class, "blob-searcher.js")));
				
				String script = String.format(
						"gitplex.blobSearcher.init('%s', '%s', %s);", 
						input.getMarkupId(true), dropdown.getMarkupId(true),
						getCallbackFunction(CallbackParameter.explicit("key")));
				
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		add(input);
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
				new CssResourceReference(BlobSearcher.class, "blob-searcher.css")));
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, QueryHit hit);
}
