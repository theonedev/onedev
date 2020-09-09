package io.onedev.server.web.page.project.blob.render.view;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.FileUtils;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.ajaxlistener.TrackViewStateListener;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.resource.RawBlobResource;
import io.onedev.server.web.resource.RawBlobResourceReference;

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
	
	protected abstract boolean isViewPlainSupported();
	
	protected WebMarkupContainer newFormats(String id) {
		WebMarkupContainer options = new WebMarkupContainer(id);
		options.setVisible(false);
		return options;
	}
	
	protected WebMarkupContainer newExtraOptions(String id) {
		return new WebMarkupContainer(id);
	}

	private void newChangeActions(@Nullable IPartialPageRequestHandler target) {
		WebMarkupContainer changeActions = new WebMarkupContainer("changeActions");

		Project project = context.getProject();
		if (SecurityUtils.canWriteCode(project) && context.isOnBranch()) {
			User user = SecurityUtils.getUser();
			String revision = context.getBlobIdent().revision;
			String path = context.getBlobIdent().path;
			boolean reviewRequired = project.isReviewRequiredForModification(user, revision, path);
			boolean buildRequired = project.isBuildRequiredForModification(user, revision, path);

			WebMarkupContainer edit = new WebMarkupContainer("edit");
			changeActions.add(edit);
			if (isEditSupported()) {
				String title;
				if (reviewRequired) 
					title = "Review required for this change. Submit pull request instead";
				else if (buildRequired) 
					title = "Build required for this change. Submit pull request instead";
				else 
					title = "Edit on branch " + context.getBlobIdent().revision;
				
				edit.add(AttributeAppender.append("title", title));
				
				AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link", true) {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						context.onModeChange(target, Mode.EDIT, null);
					}
					
				};
				if (reviewRequired || buildRequired) {
					link.add(AttributeAppender.append("class", "disabled"));
					link.setEnabled(false);
				}
				
				edit.add(link);
			} else {
				edit.add(new WebMarkupContainer("link").setVisible(false));
			}
			
			WebMarkupContainer delete = new WebMarkupContainer("delete");
			changeActions.add(delete);
			
			String title;
			if (reviewRequired) 
				title = "Review required for this change. Submit pull request instead";
			else if (buildRequired) 
				title = "Build required for this change. Submit pull request instead";
			else 
				title = "Delete from branch " + context.getBlobIdent().revision;
			
			delete.add(AttributeAppender.append("title", title));
			
			AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
				}
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					context.onModeChange(target, Mode.DELETE, null);
				}

			};

			if (reviewRequired || buildRequired) {
				link.add(AttributeAppender.append("class", "disabled"));
				link.setEnabled(false);
			}
			
			delete.add(link);
			
		} else {
			changeActions.setVisible(false);
			
			WebMarkupContainer edit = new WebMarkupContainer("edit");
			edit.add(new WebMarkupContainer("link"));
			changeActions.add(edit);
			
			WebMarkupContainer delete = new WebMarkupContainer("delete");
			delete.add(new WebMarkupContainer("link"));
			changeActions.add(delete);
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
				return context.getProject().getBlob(context.getBlobIdent(), true).getText().getLines().size() + " lines";
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(context.getProject().getBlob(context.getBlobIdent(), true).getText() != null);
			}
			
		});
		
		add(new Label("charset", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return context.getProject().getBlob(context.getBlobIdent(), true).getText().getCharset().displayName();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(context.getProject().getBlob(context.getBlobIdent(), true).getText() != null);
			}
			
		});
		
		add(new Label("size", FileUtils.byteCountToDisplaySize(context.getProject().getBlob(context.getBlobIdent(), true).getSize())));
		
		add(newFormats("formats"));
		
		add(new ResourceLink<Void>("raw", new RawBlobResourceReference(), 
				RawBlobResource.paramsOf(context.getProject(), context.getBlobIdent())));
		add(new CheckBox("viewPlain", Model.of(context.getMode() == Mode.VIEW && context.isViewPlain())) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(isViewPlainSupported());
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
				context.onModeChange(target, Mode.VIEW, !context.isViewPlain(), null);
			}
			
		}));

		add(new CheckBox("blame", Model.of(context.getMode() == Mode.BLAME)) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(context.getProject().getBlob(context.getBlobIdent(), true).getText() != null);
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
					context.onModeChange(target, Mode.VIEW, null);
				else
					context.onModeChange(target, Mode.BLAME, null);
			}
			
		}));
		
		add(newExtraOptions("extraOptions"));
		newChangeActions(null);
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BlobViewCssResourceReference()));
	}

	public BlobRenderContext getContext() {
		return context;
	}
	
}
