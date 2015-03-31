package com.pmease.gitplex.web.component.search;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.gitplex.search.hit.FileHit;
import com.pmease.gitplex.search.hit.MatchedBlob;
import com.pmease.gitplex.search.hit.QueryHit;

@SuppressWarnings("serial")
public abstract class BlobAdvancedSearchResultPanel extends Panel {

	private final List<MatchedBlob> blobs;
	
	private final String hasMoreMessage;
	
	public BlobAdvancedSearchResultPanel(String id, List<MatchedBlob> blobs, @Nullable String hasMoreMessage) {
		super(id);
		
		this.blobs = blobs;
		this.hasMoreMessage = hasMoreMessage;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new Label("hasMoreMessage", hasMoreMessage).setVisible(hasMoreMessage!=null));
		
		add(new ListView<MatchedBlob>("blobs", blobs) {

			@Override
			protected void populateItem(final ListItem<MatchedBlob> item) {
				final WebMarkupContainer hitsContainer = new WebMarkupContainer("hits") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						setVisible(!item.getModelObject().getHits().isEmpty());
					}
					
				};
				hitsContainer.setVisibilityAllowed(false);
				hitsContainer.setOutputMarkupPlaceholderTag(true);
				item.add(hitsContainer);
				
				hitsContainer.add(new ListView<QueryHit>("hits", item.getModelObject().getHits()) {

					@Override
					protected void populateItem(final ListItem<QueryHit> item) {
						item.add(new Label("lineNo", String.valueOf(item.getModelObject().getLineNo()+1) + ":"));
						item.add(new AjaxLink<Void>("lineLink") {

							@Override
							protected void onInitialize() {
								super.onInitialize();
								add(item.getModelObject().render("label"));
							}

							@Override
							public void onClick(AjaxRequestTarget target) {
								onSelect(target, item.getModelObject());
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
									return "fa fa-fw fa-angle-right";
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
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(BlobSearchPanel.class, "blob-search.css")));
	}

	protected abstract void onSelect(AjaxRequestTarget target, QueryHit hit);
	
}
