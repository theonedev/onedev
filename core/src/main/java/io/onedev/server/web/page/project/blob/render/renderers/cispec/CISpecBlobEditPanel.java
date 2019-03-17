package io.onedev.server.web.page.project.blob.render.renderers.cispec;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;

import io.onedev.server.ci.CISpec;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.edit.BlobEditPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditSupport;

@SuppressWarnings("serial")
public class CISpecBlobEditPanel extends BlobEditPanel {

	private CISpecEditPanel editor;
	
	private PlainEditPanel plainEditor;
	
	public CISpecBlobEditPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CISpecResourceReference()));
	}

	@Override
	protected FormComponentPanel<byte[]> newEditor(String componentId, byte[] initialContent) {
		return editor = new CISpecEditPanel(componentId, context, initialContent);
	}

	@Override
	protected Component newContentSubmitLink(String componentId) {
		return new AjaxSubmitLink(componentId) {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new IAjaxCallListener() {
					
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
					
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					
					@Override
					public CharSequence getCompleteHandler(Component component) {
						return null;
					}
					
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					
					@Override
					public CharSequence getBeforeHandler(Component component) {
						return String.format("onedev.server.plainEdit.onSubmit('%s');", 
								plainEditor.getMarkupId());
					}
					
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
				});
			}
			
		};
	}
	
	@Override
	protected PlainEditSupport getPlainEditSupport() {
		return new PlainEditSupport() {

			@Override
			public FormComponentPanel<String> newEditor(String componentId, String initialContent) {
				return plainEditor = new PlainEditPanel(componentId, CISpec.BLOB_PATH, initialContent);
			}
			
		};
	}

	@Override
	protected void onFormError(AjaxRequestTarget target, Form<?> form) {
		editor.onFormError(target, form);
	}

}
