package io.onedev.server.web.page.project.blob.search.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DisabledAttributeLinkBehavior;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;

import io.onedev.commons.jsymbol.util.HighlightableLabel;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.search.code.hit.FileHit;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.hit.TextHit;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.renderers.source.SourceRendererProvider;

@SuppressWarnings("serial")
public abstract class SearchResultPanel extends Panel {

	public static final int MAX_QUERY_ENTRIES = 1000;
	
	private enum ExpandStatus {EXPAND_ALL, COLLAPSE_ALL};
	
	private static final String HITS_ID = "hits";
	
	private static final String EXPAND_LINK_ID = "expandLink";
	
	private static final String NAV_CHANNEL = "blob-search-result-nav";
	
	private final BlobRenderContext context;
	
	private final List<MatchedBlob> blobs;
	
	private final boolean hasMore;
	
	private int activeBlobIndex = -1;
	
	private int activeHitIndex = -1;
	
	private ListView<MatchedBlob> blobsView;
	
	private AjaxLink<Void> prevMatchLink;

	private AjaxLink<Void> nextMatchLink;
	
	public SearchResultPanel(String id, BlobRenderContext context, List<QueryHit> hits) {
		super(id);
		
		this.context = context;
		
		hasMore = (hits.size() == MAX_QUERY_ENTRIES);
		
		Map<String, MatchedBlob> hitsByBlob = new LinkedHashMap<>();

		for (QueryHit hit: hits) {
			MatchedBlob blob = hitsByBlob.get(hit.getBlobPath());
			if (blob == null) {
				blob = new MatchedBlob(hit.getBlobPath(), new ArrayList<QueryHit>());
				hitsByBlob.put(hit.getBlobPath(), blob);
			}
			if (!(hit instanceof FileHit)) {
				blob.getHits().add(hit);
			} else { 
				FileHit fileHit = (FileHit) hit;
				if (fileHit.getMatch() != null) {
					int index = blob.getBlobPath().lastIndexOf('/');
					blob.setMatch(new LinearRange(fileHit.getMatch().getFrom()+index+1, fileHit.getMatch().getTo()+index+1));
				}
			}
		}
		
		blobs = new ArrayList<>(hitsByBlob.values());
		for (MatchedBlob blob: blobs) {
			Collections.sort(blob.getHits(), new Comparator<QueryHit>() {

				@Override
				public int compare(QueryHit o1, QueryHit o2) {
					if (o1.getTokenPos() != null) {
						if (o2.getTokenPos() != null) 
							return o1.getTokenPos().getFromRow() - o2.getTokenPos().getFromRow();
						else
							return -1;
					} else {
						if (o2.getTokenPos() != null) 
							return 1;
						else
							return 0;
					}
				}
				
			});
		}
	}

	@SuppressWarnings("deprecation")
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
				+ "$('#%s>.search-result>.body a.selectable.active').scrollIntoView();", 
				getMarkupId(), activeLinkId, getMarkupId());
		target.appendJavaScript(script);
		
		target.add(prevMatchLink);
		target.add(nextMatchLink);
		
		MatchedBlob activeBlob = blobs.get(activeBlobIndex);
		
		QueryHit hit;
		if (activeHitIndex != -1)
			hit = activeBlob.getHits().get(activeHitIndex);
		else 
			hit = new FileHit(activeBlob.getBlobPath(), null);
		
		BlobIdent selected = new BlobIdent(context.getBlobIdent().revision, hit.getBlobPath(), 
				FileMode.REGULAR_FILE.getBits());
		context.onSelect(target, selected, SourceRendererProvider.getPosition(hit.getTokenPos()));
	}
	
	private String getActiveBlobPath(ActiveIndex activeIndex) {
		MatchedBlob activeBlob = blobs.get(activeIndex.blob);
		if (activeIndex.hit != -1)
			return activeBlob.getHits().get(activeIndex.hit).getBlobPath();
		else
			return activeBlob.getBlobPath();
	}
	
	private PlanarRange getActiveBlobMark(ActiveIndex activeIndex) {
		MatchedBlob activeBlob = blobs.get(activeIndex.blob);
		if (activeIndex.hit != -1)
			return activeBlob.getHits().get(activeIndex.hit).getTokenPos();
		else
			return null;
	}
	
	private ActiveIndex getPrevMatch() {
		if (prevMatchLink.isEnabled()) {
			ActiveIndex activeIndex = new ActiveIndex(activeBlobIndex, activeHitIndex);
			if (activeIndex.hit>0) {
				activeIndex.hit--;
				return activeIndex;
			} else {
				activeIndex.blob--;
				MatchedBlob activeBlob = blobs.get(activeIndex.blob);
				if (activeBlob.getHits().isEmpty())
					activeIndex.hit = -1;
				else
					activeIndex.hit = activeBlob.getHits().size()-1;
				return activeIndex;
			}
		} else {
			return null;
		}
	}
	
	private void onPrevMatch(AjaxRequestTarget target) {
		ActiveIndex activeIndex = getPrevMatch();
		
		if (activeIndex != null) {
			activeBlobIndex = activeIndex.blob;
			activeHitIndex = activeIndex.hit;
			onActiveIndexChange(target);
		}
	}
	
	private ActiveIndex getNextMatch() {
		if (nextMatchLink.isEnabled()) {
			ActiveIndex activeIndex = new ActiveIndex(activeBlobIndex, activeHitIndex);
			if (activeIndex.blob == -1) {
				activeIndex.blob = 0;
				MatchedBlob activeBlob = blobs.get(activeIndex.blob);
				if (activeBlob.getHits().isEmpty())
					activeIndex.hit = -1;
				else
					activeIndex.hit = 0;
			} else {
				MatchedBlob activeBlob = blobs.get(activeIndex.blob);
				activeIndex.hit++;
				if (activeIndex.hit==activeBlob.getHits().size()) {
					activeIndex.blob++;
					activeBlob = blobs.get(activeIndex.blob);
					if (activeBlob.getHits().isEmpty())
						activeIndex.hit = -1;
					else
						activeIndex.hit = 0;
				}
			}
			return activeIndex;
		} else {
			return null;
		}
	}
	
	private void onNextMatch(AjaxRequestTarget target) {
		ActiveIndex activeIndex = getNextMatch();
		
		if (activeIndex != null) {
			activeBlobIndex = activeIndex.blob;
			activeHitIndex = activeIndex.hit;
			onActiveIndexChange(target);
		}
	}
	
	private String getUrlPath(String blobPath) {
		ProjectBlobPage.State state = new ProjectBlobPage.State();
		state.blobIdent = new BlobIdent(context.getBlobIdent());
		state.blobIdent.path = blobPath;
		return RequestCycle.get().urlFor(ProjectBlobPage.class, ProjectBlobPage.paramsOf(context.getProject(), state)).toString();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String message = "(too many matches, displaying " + MAX_QUERY_ENTRIES + " of them)";
		add(new Label("hasMoreMessage", message).setVisible(hasMore));
		
		add(prevMatchLink = new AjaxLink<Void>("prevMatch") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				ActiveIndex activeIndex = getPrevMatch();
				if (activeIndex != null) {
					String prevBlobPath = getActiveBlobPath(activeIndex);
					PlanarRange prevTokenPos = getActiveBlobMark(activeIndex);
					
					String prevUrlPath = getUrlPath(prevBlobPath);
					attributes.getAjaxCallListeners().add(new ConfirmSwitchFileListener(prevUrlPath, prevTokenPos!=null));
				}
			}

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
		prevMatchLink.add(new DisabledAttributeLinkBehavior());
		prevMatchLink.setOutputMarkupId(true);
		
		add(nextMatchLink = new AjaxLink<Void>("nextMatch") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				ActiveIndex nextIndex = getNextMatch();
				if (nextIndex != null) {
					String nextBlobPath = getActiveBlobPath(nextIndex);
					PlanarRange nextTokenPos = getActiveBlobMark(nextIndex);
					
					String nextUrlPath = getUrlPath(nextBlobPath);
					attributes.getAjaxCallListeners().add(new ConfirmSwitchFileListener(nextUrlPath, nextTokenPos!=null));
				}
			}
			
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
		nextMatchLink.add(new DisabledAttributeLinkBehavior());
		nextMatchLink.setOutputMarkupId(true);
		
		add(new AjaxLink<Void>("expandAll") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				RequestCycle.get().setMetaData(ExpandStatusKey.INSTANCE, ExpandStatus.EXPAND_ALL);
				target.add(SearchResultPanel.this);
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
						
						add(new SpriteImage("icon", new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								if (blobItem.getModelObject().getHits().isEmpty()) 
									return "none";
								else 
									return "arrow";
							}
							
						}).add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								if (blobItem.getModelObject().getHits().isEmpty())
									return "none";
								else if (hitsContainer.isVisibilityAllowed()) 
									return "arrow-down";
								else 
									return "arrow-right";
							}
							
						})));
						
						setOutputMarkupId(true);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						setEnabled(!blobItem.getModelObject().getHits().isEmpty());
					}
					
				});
				
				blobItem.add(new ViewStateAwareAjaxLink<Void>("blobLink") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.setChannel(new AjaxChannel(NAV_CHANNEL, AjaxChannel.Type.DROP));
						attributes.getAjaxCallListeners().add(new ConfirmSwitchFileListener());
					}
					
					@Override
					protected void onInitialize() {
						super.onInitialize();
						
						String blobPath = blobItem.getModelObject().getBlobPath();
						add(new HighlightableLabel("label", blobPath, blobItem.getModelObject().getMatch()));
						
						if (activeBlobIndex == blobItem.getIndex() && activeHitIndex == -1)
							add(AttributeAppender.append("class", " active"));
						
						BlobIdent blobIdent = new BlobIdent(context.getBlobIdent().revision, blobPath, 
								FileMode.REGULAR_FILE.getBits());
						ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
						PageParameters params = ProjectBlobPage.paramsOf(context.getProject(), state);
						CharSequence url = RequestCycle.get().urlFor(ProjectBlobPage.class, params);
						add(AttributeAppender.replace("href", url.toString()));
						
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
						hitItem.add(new ViewStateAwareAjaxLink<Void>("hitLink") {

							@Override
							protected void onInitialize() {
								super.onInitialize();
								
								add(hit.renderIcon("icon"));
								if (hit.getTokenPos() != null)
									add(new Label("lineNo", String.valueOf(hit.getTokenPos().getFromRow()+1) + ":"));
								else
									add(new Label("lineNo").setVisible(false));
								add(hit.render("label"));
								add(new Label("scope", hit.getNamespace())
										.setVisible(!(hit instanceof TextHit) && hit.getNamespace()!=null));
								
								if (activeBlobIndex == blobItem.getIndex() && activeHitIndex == hitItem.getIndex())
									add(AttributeAppender.append("class", " active"));

								BlobIdent blobIdent = new BlobIdent(context.getBlobIdent().revision, hit.getBlobPath(), 
										FileMode.REGULAR_FILE.getBits());
								ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
								state.position = SourceRendererProvider.getPosition(hit.getTokenPos());
								PageParameters params = ProjectBlobPage.paramsOf(context.getProject(), state);
								CharSequence url = RequestCycle.get().urlFor(ProjectBlobPage.class, params);
								add(AttributeAppender.replace("href", url.toString()));
								
								setMarkupId(SearchResultPanel.this.getMarkupId() 
										+ "-" + blobItem.getIndex() + "-" + hitItem.getIndex());
							}

							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								super.updateAjaxAttributes(attributes);
								attributes.setChannel(new AjaxChannel(NAV_CHANNEL, AjaxChannel.Type.DROP));
								attributes.getAjaxCallListeners().add(new ConfirmSwitchFileListener());
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
		
		setOutputMarkupId(true);
	}

	protected abstract void onClose(AjaxRequestTarget target);

	private static class ExpandStatusKey extends MetaDataKey<ExpandStatus> {
		static final ExpandStatusKey INSTANCE = new ExpandStatusKey();		
	};
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new SearchResultResourceReference()));
	}

	private static class ActiveIndex {
		int blob;
		
		int hit;
		
		public ActiveIndex(int blob, int hit) {
			this.blob = blob;
			this.hit = hit;
		}
	}
	
}
