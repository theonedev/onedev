package com.gitplex.server.web.page.project.blob.render.renderers.source;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.unbescape.javascript.JavaScriptEscape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitplex.server.GitPlex;
import com.gitplex.server.model.support.TextRange;
import com.gitplex.server.web.component.sourceformat.OptionChangeCallback;
import com.gitplex.server.web.component.sourceformat.SourceFormatPanel;
import com.gitplex.server.web.page.project.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.project.blob.render.BlobRenderContext.Mode;
import com.gitplex.server.web.page.project.blob.render.edit.BlobEditPanel;
import com.gitplex.server.web.page.project.blob.render.view.Markable;

@SuppressWarnings("serial")
public class SourceEditPanel extends BlobEditPanel implements Markable {

	private SourceFormComponent sourceFormComponent;
	
	private SourceFormatPanel sourceFormat;
	
	public SourceEditPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected FormComponentPanel<byte[]> newContentEditor(String componentId, byte[] initialContent) {
		sourceFormComponent = new SourceFormComponent(componentId, initialContent);
		return sourceFormComponent;
	}

	@Override
	protected WebMarkupContainer newEditOptions(String componentId) {
		sourceFormat = new SourceFormatPanel(componentId, new OptionChangeCallback() {

			@Override
			public void onOptioneChange(AjaxRequestTarget target) {
				String script = String.format("gitplex.server.sourceEdit.onIndentTypeChange('%s', '%s');", 
						sourceFormComponent.getMarkupId(), sourceFormat.getIndentType());
				target.appendJavaScript(script);
			}
			
		}, new OptionChangeCallback() {

			@Override
			public void onOptioneChange(AjaxRequestTarget target) {
				String script = String.format("gitplex.server.sourceEdit.onTabSizeChange('%s', %s);", 
						sourceFormComponent.getMarkupId(), sourceFormat.getTabSize());
				target.appendJavaScript(script);
			}
			
		}, new OptionChangeCallback() {
			
			@Override
			public void onOptioneChange(AjaxRequestTarget target) {
				String script = String.format("gitplex.server.sourceEdit.onLineWrapModeChange('%s', '%s');", 
						sourceFormComponent.getMarkupId(), sourceFormat.getLineWrapMode());
				target.appendJavaScript(script);
			}
			
		});	
		return sourceFormat;
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
						return String.format("gitplex.server.sourceEdit.onSubmit('%s');", 
								sourceFormComponent.getMarkupId());
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
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new SourceEditResourceReference()));

		String autosaveKey = JavaScriptEscape.escapeJavaScript(context.getAutosaveKey());
		String jsonOfMark = context.getMark()!=null?getJson(context.getMark()):"undefined"; 
		String script = String.format("gitplex.server.sourceEdit.onDomReady('%s', '%s', %s, '%s', %s, '%s', %b, '%s');", 
				sourceFormComponent.getMarkupId(), 
				JavaScriptEscape.escapeJavaScript(context.getNewPath()), 
				jsonOfMark,
				sourceFormat.getIndentType(), 
				sourceFormat.getTabSize(), 
				sourceFormat.getLineWrapMode(), 
				context.getMode() == Mode.EDIT, 
				autosaveKey);
		response.render(OnDomReadyHeaderItem.forScript(script));
		
		script = String.format("gitplex.server.sourceEdit.onWindowLoad('%s', %s, '%s');", 
				sourceFormComponent.getMarkupId(), jsonOfMark, autosaveKey);
		response.render(OnLoadHeaderItem.forScript(script));
	}

	private String getJson(TextRange mark) {
		try {
			return GitPlex.getInstance(ObjectMapper.class).writeValueAsString(mark);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void mark(AjaxRequestTarget target, TextRange mark) {
		String script;
		if (mark != null) {
			script = String.format("gitplex.server.sourceEdit.mark('%s', %s);", 
					sourceFormComponent.getMarkupId(), getJson(mark));
		} else {
			script = String.format("gitplex.server.sourceEdit.mark('%s', undefined);", 
					sourceFormComponent.getMarkupId());
		}
		target.appendJavaScript(script);
	}

}
