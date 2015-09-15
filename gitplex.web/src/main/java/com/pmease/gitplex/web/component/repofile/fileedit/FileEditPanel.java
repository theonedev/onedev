package com.pmease.gitplex.web.component.repofile.fileedit;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Charsets;
import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobChange;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.LineProcessor;
import com.pmease.commons.git.PathAndContent;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.assets.closestdescendant.ClosestDescendantResourceReference;
import com.pmease.commons.wicket.assets.codemirror.CodeMirrorResourceReference;
import com.pmease.commons.wicket.assets.diffmatchpatch.DiffMatchPatchResourceReference;
import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.diff.blob.BlobDiffPanel;
import com.pmease.gitplex.web.component.diff.revision.DiffMode;
import com.pmease.gitplex.web.component.diff.revision.LineProcessOption;
import com.pmease.gitplex.web.component.repofile.editsave.EditSavePanel;
import com.pmease.gitplex.web.page.repository.file.Mark;

@SuppressWarnings("serial")
public abstract class FileEditPanel extends Panel {

	private static final String PREVIEW_ID = "preview";
	
	private final IModel<Repository> repoModel;
	
	private final String refName;

	private final String oldPath; 

	private String content;

	private String newPath;
	
	private final ObjectId prevCommitId;
	
	private final Mark mark;
	
	private final String clientState;
	
	private AbstractDefaultAjaxBehavior previewBehavior;
	
	private AbstractDefaultAjaxBehavior saveBehavior;
	
	private EditSavePanel editSavePanel;
	
	public FileEditPanel(String id, IModel<Repository> repoModel, String refName, 
			@Nullable String oldPath, String content, ObjectId prevCommitId, 
			@Nullable Mark mark, @Nullable String clientState) {
		super(id);
		this.repoModel = repoModel;
		this.refName = refName;
		this.oldPath = GitUtils.normalizePath(oldPath);
		this.content = content;
		this.prevCommitId = prevCommitId;
		this.mark = mark;
		this.clientState = clientState;
		
		newPath = this.oldPath;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		previewBehavior = new AbstractDefaultAjaxBehavior() {

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
				
				BlobChange change = new BlobChange(changeType, oldBlobIdent, newBlobIdent) {

					@Override
					public Blob getBlob(BlobIdent blobIdent) {
						if (blobIdent.revision.equals(GitUtils.NULL_SHA1))
							return new Blob(blobIdent, content.getBytes(Charsets.UTF_8));
						else
							return repoModel.getObject().getBlob(blobIdent);
					}

					@Override
					public LineProcessor getLineProcessor() {
						return LineProcessOption.IGNORE_NOTHING;
					}
					
				};
				BlobDiffPanel preview = new BlobDiffPanel("preview", repoModel, new Model<PullRequest>(null), 
						new Model<Comment>(null), change, DiffMode.UNIFIED);
				replace(preview);
				target.add(preview);
				
				target.appendJavaScript(String.format("gitplex.fileedit.preview('%s');", getMarkupId()));
			}
			
		};
		add(new WebMarkupContainer("previewLink").add(previewBehavior));
		
		saveBehavior = new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setMethod(Method.POST);
			}
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				content = params.getParameterValue("content").toString();
				target.appendJavaScript(String.format("gitplex.fileedit.save('%s');", getMarkupId()));
			}
			
		};
		add(new WebMarkupContainer("saveLink").add(saveBehavior));
		
		add(new AjaxLink<Void>("cancelLink") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				FileEditPanel.this.onCancel(target);
			}
			
		});
		
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
		add(editSavePanel = new EditSavePanel("save", repoModel, refName, oldPath, newFile, prevCommitId, null) {

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

		response.render(JavaScriptHeaderItem.forReference(CodeMirrorResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(ClosestDescendantResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(DiffMatchPatchResourceReference.INSTANCE));
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(FileEditPanel.class, "file-edit.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(FileEditPanel.class, "file-edit.css")));
		
		String script = String.format("gitplex.fileedit.init('%s', '%s', '%s', %s, %s, %s, %s);", 
				getMarkupId(), getNewPathParam(), StringEscapeUtils.escapeEcmaScript(content), 
				previewBehavior.getCallbackFunction(CallbackParameter.explicit("content")), 
				saveBehavior.getCallbackFunction(CallbackParameter.explicit("content")), 
				mark!=null?mark.toJSON():"undefined",
				clientState!=null?"'"+clientState+"'":"undefined");
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
		this.newPath = GitUtils.normalizePath(newPath);
		
		editSavePanel.onNewPathChange(target);
		target.appendJavaScript(String.format("gitplex.fileedit.setMode('%s', '%s');", 
				getMarkupId(), getNewPathParam()));
	}
	
	public void mark(AjaxRequestTarget target, Mark mark) {
		String script = String.format("gitplex.fileedit.mark('%s', %s);", 
				getMarkupId(), mark.toJSON());
		target.appendJavaScript(script);
	}

	protected abstract void onCommitted(AjaxRequestTarget target, ObjectId newCommitId);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
