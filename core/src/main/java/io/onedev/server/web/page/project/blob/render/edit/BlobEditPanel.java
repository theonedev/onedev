package io.onedev.server.web.page.project.blob.render.edit;

import java.nio.charset.Charset;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

import io.onedev.commons.utils.Provider;
import io.onedev.server.util.ContentDetector;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.page.project.blob.navigator.BlobNameChanging;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.blob.render.commitoption.CommitOptionPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditSupport;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;

@SuppressWarnings("serial")
public abstract class BlobEditPanel extends Panel {

	protected final BlobRenderContext context;
	
	private FormComponentPanel<byte[]> editor;
	
	private FormComponentPanel<String> plainEditor;
	
	private CommitOptionPanel commitOption;
	
	private AbstractDefaultAjaxBehavior recreateBehavior;
	
	private String tabSelectionInfo;	
	
	private byte[] editingContent;
		
	public BlobEditPanel(String id, BlobRenderContext context) {
		super(id);
		this.context = context;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("editPlainTab").setVisible(getPlainEditSupport() != null));
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
				context.onModeChange(target, Mode.VIEW);
			}
			
		});
		
		add(newEditOptions("editOptions"));
		
		add(new Form<Void>("content") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				byte[] content;
				if (context.getMode() == Mode.EDIT)
					content = context.getProject().getBlob(context.getBlobIdent()).getBytes();
				else
					content = new byte[0];
				add(editor = newEditor("editor", content));
				editor.setOutputMarkupId(true);
				
				PlainEditSupport plainEditSupport = getPlainEditSupport();

				if (plainEditSupport != null) {
					Charset detectedCharset = ContentDetector.detectCharset(content);
					Charset charset = detectedCharset!=null?detectedCharset:Charset.defaultCharset();
					add(plainEditor = plainEditSupport.newEditor("plainEditor", new String(content, charset)));
					plainEditor.setOutputMarkupId(true);
				} else { 
					add(new WebMarkupContainer("plainEditor").setVisible(false));
				}
				
				add(new HiddenField<String>("tabSelectionInfo", new PropertyModel<String>(BlobEditPanel.this, "tabSelectionInfo")));
				
				add(newContentSubmitLink("submit"));
				
				setOutputMarkupId(true);
			}

			@Override
			protected void onSubmit() {
				super.onSubmit();

				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				Preconditions.checkNotNull(target);
				
				String fromTab = StringUtils.substringBefore(tabSelectionInfo, " ");
				String toTab = StringUtils.substringAfter(tabSelectionInfo, " ");
				switch (toTab) {
				case "edit":
					if (fromTab.equals("edit-plain")) 
						editingContent = plainEditor.getModelObject().getBytes(Charsets.UTF_8);
					replace(editor = newEditor("editor", editingContent));
					editor.setOutputMarkupId(true);
					target.add(editor);
					break;
				case "edit-plain":
					if (fromTab.equals("edit")) 
						editingContent = editor.getModelObject();
					replace(plainEditor = getPlainEditSupport().newEditor("plainEditor", 
							new String(editingContent, Charsets.UTF_8)));
					plainEditor.setOutputMarkupId(true);
					target.add(plainEditor);
					break;
				case "save":
					if (fromTab.equals("edit"))
						editingContent = editor.getModelObject();
					else
						editingContent = plainEditor.getModelObject().getBytes(Charsets.UTF_8);
					commitOption.onContentChange(target);
					break;
				}
				
				String script = String.format(
						"onedev.server.blobEdit.selectTab($('#%s>.blob-edit>.head>.%s'));", 
						BlobEditPanel.this.getMarkupId(true), toTab);
				
				target.appendJavaScript(script);
			}

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}

		});
		
		add(commitOption = new CommitOptionPanel("commitOptions", context, new Provider<byte[]>() {

			@Override
			public byte[] get() {
				return editingContent;
			}
			
		}));
		
		add(recreateBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				// recreate content editor as different name might be using different editor
				context.onModeChange(target, Mode.ADD);
			}
			
		});
		
		if (context.getMode() == Mode.ADD)
			add(AttributeAppender.append("class", "no-autofocus"));
		
		setOutputMarkupId(true);
	}

	protected Component newContentSubmitLink(String componentId) {
		return new AjaxSubmitLink(componentId) {
		};
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

	@Nullable
	protected abstract PlainEditSupport getPlainEditSupport();

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new BlobEditResourceReference()));
		
		String script = String.format("onedev.server.blobEdit.onDomReady('%s');", getMarkupId()); 
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
