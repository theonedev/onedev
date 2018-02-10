package com.turbodev.server.web.page.project.blob.render.view;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;

import com.turbodev.utils.FileUtils;
import com.google.common.base.Preconditions;
import com.turbodev.server.TurboDev;
import com.turbodev.server.git.BlobIdent;
import com.turbodev.server.manager.ProjectManager;
import com.turbodev.server.manager.UserManager;
import com.turbodev.server.model.Project;
import com.turbodev.server.model.User;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.web.component.link.ViewStateAwareAjaxLink;
import com.turbodev.server.web.page.project.blob.render.BlobRenderContext;
import com.turbodev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import com.turbodev.server.web.util.ajaxlistener.ConfirmLeaveListener;
import com.turbodev.server.web.util.ajaxlistener.TrackViewStateListener;
import com.turbodev.server.web.util.resource.RawBlobResource;
import com.turbodev.server.web.util.resource.RawBlobResourceReference;

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
	
	protected abstract boolean isEditSupported();
	
	protected abstract boolean isBlameSupported();
	
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

		Project project = context.getProject();
		if (SecurityUtils.canWrite(project) && context.isOnBranch()) {
			ProjectManager projectManager = TurboDev.getInstance(ProjectManager.class);
			User user = TurboDev.getInstance(UserManager.class).getCurrent();
			boolean needsQualityCheck = projectManager.isModificationNeedsQualityCheck(
					user, project, context.getBlobIdent().revision, context.getBlobIdent().path);

			if (isEditSupported()) {
				AjaxLink<Void> editLink = new ViewStateAwareAjaxLink<Void>("edit", true) {

					@Override
					protected void disableLink(ComponentTag tag) {
						super.disableLink(tag);
						tag.append("class", "disabled", " ");
						tag.put("title", "Direct edit not allowed. Submit pull request for review instead");
					}
					
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
				if (needsQualityCheck)
					editLink.setEnabled(false);
				else
					editLink.add(AttributeAppender.append("title", "Edit on branch " + context.getBlobIdent().revision));
				
				changeActions.add(editLink);
			} else {
				changeActions.add(new WebMarkupContainer("edit").setVisible(false));
			}
			
			AjaxLink<Void> deleteLink = new ViewStateAwareAjaxLink<Void>("delete") {

				@Override
				protected void disableLink(ComponentTag tag) {
					super.disableLink(tag);
					tag.append("class", "disabled", " ");
					tag.put("title", "Direct deletion not allowed. Submit pull request for review instead");
				}

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

			if (needsQualityCheck)
				deleteLink.setEnabled(false);
			else
				deleteLink.add(AttributeAppender.append("title", "Delete from branch " + context.getBlobIdent().revision));
			
			changeActions.add(deleteLink);
			
		} else {
			changeActions.setVisible(false);
			changeActions.add(new WebMarkupContainer("edit"));
			changeActions.add(new WebMarkupContainer("delete"));
		}
		
		changeActions.setOutputMarkupId(true);
		
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
				return context.getProject().getBlob(context.getBlobIdent()).getText().getLines().size() + " lines";
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(context.getProject().getBlob(context.getBlobIdent()).getText() != null);
			}
			
		});
		
		add(new Label("charset", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return context.getProject().getBlob(context.getBlobIdent()).getText().getCharset().displayName();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(context.getProject().getBlob(context.getBlobIdent()).getText() != null);
			}
			
		});
		
		add(new Label("size", FileUtils.byteCountToDisplaySize(context.getProject().getBlob(context.getBlobIdent()).getSize())));
		
		add(newOptions("options"));
		
		add(new ResourceLink<Void>("raw", new RawBlobResourceReference(), 
				RawBlobResource.paramsOf(context.getProject(), context.getBlobIdent())));
		add(new CheckBox("blame", Model.of(context.getMode() == Mode.BLAME)) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(context.getProject().getBlob(context.getBlobIdent()).getText() != null);
			}

		}.add(new OnChangeAjaxBehavior() {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setMethod(Method.POST);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
				attributes.getAjaxCallListeners().add(new TrackViewStateListener(true));
			}
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (context.getMode() == Mode.BLAME)
					context.onModeChange(target, Mode.VIEW);
				else
					context.onModeChange(target, Mode.BLAME);
			}
			
		}));

		add(newAdditionalActions("extraActions"));
		newChangeActions(null);
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new BlobViewResourceReference()));
		
		response.render(OnDomReadyHeaderItem.forScript(String.format("turbodev.server.blobView.onDomReady('%s');", getMarkupId())));
	}

	public BlobRenderContext getContext() {
		return context;
	}
	
}
