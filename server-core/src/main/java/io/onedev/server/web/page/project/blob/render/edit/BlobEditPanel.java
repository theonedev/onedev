package io.onedev.server.web.page.project.blob.render.edit;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.flow.RedirectToUrlException;

import io.onedev.server.util.Provider;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.blob.navigator.BlobNameChanging;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.blob.render.commitoption.CommitOptionPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditSupport;

public abstract class BlobEditPanel extends Panel {

	public static enum Tab {EDIT, EDIT_PLAIN, CHANGES, SAVE, FLUSH};
	
	protected final BlobRenderContext context;
	
	private Form<?> form;

	private FormComponentPanel<byte[]> editor;
		
	private CommitOptionPanel commitOption;	
	
	private AbstractDefaultAjaxBehavior recreateBehavior;
	
	private Tab currentTab = Tab.EDIT;
			
	public BlobEditPanel(String id, BlobRenderContext context) {
		super(id);
		this.context = context;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("editPlainTab") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (BlobEditPanel.this instanceof PlainEditSupport)
					add(((PlainEditSupport)BlobEditPanel.this).renderTabHead("tabHead"));
				else
					add(new WebMarkupContainer("tabHead"));
			}

		}.setVisible(this instanceof PlainEditSupport));

		add(new WebMarkupContainer("changesTab")
				.setVisible(context.getMode() != Mode.ADD || context.getNewPath() != null));

		add(new WebMarkupContainer("saveTab")
				.setVisible(context.getMode() != Mode.ADD || context.getNewPath() != null));
		
		add(new ViewStateAwareAjaxLink<Void>("cancelLink", true) {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (currentTab == Tab.EDIT && editor instanceof EditCompleteAware)  
					((EditCompleteAware)editor).onEditCancel(target); 
				if (context.getUrlBeforeEdit() != null) 
					throw new RedirectToUrlException(context.getUrlBeforeEdit());
				else 
					context.onModeChange(target, Mode.VIEW, null);
			}
			
		});
				
		add(newEditOptions("editOptions"));
		
		add(form = new Form<Void>("form") {

			private AjaxSubmitLink newSubmitLink(String componentId, Tab tab) {
				return new AjaxSubmitLink(componentId) {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						
						if (tab == Tab.FLUSH) {
							if (currentTab == Tab.EDIT 
									&& editor instanceof EditCompleteAware 
									&& !((EditCompleteAware)editor).onEditComplete(target)) {
								onError(target, form);
							} else {
								onFlushed(target, editor.getModelObject());
							}
						} else {
							if (currentTab == Tab.EDIT 
									&& editor instanceof EditCompleteAware 
									&& !((EditCompleteAware)editor).onEditComplete(target)) {
								onError(target, form);
							} else {
								var editingContent = editor.getModelObject();
								if (tab == Tab.EDIT) {
									editor = newEditor("editor", editingContent);
									form.replace(editor);
									updateForm(target);
									hideChangesViewer(target);
								} else if (tab == Tab.EDIT_PLAIN) {
									editor = ((PlainEditSupport)BlobEditPanel.this).newPlainEditor("editor", editingContent);
									form.replace(editor);
									updateForm(target);
									hideChangesViewer(target);
								} else if (tab == Tab.CHANGES) {
									showChangesViewer(target);
								} else {
									commitOption.onContentChange(target);
									hideChangesViewer(target);
								}

								String script = String.format(
										"onedev.server.blobEdit.selectTab($('#%s>.blob-edit>.head>.%s'));", 
										BlobEditPanel.this.getMarkupId(true), tab.name().toLowerCase().replace("_", "-"));
								target.appendJavaScript(script);								
								currentTab = tab;
							}
						}
					}

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						updateForm(target);
						if (tab == Tab.FLUSH)
							onFlushError(target);
					}

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						
						attributes.getAjaxCallListeners().add(new AjaxCallListener()
								.onBefore("$('.blob-submit-aware').trigger('beforeSubmit');"));
					}
					
				};
			}
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(new FencedFeedbackPanel("feedback", this));
				
				byte[] bytes;
				if (context.getMode() == Mode.EDIT) {
					var blob = context.getProject().getBlob(context.getBlobIdent(), true);
					bytes = blob.getBytes();
					if (blob.getText() != null && !Arrays.equals(bytes, blob.getText().getContent().getBytes(StandardCharsets.UTF_8))) {
						var message = String.format(
								"<svg class='icon icon-sm mr-1'><use xlink:href='%s'/></svg> %s will be transcoded to UTF-8 upon commit",
								SpriteImage.getVersionedHref("warning-o"), blob.getText().getCharset().name());
						BlobEditPanel.this.add(new Label("transcodeWarning", message).setEscapeModelStrings(false));
					}
				} else {
					bytes = new byte[0];
				}
				add(editor = newEditor("editor", bytes));
				editor.setOutputMarkupId(true);
				
				add(newSubmitLink("edit", Tab.EDIT));
				add(newSubmitLink("editPlain", Tab.EDIT_PLAIN));
				add(newSubmitLink("changes", Tab.CHANGES));
				add(newSubmitLink("save", Tab.SAVE));
				add(newSubmitLink("flush", Tab.FLUSH));

				if (BlobEditPanel.this.get("transcodeWarning") == null)
					BlobEditPanel.this.add(new WebMarkupContainer("transcodeWarning").setVisible(false));					
				setOutputMarkupId(true);
			}

		});
		
		add(newChangesViewerPlaceholder());

		add(commitOption = new CommitOptionPanel("commitOptions", context, new Provider<byte[]>() {

			@Override
			public byte[] get() {
				return editor.getModelObject();
			}
			
		}));
		
		add(recreateBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				// recreate content editor as different name might be using different editor
				context.onModeChange(target, Mode.ADD, null);
			}
			
		});
		
		if (context.getMode() == Mode.ADD)
			add(AttributeAppender.append("class", "no-autofocus"));
		
		setOutputMarkupId(true);
	}

	private void showChangesViewer(IPartialPageRequestHandler handler) {
		var changesViewer = newChangesViewer("changesViewer", getInitialContent(), editor.getModelObject());
		changesViewer.setOutputMarkupPlaceholderTag(true);
		replace(changesViewer);
		handler.add(changesViewer);
	}

	private void hideChangesViewer(AjaxRequestTarget target) {
		var changesViewer = newChangesViewerPlaceholder();
		replace(changesViewer);
		target.add(changesViewer);	
	}

	private Component newChangesViewerPlaceholder() {
		var changesViewer = new WebMarkupContainer("changesViewer");
		changesViewer.setVisible(false);
		changesViewer.setOutputMarkupPlaceholderTag(true);
		return changesViewer;
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		if (event.getPayload() instanceof BlobNameChanging) {
			/*
			 * Blob name is changing and current editor might be inappropriate for current  
			 * blob name, so we need to re-create the editor if the form does not have 
			 * any change yet
			 */
			BlobNameChanging payload = (BlobNameChanging) event.getPayload();
			String script = String.format("onedev.server.blobEdit.onNameChanging('%s', %s, %s);", 
					getMarkupId(), context.getMode() == Mode.ADD, recreateBehavior.getCallbackFunction());
			payload.getHandler().appendJavaScript(script);
		}
	}

	protected WebMarkupContainer newEditOptions(String componentId) {
		WebMarkupContainer options = new WebMarkupContainer(componentId);
		options.setVisible(false);
		return options;
	}
	
	protected abstract FormComponentPanel<byte[]> newEditor(String componentId, byte[] initialContent);

	protected abstract Component newChangesViewer(String componentId, byte[] initialContent, byte[] editingContent);

	public FormComponentPanel<byte[]> getEditor() {
		return editor;
	}

	private void updateForm(IPartialPageRequestHandler handler) {
		handler.prependJavaScript(String.format("onedev.server.blobEdit.recordFormFlags('%s');", form.getMarkupId()));
		handler.add(form);
		handler.appendJavaScript(String.format("onedev.server.blobEdit.restoreFormFlags('%s');", form.getMarkupId()));
		handler.appendJavaScript("$(window).resize();");
	}

	protected void updateEditingContent(IPartialPageRequestHandler handler, byte[] editingContent) {
		if (currentTab == Tab.EDIT) 
			editor = newEditor("editor", editingContent);
		else
			editor = ((PlainEditSupport)BlobEditPanel.this).newPlainEditor("editor", editingContent);
		form.replace(editor);

		if (currentTab == Tab.EDIT || currentTab == Tab.EDIT_PLAIN) {
			updateForm(handler);
		} else {
			// Update editor instead of form to keep form invisible
			editor.setOutputMarkupId(true);
			handler.add(editor);
		}		
		handler.appendJavaScript(String.format("onedev.server.form.markDirty($('#%s'));", form.getMarkupId()));

		if (currentTab == Tab.CHANGES) {
			showChangesViewer(handler);
		} else if (currentTab == Tab.SAVE) {
			commitOption.onContentChange(handler);
		}
	}

	private byte[] getInitialContent() {
		if (context.getMode() == Mode.EDIT) {
			var blob = context.getProject().getBlob(context.getBlobIdent(), true);
			return blob.getBytes();
		} else {
			return new byte[0];
		}
	}

	protected Tab getCurrentTab() {
		return currentTab;
	}

	protected void requestToFlush(IPartialPageRequestHandler handler) {
		handler.appendJavaScript(String.format("$('#%s>.blob-edit>.body>form>.flush').click();", getMarkupId()));
	}
	
	protected void onFlushed(IPartialPageRequestHandler handler, byte[] editingContent) {		
	}

	protected void onFlushError(IPartialPageRequestHandler handler) {		
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new BlobEditResourceReference()));
		
		String script = String.format("onedev.server.blobEdit.onDomReady('%s');", getMarkupId()); 
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
