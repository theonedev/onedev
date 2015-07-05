package com.pmease.gitplex.web.component.fileedit;

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

import com.pmease.commons.git.ParentPathAndName;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.editsave.EditSavePanel;

@SuppressWarnings("serial")
public abstract class FileEditPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String refName;

	private final ParentPathAndName parentPathAndName; 
	
	private final ObjectId prevCommitId;
	
	private String content;
	
	private AbstractDefaultAjaxBehavior previewBehavior;
	
	private AbstractDefaultAjaxBehavior saveBehavior;
	
	public FileEditPanel(String id, IModel<Repository> repoModel, String refName, 
			ParentPathAndName parentPathAndName, String content, ObjectId prevCommitId) {
		super(id);
		this.repoModel = repoModel;
		this.refName = refName;
		this.parentPathAndName = parentPathAndName;
		this.content = content;
		this.prevCommitId = prevCommitId;
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
		
		IModel<String> contentModel = new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return content;
			}
			
		};
		add(new EditSavePanel("save", repoModel, refName, parentPathAndName, contentModel, prevCommitId, null) {

			@Override
			protected void onCommitted(AjaxRequestTarget target, ObjectId newCommitId) {
				FileEditPanel.this.onCommitted(target, newCommitId, getNewName());
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
		
		String script = String.format("gitplex.fileEdit.init('%s', '%s', %s, %s);", 
				getMarkupId(), StringEscapeUtils.escapeEcmaScript(content), 
				previewBehavior.getCallbackFunction(CallbackParameter.explicit("content")), 
				saveBehavior.getCallbackFunction(CallbackParameter.explicit("content")));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected void onDetach() {
		repoModel.detach();

		super.onDetach();
	}

	protected abstract void onCommitted(AjaxRequestTarget target, ObjectId newCommitId, String newName);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
