package io.onedev.server.web.page.project.blob.render.edit;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.flow.RedirectToUrlException;

import io.onedev.server.util.Provider;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.blob.navigator.BlobNameChanging;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.blob.render.commitoption.CommitOptionPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditSupport;

@SuppressWarnings("serial")
public abstract class BlobEditPanel extends Panel {

	public static enum Tab {EDIT, EDIT_PLAIN, SAVE};
	
	protected final BlobRenderContext context;
	
	private FormComponentPanel<byte[]> editor;
	
	private CommitOptionPanel commitOption;
	
	private AbstractDefaultAjaxBehavior recreateBehavior;
	
	private Tab currentTab = Tab.EDIT;
	
	private byte[] editingContent;
	
	private String position;
		
	public BlobEditPanel(String id, BlobRenderContext context) {
		super(id);
		this.context = context;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("editPlainTab").setVisible(this instanceof PlainEditSupport));
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
				if (context.getUrlBeforeEdit() != null) 
					throw new RedirectToUrlException(context.getUrlBeforeEdit());
				else 
					context.onModeChange(target, Mode.VIEW, null);
			}
			
		});
		
		add(newEditOptions("editOptions"));
		
		add(new Form<Void>("content") {

			private AjaxSubmitLink newSubmitLink(String componentId, Tab tab) {
				return new AjaxSubmitLink(componentId) {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						if (currentTab == Tab.EDIT && tab == Tab.EDIT_PLAIN 
								|| currentTab == Tab.EDIT_PLAIN && tab == Tab.EDIT
								|| tab == Tab.SAVE) {
							editingContent = editor.getModelObject();
						}
						if (tab != Tab.SAVE) {
							if (tab == Tab.EDIT)
								editor = newEditor("editor", editingContent);
							else
								editor = ((PlainEditSupport)BlobEditPanel.this).newPlainEditor("editor", editingContent);
							getParent().replace(editor);
						} else {
							commitOption.onContentChange(target);
						}

						String script = String.format(
								"onedev.server.blobEdit.selectTab($('#%s>.blob-edit>.head>.%s'));", 
								BlobEditPanel.this.getMarkupId(true), tab.name().toLowerCase().replace("_", "-"));
						target.appendJavaScript(script);
						
						currentTab = tab;

						target.prependJavaScript(String.format("onedev.server.blobEdit.recordFormFlags('%s');", form.getMarkupId()));
						target.add(form);
						target.appendJavaScript(String.format("onedev.server.blobEdit.restoreFormFlags('%s');", form.getMarkupId()));
					}

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(form);
						((BasePage)getPage()).resizeWindow(target);
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
				
				byte[] content;
				if (context.getMode() == Mode.EDIT)
					content = context.getProject().getBlob(context.getBlobIdent(), true).getBytes();
				else
					content = new byte[0];
				add(editor = newEditor("editor", content));
				editor.setOutputMarkupId(true);
				
				add(newSubmitLink("edit", Tab.EDIT));
				add(newSubmitLink("editPlain", Tab.EDIT_PLAIN));
				add(newSubmitLink("save", Tab.SAVE));
				add(new HiddenField<String>("position", new PropertyModel<String>(BlobEditPanel.this, "position")));
				
				setOutputMarkupId(true);
			}

		});
		
		add(commitOption = new CommitOptionPanel("commitOptions", context, new Provider<byte[]>() {

			@Override
			public byte[] get() {
				return editingContent;
			}
			
		}) {

			@Override
			protected String getPosition() {
				return BlobEditPanel.this.getPosition();
			}

		});
		
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

	public FormComponentPanel<byte[]> getEditor() {
		return editor;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new BlobEditResourceReference()));
		
		String script = String.format("onedev.server.blobEdit.onDomReady('%s');", getMarkupId()); 
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Nullable
	protected String getPosition() {
		return position;
	}
	
}
