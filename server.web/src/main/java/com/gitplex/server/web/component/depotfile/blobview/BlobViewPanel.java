package com.gitplex.server.web.component.depotfile.blobview;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.cycle.RequestCycle;

import com.google.common.base.Preconditions;
import com.gitplex.commons.git.BlobIdent;
import com.gitplex.commons.util.FileUtils;
import com.gitplex.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.gitplex.commons.wicket.component.PreventDefaultAjaxLink;
import com.gitplex.commons.wicket.component.ViewStateAwareAjaxLink;
import com.gitplex.server.core.security.SecurityUtils;
import com.gitplex.server.web.page.depot.file.DepotFilePage;
import com.gitplex.server.web.resource.BlobResource;
import com.gitplex.server.web.resource.BlobResourceReference;

@SuppressWarnings("serial")
public abstract class BlobViewPanel extends Panel {

	protected final BlobViewContext context;
	
	public BlobViewPanel(String id, BlobViewContext context) {
		super(id);
		
		BlobIdent blobIdent = context.getBlobIdent();
		Preconditions.checkArgument(blobIdent.revision != null 
				&& blobIdent.path != null && blobIdent.mode != null);
		
		this.context = context;
	}
	
	protected WebMarkupContainer newOptions(String id) {
		return new WebMarkupContainer(id);
	}
	
	protected WebMarkupContainer newAdditionalActions(String id) {
		return new WebMarkupContainer(id);
	}

	private void newChangeActions(@Nullable IPartialPageRequestHandler target) {
		WebMarkupContainer changeActions = new WebMarkupContainer("changeActions");
		changeActions.setVisible(SecurityUtils.canModify(context.getDepot(), context.getBlobIdent().revision, context.getBlobIdent().path) 
				&& (context.isOnBranch()));
		changeActions.setOutputMarkupId(true);
		
		if (context.getDepot().getBlob(context.getBlobIdent()).getText() != null) {
			if (context.isOnBranch()) {
				AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("edit") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
					}
					
					@Override
					public void onClick(AjaxRequestTarget target, String viewState) {
						RequestCycle.get().setMetaData(DepotFilePage.VIEW_STATE_KEY, viewState);
						context.onEdit(target);
					}
				};
				link.add(AttributeAppender.append("title", "Edit on branch " + context.getBlobIdent().revision));
				changeActions.add(link);
			} else {
				changeActions.add(new WebMarkupContainer("edit").setVisible(false));
			}
		} else {
			changeActions.add(new WebMarkupContainer("edit").setVisible(false));
		}
		
		if (context.isOnBranch()) {
			AjaxLink<Void> link = new PreventDefaultAjaxLink<Void>("delete") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
				}
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					context.onDelete(target);
				}

			};
			link.add(AttributeAppender.append("title", "Delete from branch " + context.getBlobIdent().revision));
			changeActions.add(link);
		} else {
			changeActions.add(new WebMarkupContainer("delete").setVisible(false));
		}
		
		if (target != null) {
			replace(changeActions);
			target.add(changeActions);
		} else {
			add(changeActions);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("lines", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return context.getDepot().getBlob(context.getBlobIdent()).getText().getLines().size() + " lines";
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(context.getDepot().getBlob(context.getBlobIdent()).getText() != null);
			}
			
		});
		
		add(new Label("charset", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return context.getDepot().getBlob(context.getBlobIdent()).getText().getCharset().displayName();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(context.getDepot().getBlob(context.getBlobIdent()).getText() != null);
			}
			
		});
		
		add(new Label("size", FileUtils.byteCountToDisplaySize(context.getDepot().getBlob(context.getBlobIdent()).getSize())));
		
		add(newOptions("options"));
		
		add(new ResourceLink<Void>("raw", new BlobResourceReference(), 
				BlobResource.paramsOf(context.getDepot(), context.getBlobIdent())));
		add(newAdditionalActions("additionalActions"));
		newChangeActions(null);
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new BlobViewResourceReference()));
		
		response.render(OnDomReadyHeaderItem.forScript(String.format("gitplex.server.blobView('%s');", getMarkupId())));
	}

	public BlobViewContext getContext() {
		return context;
	}
	
}
