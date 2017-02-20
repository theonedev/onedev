package com.gitplex.server.web.component.depotfile.fileedit;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.gitplex.server.GitPlex;
import com.gitplex.server.git.Blob;
import com.gitplex.server.git.BlobChange;
import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.git.PathAndContent;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.support.TextRange;
import com.gitplex.server.util.diff.WhitespaceOption;
import com.gitplex.server.web.behavior.AbstractPostAjaxBehavior;
import com.gitplex.server.web.component.ViewStateAwareAjaxLink;
import com.gitplex.server.web.component.depotfile.editsave.EditSavePanel;
import com.gitplex.server.web.component.diff.blob.BlobDiffPanel;
import com.gitplex.server.web.component.diff.revision.DiffViewMode;
import com.gitplex.server.web.component.sourceformat.OptionChangeCallback;
import com.gitplex.server.web.component.sourceformat.SourceFormatPanel;
import com.gitplex.server.web.page.depot.file.DepotFilePage;
import com.gitplex.server.web.util.ajaxlistener.ConfirmLeaveListener;

@SuppressWarnings("serial")
public abstract class FileEditPanel extends Panel {

	private static final String PREVIEW_ID = "preview";
	
	private final IModel<Depot> depotModel;
	
	private final String refName;

	private final String oldPath; 

	private String content;

	private String newPath;
	
	private final ObjectId prevCommitId;
	
	private final TextRange mark;
	
	private AbstractPostAjaxBehavior previewBehavior;
	
	private AbstractPostAjaxBehavior saveBehavior;
	
	private EditSavePanel editSavePanel;
	
	private SourceFormatPanel sourceFormat;
	
	public FileEditPanel(String id, IModel<Depot> depotModel, String refName, 
			@Nullable String oldPath, String content, ObjectId prevCommitId, 
			@Nullable TextRange mark) {
		super(id);
		this.depotModel = depotModel;
		this.refName = refName;
		this.oldPath = GitUtils.normalizePath(oldPath);
		this.content = content;
		this.prevCommitId = prevCommitId;
		this.mark = mark;
		
		newPath = this.oldPath;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		previewBehavior = new AbstractPostAjaxBehavior() {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setMethod(Method.POST);
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				content = params.getParameterValue("content").toString();

				BlobIdent oldBlobIdent = new BlobIdent();
				oldBlobIdent.revision = prevCommitId.name();
				oldBlobIdent.path = oldPath;
				oldBlobIdent.mode = FileMode.REGULAR_FILE.getBits();
				
				BlobIdent newBlobIdent = new BlobIdent();
				newBlobIdent.revision = ObjectId.zeroId().name();
				if (newPath != null)
					newBlobIdent.path = newPath;
				else
					newBlobIdent.path = "file.txt";
				newBlobIdent.mode = FileMode.REGULAR_FILE.getBits();
				
				DiffEntry.ChangeType changeType;
				if (oldPath == null)
					changeType = DiffEntry.ChangeType.ADD;
				else
					changeType = DiffEntry.ChangeType.MODIFY;
				
				BlobChange change = new BlobChange(changeType, oldBlobIdent, newBlobIdent, 
						WhitespaceOption.DEFAULT) {

					@Override
					public Blob getBlob(BlobIdent blobIdent) {
						if (blobIdent.revision.equals(ObjectId.zeroId().name()))
							return new Blob(blobIdent, ObjectId.zeroId(), content.getBytes(Charsets.UTF_8));
						else
							return depotModel.getObject().getBlob(blobIdent);
					}

				};
				BlobDiffPanel preview = new BlobDiffPanel("preview", depotModel, new Model<PullRequest>(null), 
						change, DiffViewMode.UNIFIED, null, null);
				replace(preview);
				target.add(preview);
				
				target.appendJavaScript(String.format("gitplex.server.fileedit.preview('%s');", getMarkupId()));
			}
			
		};
		add(new WebMarkupContainer("previewLink").add(previewBehavior));
		
		saveBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setMethod(Method.POST);
			}
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				content = params.getParameterValue("content").toString();
				target.appendJavaScript(String.format("gitplex.server.fileedit.save('%s');", getMarkupId()));
			}
			
		};
		add(new WebMarkupContainer("saveLink").add(saveBehavior));
		
		add(new ViewStateAwareAjaxLink<Void>("cancelLink") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			protected void onClick(AjaxRequestTarget target, String viewState) {
				RequestCycle.get().setMetaData(DepotFilePage.VIEW_STATE_KEY, viewState);
				FileEditPanel.this.onCancel(target);
			}
			
		});
		
		add(sourceFormat = new SourceFormatPanel("sourceFormat", new OptionChangeCallback() {

			@Override
			public void onOptioneChange(AjaxRequestTarget target) {
				String script = String.format("gitplex.server.fileedit.onIndentTypeChange('%s', '%s');", 
						FileEditPanel.this.getMarkupId(), sourceFormat.getIndentType());
				target.appendJavaScript(script);
			}
			
		}, new OptionChangeCallback() {

			@Override
			public void onOptioneChange(AjaxRequestTarget target) {
				String script = String.format("gitplex.server.fileedit.onTabSizeChange('%s', %s);", 
						FileEditPanel.this.getMarkupId(), sourceFormat.getTabSize());
				target.appendJavaScript(script);
			}
			
		}, new OptionChangeCallback() {
			
			@Override
			public void onOptioneChange(AjaxRequestTarget target) {
				String script = String.format("gitplex.server.fileedit.onLineWrapModeChange('%s', '%s');", 
						FileEditPanel.this.getMarkupId(), sourceFormat.getLineWrapMode());
				target.appendJavaScript(script);
			}
			
		}));
		
		add(new WebMarkupContainer(PREVIEW_ID).setOutputMarkupId(true));
		
		PathAndContent newFile = new PathAndContent() {

			@Override
			public String getPath() {
				return newPath;
			}

			@Override
			public byte[] getContent() {
				return content.getBytes(Charsets.UTF_8);
			}

		};
		add(editSavePanel = new EditSavePanel("save", depotModel, refName, oldPath, newFile, prevCommitId, null) {

			@Override
			protected void onCommitted(AjaxRequestTarget target, ObjectId oldCommit, ObjectId newCommit) {
				FileEditPanel.this.onCommitted(target, oldCommit, newCommit);
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new FileEditResourceReference()));
		
		String viewState = RequestCycle.get().getMetaData(DepotFilePage.VIEW_STATE_KEY);
		
		String script = String.format("gitplex.server.fileedit.init('%s', '%s', '%s', %s, %s, %s, %s, '%s', %s, '%s');", 
				getMarkupId(), getNewPathParam(), StringEscapeUtils.escapeEcmaScript(content), 
				previewBehavior.getCallbackFunction(CallbackParameter.explicit("content")), 
				saveBehavior.getCallbackFunction(CallbackParameter.explicit("content")), 
				mark!=null?getJson(mark):"undefined",
				viewState!=null?"JSON.parse('"+viewState+"')":"undefined", 
				sourceFormat.getIndentType(), 
				sourceFormat.getTabSize(), 
				sourceFormat.getLineWrapMode());
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
		depotModel.detach();
		
		super.onDetach();
	}
	
	public void onNewPathChange(AjaxRequestTarget target, String newPath) {
		this.newPath = GitUtils.normalizePath(newPath);
		
		editSavePanel.onNewPathChange(target);
		target.appendJavaScript(String.format("gitplex.server.fileedit.setMode('%s', '%s');", 
				getMarkupId(), getNewPathParam()));
	}
	
	private String getJson(TextRange mark) {
		try {
			return GitPlex.getInstance(ObjectMapper.class).writeValueAsString(mark);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void mark(AjaxRequestTarget target, TextRange mark) {
		String script = String.format("gitplex.server.fileedit.mark('%s', %s);", 
				getMarkupId(), getJson(mark));
		target.appendJavaScript(script);
	}

	protected abstract void onCommitted(AjaxRequestTarget target, ObjectId oldCommit, ObjectId newCommit);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
}
