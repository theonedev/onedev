package com.gitplex.server.web.page.depot.blob.render.view;

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

import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.FileUtils;
import com.gitplex.server.web.component.link.ViewStateAwareAjaxLink;
import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext.Mode;
import com.gitplex.server.web.util.ajaxlistener.ConfirmLeaveListener;
import com.gitplex.server.web.util.resource.RawBlobResource;
import com.gitplex.server.web.util.resource.RawBlobResourceReference;
import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public abstract class BlobViewPanel extends Panel {

	protected final BlobRenderContext context;
	
	public BlobViewPanel(String id, BlobRenderContext context) {
		super(id);
		
		BlobIdent blobIdent = context.getBlobIdent();
		Preconditions.checkArgument(blobIdent.revision != null 
				&& blobIdent.path != null && blobIdent.mode != null);
		
		this.context = context;
	}
	
	protected abstract boolean canEdit();
	
	protected abstract boolean canBlame();
	
	protected WebMarkupContainer newOptions(String id) {
		WebMarkupContainer options = new WebMarkupContainer(id);
		options.setVisible(false);
		return options;
	}
	
	protected WebMarkupContainer newAdditionalActions(String id) {
		return new WebMarkupContainer(id);
	}

	private void newChangeActions(@Nullable IPartialPageRequestHandler target) {
		WebMarkupContainer changeActions = new WebMarkupContainer("changeActions");
		changeActions.setVisible(SecurityUtils.canModify(context.getDepot(), context.getBlobIdent().revision, context.getBlobIdent().path) 
				&& (context.isOnBranch()));
		changeActions.setOutputMarkupId(true);
		
		if (canEdit()) {
			AjaxLink<Void> editLink = new ViewStateAwareAjaxLink<Void>("edit", true) {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
				}
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					context.onModeChange(target, Mode.EDIT);
				}
				
			};
			editLink.add(AttributeAppender.append("title", "Edit on branch " + context.getBlobIdent().revision));
			changeActions.add(editLink);
		} else {
			changeActions.add(new WebMarkupContainer("edit").setVisible(false));
		}
		
		AjaxLink<Void> deleteLink = new ViewStateAwareAjaxLink<Void>("delete") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				context.onModeChange(target, Mode.DELETE);
			}

		};
		deleteLink.add(AttributeAppender.append("title", "Delete from branch " + context.getBlobIdent().revision));
		changeActions.add(deleteLink);
		
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
		
		add(new ResourceLink<Void>("raw", new RawBlobResourceReference(), 
				RawBlobResource.paramsOf(context.getDepot(), context.getBlobIdent())));
		add(new ViewStateAwareAjaxLink<Void>("blame", true) {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				if (context.getMode() == Mode.BLAME)
					context.onModeChange(target, Mode.VIEW);
				else
					context.onModeChange(target, Mode.BLAME);
			}

		}.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (context.getMode() == Mode.BLAME)
					return "active";
				else
					return "";
			}
			
		})));
		
		add(newAdditionalActions("extraActions"));
		newChangeActions(null);
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new BlobViewResourceReference()));
		
		response.render(OnDomReadyHeaderItem.forScript(String.format("gitplex.server.blobView('%s');", getMarkupId())));
	}

	public BlobRenderContext getContext() {
		return context;
	}
	
}
