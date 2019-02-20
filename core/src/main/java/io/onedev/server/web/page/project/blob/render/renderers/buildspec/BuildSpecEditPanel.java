package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.FormComponentPanel;

import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.edit.BlobEditPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditSupport;

@SuppressWarnings("serial")
public class BuildSpecEditPanel extends BlobEditPanel {

	private PlainEditPanel plainEditor;
	
	public BuildSpecEditPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildSpecCssResourceReference()));
	}

	@Override
	protected FormComponentPanel<byte[]> newEditor(String componentId, byte[] initialContent) {
		return new BuildSpecEditor(componentId, context, initialContent);
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
				return plainEditor = new PlainEditPanel(componentId, initialContent);
			}
			
		};
	}

}
