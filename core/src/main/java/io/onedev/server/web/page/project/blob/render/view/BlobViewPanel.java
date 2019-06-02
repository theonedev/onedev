package io.onedev.server.web.page.project.blob.render.view;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
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

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.FileUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.ajaxlistener.TrackViewStateListener;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.download.RawBlobDownloadResource;
import io.onedev.server.web.download.RawBlobDownloadResourceReference;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;

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
		if (SecurityUtils.canWriteCode(project.getFacade()) && context.isOnBranch()) {
			User user = OneDev.getInstance(UserManager.class).getCurrent();
			String revision = context.getBlobIdent().revision;
			String path = context.getBlobIdent().path;
			boolean reviewRequired = project.isReviewRequiredForModification(user, revision, path);
			boolean buildRequired = project.isBuildRequiredForModification(revision, path);

			if (isEditSupported()) {
				AjaxLink<Void> editLink = new ViewStateAwareAjaxLink<Void>("edit", true) {

					@Override
					protected void disableLink(ComponentTag tag) {
						super.disableLink(tag);
						
					}
					
					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
					}
					
					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						if (reviewRequired) {
							tag.append("class", "disabled", " ");
							tag.put("title", "Review required for this change. Submit pull request instead");
						} else if (buildRequired) {
							tag.append("class", "disabled", " ");
							tag.put("title", "Build required for this change. Submit pull request instead");
						} else {
							tag.put("title", "Edit on branch " + context.getBlobIdent().revision);
						}
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						context.onModeChange(target, Mode.EDIT);
					}
					
				};
				editLink.setEnabled(!reviewRequired && !buildRequired);
				
				changeActions.add(editLink);
			} else {
				changeActions.add(new WebMarkupContainer("edit").setVisible(false));
			}
			
			AjaxLink<Void> deleteLink = new ViewStateAwareAjaxLink<Void>("delete") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					if (reviewRequired) {
						tag.append("class", "disabled", " ");
						tag.put("title", "Review required for this change. Submit pull request instead");
					} else if (buildRequired) {
						tag.append("class", "disabled", " ");
						tag.put("title", "Build required for this change. Submit pull request instead");
					} else {
						tag.put("title", "Delete from branch " + context.getBlobIdent().revision);
					}
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

			deleteLink.setEnabled(!reviewRequired && !buildRequired);
			
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
		
		add(newOptions("options"));
		
		add(new ResourceLink<Void>("raw", new RawBlobDownloadResourceReference(), 
				RawBlobDownloadResource.paramsOf(context.getProject(), context.getBlobIdent())));
		add(new CheckBox("viewPlain", Model.of(context.getMode() == Mode.VIEW_PLAIN)) {
			
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
				if (context.getMode() == Mode.VIEW)
					context.onModeChange(target, Mode.VIEW_PLAIN);
				else
					context.onModeChange(target, Mode.VIEW);
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
		
		response.render(OnDomReadyHeaderItem.forScript(String.format("onedev.server.blobView.onDomReady('%s');", getMarkupId())));
	}

	public BlobRenderContext getContext() {
		return context;
	}
	
}
