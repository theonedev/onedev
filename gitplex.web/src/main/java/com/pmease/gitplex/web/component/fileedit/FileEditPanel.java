package com.pmease.gitplex.web.component.fileedit;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Charsets;
import com.pmease.commons.git.PathAndContent;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.editsave.EditSavePanel;

@SuppressWarnings("serial")
public abstract class FileEditPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String refName;

	private final String oldPath; 

	private String content;

	private String newPath;
	
	private final ObjectId prevCommitId;
	
	private AbstractDefaultAjaxBehavior previewBehavior;
	
	private AbstractDefaultAjaxBehavior saveBehavior;
	
	public FileEditPanel(String id, IModel<Repository> repoModel, String refName, 
			@Nullable String oldPath, String content, ObjectId prevCommitId) {
		super(id);
		this.repoModel = repoModel;
		this.refName = refName;
		this.oldPath = oldPath;
		this.content = content;
		this.prevCommitId = prevCommitId;
		
		newPath = oldPath;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		previewBehavior = new AbstractDefaultAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				content = params.getParameterValue("content").toString();
				target.appendJavaScript(String.format("gitplex.fileEdit.preview('%s');", getMarkupId()));
			}
			
		};
		add(new WebMarkupContainer("previewLink").add(previewBehavior));
		
		saveBehavior = new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				content = params.getParameterValue("content").toString();
				target.appendJavaScript(String.format("gitplex.fileEdit.save('%s');", getMarkupId()));
			}
			
		};
		add(new WebMarkupContainer("saveLink").add(saveBehavior));
		
		add(new AjaxLink<Void>("cancelLink") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				FileEditPanel.this.onCancel(target);
			}
			
		});
		
		add(new WebMarkupContainer("preview"));
		
		IModel<PathAndContent> newFileModel = new AbstractReadOnlyModel<PathAndContent>() {

			@Override
			public PathAndContent getObject() {
				return new PathAndContent(newPath, content.getBytes(Charsets.UTF_8));
			}
			
		};
		add(new EditSavePanel("save", repoModel, refName, oldPath, newFileModel, prevCommitId, null) {

			@Override
			protected void onCommitted(AjaxRequestTarget target, ObjectId newCommitId) {
				FileEditPanel.this.onCommitted(target, newCommitId);
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(FileEditPanel.class, "file-edit.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(FileEditPanel.class, "file-edit.css")));
		
		String script = String.format("gitplex.fileEdit.init('%s', '%s', '%s', %s, %s);", 
				getMarkupId(), getNewPathParam(), StringEscapeUtils.escapeEcmaScript(content), 
				previewBehavior.getCallbackFunction(CallbackParameter.explicit("content")), 
				saveBehavior.getCallbackFunction(CallbackParameter.explicit("content")));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	private String getNewPathParam() {
		if (newPath != null)
			return StringEscapeUtils.escapeEcmaScript(newPath);
		else
			return "unknown.txt";
	}
	
	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}
	
	public void onNewPathChange(AjaxRequestTarget target, String newPath) {
		this.newPath = newPath;
		
		target.appendJavaScript(String.format("gitplex.fileEdit.setMode('%s', '%s');", 
				getMarkupId(), getNewPathParam()));
	}

	protected abstract void onCommitted(AjaxRequestTarget target, ObjectId newCommitId);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
