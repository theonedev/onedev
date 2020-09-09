package io.onedev.server.web.page.project.blob.render.renderers.source;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.unbescape.javascript.JavaScriptEscape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.OneDev;
import io.onedev.server.web.component.sourceformat.OptionChangeCallback;
import io.onedev.server.web.component.sourceformat.SourceFormatPanel;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.edit.BlobEditPanel;
import io.onedev.server.web.page.project.blob.render.view.Positionable;

@SuppressWarnings("serial")
public class SourceEditPanel extends BlobEditPanel implements Positionable {

	private SourceFormatPanel sourceFormat;
	
	public SourceEditPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected FormComponentPanel<byte[]> newEditor(String componentId, byte[] initialContent) {
		return new SourceFormComponent(componentId, initialContent) {

			@Override
			protected BlobRenderContext getContext() {
				return context;
			}

			@Override
			protected SourceFormatPanel getSourceFormat() {
				return sourceFormat;
			}
		};
	}

	@Override
	protected WebMarkupContainer newEditOptions(String componentId) {
		sourceFormat = new SourceFormatPanel(componentId, new OptionChangeCallback() {

			@Override
			public void onOptioneChange(AjaxRequestTarget target) {
				String script = String.format("onedev.server.sourceEdit.onIndentTypeChange('%s');", 
						sourceFormat.getIndentType());
				target.appendJavaScript(script);
			}
			
		}, new OptionChangeCallback() {

			@Override
			public void onOptioneChange(AjaxRequestTarget target) {
				String script = String.format("onedev.server.sourceEdit.onTabSizeChange(%s);", 
						sourceFormat.getTabSize());
				target.appendJavaScript(script);
			}
			
		}, new OptionChangeCallback() {
			
			@Override
			public void onOptioneChange(AjaxRequestTarget target) {
				String script = String.format("onedev.server.sourceEdit.onLineWrapModeChange('%s');", 
						sourceFormat.getLineWrapMode());
				target.appendJavaScript(script);
			}
			
		});	
		return sourceFormat;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new SourceEditResourceReference()));
		
		String autosaveKey = JavaScriptEscape.escapeJavaScript(context.getAutosaveKey());
		PlanarRange mark = SourceRendererProvider.getRange(context.getPosition());
		
		String script = String.format("onedev.server.sourceEdit.onWindowLoad('%s', %s, '%s');", 
				getEditor().getMarkupId(), mark != null? getJson(mark): "undefined", autosaveKey);
		response.render(OnLoadHeaderItem.forScript(script));		
	}
	
	private String getJson(PlanarRange mark) {
		try {
			return OneDev.getInstance(ObjectMapper.class).writeValueAsString(mark);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void position(AjaxRequestTarget target, String position) {
		String script;
		PlanarRange mark = SourceRendererProvider.getRange(position);
		if (mark != null) 
			script = String.format("onedev.server.sourceEdit.mark(%s);", getJson(mark));
		else 
			script = String.format("onedev.server.sourceEdit.mark();");
		target.appendJavaScript(script);
	}

}
