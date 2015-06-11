package com.pmease.gitplex.web.page.repository.file;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;

import com.pmease.gitplex.search.hit.FileHit;
import com.pmease.gitplex.search.hit.MatchedBlob;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.hit.TextHit;

@SuppressWarnings("serial")
public abstract class SearchResultPanel extends Panel {

	public static final int MAX_QUERY_ENTRIES = 1000;
	
	private enum ExpandStatus {EXPAND_ALL, COLLAPSE_ALL};
	
	private static final String HITS_ID = "hits";
	
	private final List<MatchedBlob> blobs;
	
	private final boolean hasMore;

	public SearchResultPanel(String id, List<QueryHit> hits) {
		super(id);
		
		hasMore = (hits.size() == SearchResultPanel.MAX_QUERY_ENTRIES);
		
		Map<String, MatchedBlob> hitsByBlob = new LinkedHashMap<>();

		for (QueryHit hit: hits) {
			MatchedBlob blob = hitsByBlob.get(hit.getBlobPath());
			if (blob == null) {
				blob = new MatchedBlob(hit.getBlobPath(), new ArrayList<QueryHit>());
				hitsByBlob.put(hit.getBlobPath(), blob);
			}
			if (!(hit instanceof FileHit))
				blob.getHits().add(hit);
		}
		
		blobs = new ArrayList<>(hitsByBlob.values());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String message = "too many matches, displaying " + MAX_QUERY_ENTRIES + " of them";
		add(new Label("hasMoreMessage", message).setVisible(hasMore));
		
		add(new AjaxLink<Void>("prevMatch") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				
			}
			
		});
		add(new AjaxLink<Void>("nextMatch") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				
			}
			
		});
		add(new AjaxLink<Void>("expandAll") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				RequestCycle.get().setMetaData(ExpandStatusKey.INSTANCE, ExpandStatus.EXPAND_ALL);
				target.add(SearchResultPanel.this);
				target.appendJavaScript("$(window).resize();");
			}
			
		});
		add(new AjaxLink<Void>("collapseAll") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				RequestCycle.get().setMetaData(ExpandStatusKey.INSTANCE, ExpandStatus.COLLAPSE_ALL);
				target.add(SearchResultPanel.this);
				target.appendJavaScript("$(window).resize();");
			}
			
		});
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		
		add(new ListView<MatchedBlob>("blobs", blobs) {

			@Override
			protected void populateItem(final ListItem<MatchedBlob> item) {
				final WebMarkupContainer hitsContainer = new WebMarkupContainer(HITS_ID) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						setVisible(!item.getModelObject().getHits().isEmpty());
					}
					
				};
				ExpandStatus expandStatus = RequestCycle.get().getMetaData(ExpandStatusKey.INSTANCE);
				if (expandStatus == ExpandStatus.EXPAND_ALL) 
					hitsContainer.setVisibilityAllowed(true);
				else if (expandStatus == ExpandStatus.COLLAPSE_ALL) 
					hitsContainer.setVisibilityAllowed(false);				
				else
					hitsContainer.setVisibilityAllowed(item.getIndex() == 0);
					
				hitsContainer.setOutputMarkupPlaceholderTag(true);
				
				item.add(hitsContainer);
				
				hitsContainer.add(new ListView<QueryHit>(HITS_ID, item.getModelObject().getHits()) {

					@Override
					protected void populateItem(ListItem<QueryHit> item) {
						final QueryHit hit = item.getModelObject();
						item.add(new Image("icon", hit.getIcon()) {

							@Override
							protected boolean shouldAddAntiCacheParameter() {
								return false;
							}
							
						});
						item.add(new Label("lineNo", String.valueOf(hit.getTokenPos().getLine()+1) + ":"));
						item.add(new AjaxLink<Void>("lineLink") {

							@Override
							protected void onInitialize() {
								super.onInitialize();
								add(hit.render("label"));
								add(new Label("scope", hit.getScope())
										.setVisible(!(hit instanceof TextHit) && hit.getScope()!=null));
							}

							@Override
							public void onClick(AjaxRequestTarget target) {
								onSelect(target, hit);
							}
							
						});
					}
					
				});
				
				item.add(new AjaxLink<Void>("expandLink") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						hitsContainer.setVisibilityAllowed(!hitsContainer.isVisibilityAllowed());
						target.add(hitsContainer);
						target.add(this);
					}

					@Override
					protected void onInitialize() {
						super.onInitialize();
						
						WebMarkupContainer icon = new WebMarkupContainer("icon");
						icon.add(AttributeModifier.append("class", new LoadableDetachableModel<String>() {

							@Override
							protected String load() {
								if (item.getModelObject().getHits().isEmpty())
									return "fa fa-fw fa-angle-right fa-none";
								else if (hitsContainer.isVisibilityAllowed())
									return "fa fa-fw fa-caret-down";
								else
									return "fa fa-fw fa-caret-right";
							}
							
						}));
						
						setOutputMarkupId(true);
						
						add(icon);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						setEnabled(!item.getModelObject().getHits().isEmpty());
					}
					
				});
				
				item.add(new AjaxLink<Void>("pathLink") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						
						add(new Label("label", item.getModelObject().getBlobPath()));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, new FileHit(item.getModelObject().getBlobPath()));
					}
					
				});
			}
			
		});
		
		add(new WebMarkupContainer("noMatchingResult").setVisible(blobs.isEmpty()));
		
		setOutputMarkupId(true);
	}

	protected abstract void onSelect(AjaxRequestTarget target, QueryHit hit);

	protected abstract void onClose(AjaxRequestTarget target);

	private static class ExpandStatusKey extends MetaDataKey<ExpandStatus> {
		static final ExpandStatusKey INSTANCE = new ExpandStatusKey();		
	};
}
