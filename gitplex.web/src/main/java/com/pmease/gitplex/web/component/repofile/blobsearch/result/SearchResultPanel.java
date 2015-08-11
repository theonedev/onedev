package com.pmease.gitplex.web.component.repofile.blobsearch.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.component.DirtyAwareAjaxLink;
import com.pmease.gitplex.search.hit.FileHit;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.hit.TextHit;

@SuppressWarnings("serial")
public abstract class SearchResultPanel extends Panel {

	public static final int MAX_QUERY_ENTRIES = 1000;
	
	private enum ExpandStatus {EXPAND_ALL, COLLAPSE_ALL};
	
	private static final String HITS_ID = "hits";
	
	private static final String EXPAND_LINK_ID = "expandLink";
	
	private static final String NAV_CHANNEL = "blob-search-result-nav";
	
	private final List<MatchedBlob> blobs;
	
	private final boolean hasMore;
	
	private int activeBlobIndex = -1;
	
	private int activeHitIndex = -1;
	
	private ListView<MatchedBlob> blobsView;
	
	private AjaxLink<Void> prevMatchLink;

	private AjaxLink<Void> nextMatchLink;
	
	public SearchResultPanel(String id, List<QueryHit> hits) {
		super(id);
		
		hasMore = (hits.size() == MAX_QUERY_ENTRIES);
		
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
		for (MatchedBlob blob: blobs) {
			Collections.sort(blob.getHits(), new Comparator<QueryHit>() {

				@Override
				public int compare(QueryHit hit1, QueryHit hit2) {
					return hit1.getTokenPos().getLine() - hit2.getTokenPos().getLine();
				}
				
			});
		}
	}

	private void onActiveIndexChange(AjaxRequestTarget target) {
		Component hitsContainer = blobsView.get(activeBlobIndex).get(HITS_ID);
		if (!hitsContainer.isVisibilityAllowed()) {
			hitsContainer.setVisibilityAllowed(true);
			target.add(hitsContainer);
			target.add(blobsView.get(activeBlobIndex).get(EXPAND_LINK_ID));
		} 

		String activeLinkId = getMarkupId() + "-" + activeBlobIndex; 
		if (activeHitIndex != -1)
			activeLinkId += "-" + activeHitIndex;

		String script = String.format(""
				+ "$('#%s').find('.selectable').removeClass('active');"
				+ "$('#%s').addClass('active');"
				+ "gitplex.blobSearchResult.scrollIfNecessary('%s');", 
				getMarkupId(), activeLinkId, getMarkupId());
		target.appendJavaScript(script);
		
		target.add(prevMatchLink);
		target.add(nextMatchLink);
		
		MatchedBlob activeBlob = blobs.get(activeBlobIndex);
		if (activeHitIndex != -1)
			onSelect(target, activeBlob.getHits().get(activeHitIndex));
		else 
			onSelect(target, new FileHit(activeBlob.getBlobPath()));
	}
	
	private void onPrevMatch(AjaxRequestTarget target) {
		if (prevMatchLink.isEnabled()) {		
			if (activeHitIndex>0) {
				activeHitIndex--;
			} else {
				activeBlobIndex--;
				MatchedBlob activeBlob = blobs.get(activeBlobIndex);
				if (activeBlob.getHits().isEmpty())
					activeHitIndex = -1;
				else
					activeHitIndex = activeBlob.getHits().size()-1;
			}
			
			onActiveIndexChange(target);
		}
	}
	
	private void onNextMatch(AjaxRequestTarget target) {
		if (nextMatchLink.isEnabled()) {
			if (activeBlobIndex == -1) {
				activeBlobIndex = 0;
				MatchedBlob activeBlob = blobs.get(activeBlobIndex);
				if (activeBlob.getHits().isEmpty())
					activeHitIndex = -1;
				else
					activeHitIndex = 0;
			} else {
				MatchedBlob activeBlob = blobs.get(activeBlobIndex);
				activeHitIndex++;
				if (activeHitIndex==activeBlob.getHits().size()) {
					activeBlobIndex++;
					activeBlob = blobs.get(activeBlobIndex);
					if (activeBlob.getHits().isEmpty())
						activeHitIndex = -1;
					else
						activeHitIndex = 0;
				}
			}
			
			onActiveIndexChange(target);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String message = "too many matches, displaying " + MAX_QUERY_ENTRIES + " of them";
		add(new Label("hasMoreMessage", message).setVisible(hasMore));
		
		add(prevMatchLink = new DirtyAwareAjaxLink<Void>("prevMatch") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onPrevMatch(target);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setEnabled(activeBlobIndex>0 || activeHitIndex>0);
			}
			
		});
		prevMatchLink.setOutputMarkupId(true);
		
		add(nextMatchLink = new DirtyAwareAjaxLink<Void>("nextMatch") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onNextMatch(target);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();

				int lastBlobIndex = blobs.size()-1;
				if (lastBlobIndex < 0)
					setEnabled(false);
				else
					setEnabled(activeBlobIndex<lastBlobIndex  || activeHitIndex<blobs.get(lastBlobIndex).getHits().size()-1);
			}
			
		});
		nextMatchLink.setOutputMarkupId(true);
		
		add(new AjaxLink<Void>("expandAll") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				RequestCycle.get().setMetaData(ExpandStatusKey.INSTANCE, ExpandStatus.EXPAND_ALL);
				target.add(SearchResultPanel.this);
				target.appendJavaScript("$(window).resize();");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				boolean visible = false;
				for (MatchedBlob blob: blobs) {
					if (!blob.getHits().isEmpty()) {
						visible = true;
						break;
					}
				}
				
				setVisible(visible);
			}
			
		});
		add(new AjaxLink<Void>("collapseAll") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				RequestCycle.get().setMetaData(ExpandStatusKey.INSTANCE, ExpandStatus.COLLAPSE_ALL);
				target.add(SearchResultPanel.this);
				target.appendJavaScript("$(window).resize();");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				boolean visible = false;
				for (MatchedBlob blob: blobs) {
					if (!blob.getHits().isEmpty()) {
						visible = true;
						break;
					}
				}
				
				setVisible(visible);
			}
			
		});
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		
		add(blobsView = new ListView<MatchedBlob>("blobs", blobs) {

			@Override
			protected void populateItem(final ListItem<MatchedBlob> blobItem) {
				final WebMarkupContainer hitsContainer = new WebMarkupContainer(HITS_ID) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						setVisible(!blobItem.getModelObject().getHits().isEmpty());
					}
					
				};
				
				blobItem.add(new AjaxLink<Void>(EXPAND_LINK_ID) {

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
								if (blobItem.getModelObject().getHits().isEmpty())
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
						
						setEnabled(!blobItem.getModelObject().getHits().isEmpty());
					}
					
				});
				
				blobItem.add(new DirtyAwareAjaxLink<Void>("blobLink") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.setChannel(new AjaxChannel(NAV_CHANNEL, AjaxChannel.Type.DROP));
					}
					
					@Override
					protected void onInitialize() {
						super.onInitialize();
						
						add(new Label("label", blobItem.getModelObject().getBlobPath()));
						
						if (activeBlobIndex == blobItem.getIndex() && activeHitIndex == -1)
							add(AttributeAppender.append("class", " active"));
						
						setMarkupId(SearchResultPanel.this.getMarkupId() + "-" + blobItem.getIndex());
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						activeBlobIndex = blobItem.getIndex();
						activeHitIndex = -1;
						onActiveIndexChange(target);
					}
					
				});
				
				ExpandStatus expandStatus = RequestCycle.get().getMetaData(ExpandStatusKey.INSTANCE);
				if (expandStatus == ExpandStatus.EXPAND_ALL) 
					hitsContainer.setVisibilityAllowed(true);
				else if (expandStatus == ExpandStatus.COLLAPSE_ALL) 
					hitsContainer.setVisibilityAllowed(false);				
				else
					hitsContainer.setVisibilityAllowed(blobItem.getIndex() == 0);
					
				hitsContainer.setOutputMarkupPlaceholderTag(true);
				
				blobItem.add(hitsContainer);
				
				hitsContainer.add(new ListView<QueryHit>(HITS_ID, blobItem.getModelObject().getHits()) {

					@Override
					protected void populateItem(final ListItem<QueryHit> hitItem) {
						final QueryHit hit = hitItem.getModelObject();
						hitItem.add(new DirtyAwareAjaxLink<Void>("hitLink") {

							@Override
							protected void onInitialize() {
								super.onInitialize();
								
								add(new Image("icon", hit.getIcon()) {

									@Override
									protected boolean shouldAddAntiCacheParameter() {
										return false;
									}
									
								});
								add(new Label("lineNo", String.valueOf(hit.getTokenPos().getLine()+1) + ":"));
								add(hit.render("label"));
								add(new Label("scope", hit.getScope())
										.setVisible(!(hit instanceof TextHit) && hit.getScope()!=null));
								
								if (activeBlobIndex == blobItem.getIndex() && activeHitIndex == hitItem.getIndex())
									add(AttributeAppender.append("class", " active"));

								setMarkupId(SearchResultPanel.this.getMarkupId() 
										+ "-" + blobItem.getIndex() + "-" + hitItem.getIndex());
							}

							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								super.updateAjaxAttributes(attributes);
								attributes.setChannel(new AjaxChannel(NAV_CHANNEL, AjaxChannel.Type.DROP));
							}
							
							@Override
							public void onClick(AjaxRequestTarget target) {
								activeBlobIndex = blobItem.getIndex();
								activeHitIndex = hitItem.getIndex();
								onActiveIndexChange(target);
							}
							
						});
					}
					
				});
				
			}
			
		});
		
		add(new WebMarkupContainer("noMatchingResult").setVisible(blobs.isEmpty()));
		
		add(new AbstractDefaultAjaxBehavior() {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setChannel(new AjaxChannel(NAV_CHANNEL, AjaxChannel.Type.DROP));
			}
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				String key = params.getParameterValue("key").toString();
				
				if (key.equals("up")) 
					onPrevMatch(target);
				else if (key.equals("down")) 
					onNextMatch(target);
				else 
					throw new IllegalStateException("Unrecognized key: " + key);
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);

				response.render(JavaScriptHeaderItem.forReference(
						new JavaScriptResourceReference(SearchResultPanel.class, "search-result.js")));
				response.render(CssHeaderItem.forReference(
						new CssResourceReference(SearchResultPanel.class, "search-result.css")));
				
				response.render(JavaScriptHeaderItem.forReference(HotkeysResourceReference.INSTANCE));
				
				String script = String.format(""
						+ "var $body = $('#%s>.search-result>.body');"
						+ "var callback = %s;"
						+ "$body.bind('keydown', 'up', function(e) {e.preventDefault(); if (pmease.commons.form.confirmLeave()) callback('up');});"
						+ "$body.bind('keydown', 'down', function(e) {e.preventDefault(); if (pmease.commons.form.confirmLeave()) callback('down');});", 
						getMarkupId(), getCallbackFunction(CallbackParameter.explicit("key")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
		setOutputMarkupId(true);
	}

	protected abstract void onSelect(AjaxRequestTarget target, QueryHit hit);

	protected abstract void onClose(AjaxRequestTarget target);

	private static class ExpandStatusKey extends MetaDataKey<ExpandStatus> {
		static final ExpandStatusKey INSTANCE = new ExpandStatusKey();		
	};
}
