package com.gitplex.server.web.component.depotfile.editsave;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Preconditions;
import com.gitplex.commons.git.Blob;
import com.gitplex.commons.git.BlobChange;
import com.gitplex.commons.git.BlobIdent;
import com.gitplex.commons.git.FileEdit;
import com.gitplex.commons.git.PathAndContent;
import com.gitplex.commons.git.exception.GitObjectAlreadyExistsException;
import com.gitplex.commons.git.exception.NotGitTreeException;
import com.gitplex.commons.git.exception.ObsoleteCommitException;
import com.gitplex.commons.lang.diff.WhitespaceOption;
import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.manager.AccountManager;
import com.gitplex.server.web.component.diff.blob.BlobDiffPanel;
import com.gitplex.server.web.component.diff.revision.DiffViewMode;
import com.gitplex.server.web.page.depot.file.DepotFilePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import jersey.repackaged.com.google.common.base.Objects;

@SuppressWarnings("serial")
public abstract class EditSavePanel extends Panel {

	private final IModel<Depot> depotModel;
	
	private final String refName;
	
	private FileEdit fileEdit;
	
	private final CancelListener cancelListener;
	
	private ObjectId prevCommitId;
	
	private ObjectId currentCommitId;
	
	private String summaryCommitMessage;
	
	private String detailCommitMessage;
	
	private BlobChange change;
	
	private boolean saveInProgress;
	
	public EditSavePanel(String id, IModel<Depot> depotModel, String refName, 
			@Nullable String oldPath, @Nullable PathAndContent newFile, 
			ObjectId prevCommitId, @Nullable CancelListener cancelListener) {
		super(id);
	
		this.depotModel = depotModel;
		this.refName = refName;
		this.fileEdit = new FileEdit(oldPath, newFile);
		this.cancelListener = cancelListener;
		this.prevCommitId = prevCommitId;
	}

	private String getDefaultCommitMessage() {
		String oldPath = fileEdit.getOldPath();
		String oldName;
		if (oldPath != null && oldPath.contains("/"))
			oldName = StringUtils.substringAfterLast(oldPath, "/");
		else
			oldName = oldPath;
		
		PathAndContent newFile = fileEdit.getNewFile();
		if (newFile == null) { 
			return "Delete " + oldName;
		} else {
			String newPath = newFile.getPath();

			String newName;
			if (newPath != null && newPath.contains("/"))
				newName = StringUtils.substringAfterLast(newPath, "/");
			else
				newName = newPath;
			
			if (oldPath == null) {
				if (newName != null)
					return "Add " + newName;
				else
					return "Add new file";
			} else if (oldPath.equals(newPath)) {
				return "Edit " + oldName;
			} else {
				return "Rename " + oldName;
			}
		}
			
	}
	
	private void newChangedContainer(@Nullable AjaxRequestTarget target) {
		WebMarkupContainer changedContainer = new WebMarkupContainer("changed");
		changedContainer.setVisible(change != null);
		changedContainer.setOutputMarkupPlaceholderTag(true);
		if (change != null) {
			changedContainer.add(new BlobDiffPanel("changes", depotModel, 
					new Model<PullRequest>(null), change, DiffViewMode.UNIFIED, null, null));
		} else {
			changedContainer.add(new WebMarkupContainer("changes"));
		}
		if (target != null) {
			replace(changedContainer);
			target.add(changedContainer);
			if (change != null) {
				String script = String.format("$('#%s .edit-save input[type=submit]').val('Commit and overwrite');", 
						getMarkupId());
				target.appendJavaScript(script);
			}
		} else {
			add(changedContainer);		
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final NotificationPanel feedback = new NotificationPanel("feedback", this);
		feedback.setOutputMarkupPlaceholderTag(true);
		add(feedback);
				
		newChangedContainer(null);
		
		Form<?> form = new Form<Void>("form");
		add(form);

		form.add(new TextField<String>("summaryCommitMessage", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return summaryCommitMessage;
			}

			@Override
			public void setObject(String object) {
				summaryCommitMessage = object;
			}
			
		}) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("placeholder", getDefaultCommitMessage());
			}
			
		});
		
		form.add(new TextArea<String>("detailCommitMessage", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return detailCommitMessage;
			}

			@Override
			public void setObject(String object) {
				detailCommitMessage = object;
			}
			
		}));
		
		AjaxButton saveButton = new AjaxButton("save") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setMethod(Method.POST);
				
				String script = String.format(""
						+ "var viewState = $('#%s').data('viewState');"
						+ "return viewState?JSON.stringify(viewState):'';", EditSavePanel.this.getMarkupId());
				attributes.getDynamicExtraParameters().add("return {view_state: function() {" + script + "}}");
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				if (saveInProgress || (fileEdit.getOldPath() != null && fileEdit.getNewFile() != null))
					tag.put("disabled", "disabled");
				if (saveInProgress)
					tag.put("value", "Please wait...");
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				save(target, feedback);
				saveInProgress = true;
				target.add(this);
				target.appendJavaScript("gitplex.commons.form.markClean($('form'));");
			}
			
		};
		saveButton.setOutputMarkupId(true);
		form.add(saveButton);
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(cancelListener != null);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				cancelListener.onCancel(target);
			}
			
		});

		setOutputMarkupId(true);
	}
	
	private void save(AjaxRequestTarget target, FeedbackPanel feedback) {
		String viewState = RequestCycle.get().getRequest().getPostParameters()
				.getParameterValue("view_state").toString();
		RequestCycle.get().setMetaData(DepotFilePage.VIEW_STATE_KEY, viewState);

		change = null;
		
		PathAndContent newFile = fileEdit.getNewFile();
		if (newFile != null && StringUtils.isBlank(newFile.getPath())) {
			EditSavePanel.this.error("Please specify file name.");
			target.add(feedback);
		} else {
			String commitMessage = summaryCommitMessage;
			if (StringUtils.isBlank(commitMessage))
				commitMessage = getDefaultCommitMessage();
			if (StringUtils.isNotBlank(detailCommitMessage))
				commitMessage += "\n\n" + detailCommitMessage;
			Account user = Preconditions.checkNotNull(GitPlex.getInstance(AccountManager.class).getCurrent());

			Repository repository = depotModel.getObject().getRepository();
			ObjectId newCommitId = null;
			while(newCommitId == null) {
				try {
					newCommitId = fileEdit.commit(repository, refName, 
							prevCommitId, prevCommitId, user.asPerson(), commitMessage);
				} catch (ObsoleteCommitException e) {
					currentCommitId = e.getOldCommitId();
					try (RevWalk revWalk = new RevWalk(repository)) {
						RevCommit prevCommit = revWalk.parseCommit(prevCommitId);
						RevCommit currentCommit = revWalk.parseCommit(currentCommitId);
						prevCommitId = currentCommitId;

						String oldPath = fileEdit.getOldPath();
						if (oldPath != null) {
							TreeWalk treeWalk = TreeWalk.forPath(repository, oldPath, 
									prevCommit.getTree().getId(), currentCommit.getTree().getId());
							if (treeWalk != null) {
								if (!treeWalk.getObjectId(0).equals(treeWalk.getObjectId(1)) 
										|| !treeWalk.getFileMode(0).equals(treeWalk.getFileMode(1))) {
									// mark changed if original file exists and content or mode has been modified
									// by others
									if (treeWalk.getObjectId(1).equals(ObjectId.zeroId())) {
										if (newFile != null) {
											fileEdit = new FileEdit(null, newFile);
											change = getChange(treeWalk, prevCommit, currentCommit);
											break;
										} else {
											newCommitId = currentCommitId;
											break;
										}
									} else {
										change = getChange(treeWalk, prevCommit, currentCommit);
										break;
									}
								}
							}
						}
						if (newFile != null && !newFile.getPath().equals(oldPath)) { 
							TreeWalk treeWalk = TreeWalk.forPath(repository, newFile.getPath(), 
									prevCommit.getTree().getId(), currentCommit.getTree().getId());
							if (treeWalk != null) {
								if (!treeWalk.getObjectId(0).equals(treeWalk.getObjectId(1)) 
										|| !treeWalk.getFileMode(0).equals(treeWalk.getFileMode(1))) {
									// if added/renamed file exists and content or mode has been modified 
									// by others
									change = getChange(treeWalk, prevCommit, currentCommit);
									break;
								}
							}
						} 
					} catch (IOException e2) {
						throw new RuntimeException(e2);
					}
				} catch (GitObjectAlreadyExistsException e) {
					EditSavePanel.this.error("A file with same name already exists. "
							+ "Please choose a different name and try again.");
					target.add(feedback);
					break;
				} catch (NotGitTreeException e) {
					EditSavePanel.this.error("A file exists where you’re trying to create a subdirectory. "
							+ "Choose a new path and try again..");
					target.add(feedback);
					break;
				}
			}
			if (newCommitId != null)
				onCommitted(target, prevCommitId, newCommitId);
			else
				newChangedContainer(target);
		}
	}
	
	private BlobChange getChange(TreeWalk treeWalk, RevCommit oldCommit, RevCommit newCommit) {
		DiffEntry.ChangeType changeType = DiffEntry.ChangeType.MODIFY;
		BlobIdent oldBlobIdent = new BlobIdent();
		oldBlobIdent.revision = oldCommit.name();
		if (!treeWalk.getObjectId(0).equals(ObjectId.zeroId())) {
			oldBlobIdent.path = treeWalk.getPathString();
			oldBlobIdent.mode = treeWalk.getRawMode(0);
		} else {
			changeType = DiffEntry.ChangeType.ADD;
		}
		
		BlobIdent newBlobIdent = new BlobIdent();
		newBlobIdent.revision = newCommit.name();
		if (!treeWalk.getObjectId(1).equals(ObjectId.zeroId())) {
			newBlobIdent.path = treeWalk.getPathString();
			newBlobIdent.mode = treeWalk.getRawMode(1);
		} else {
			changeType = DiffEntry.ChangeType.DELETE;
		}
		
		return new BlobChange(changeType, oldBlobIdent, newBlobIdent, WhitespaceOption.DEFAULT) {

			@Override
			public Blob getBlob(BlobIdent blobIdent) {
				return depotModel.getObject().getBlob(blobIdent);
			}

		};
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new EditSaveResourceReference()));
		
		String script = String.format("gitplex.server.editsave.init('%s');", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	protected abstract void onCommitted(AjaxRequestTarget target, ObjectId oldCommitId, ObjectId newCommitId);
	
	public void onNewPathChange(AjaxRequestTarget target) {
		String script = String.format("gitplex.server.editsave.onPathChange('%s', '%s', %b);", 
				getMarkupId(), StringEscapeUtils.escapeEcmaScript(getDefaultCommitMessage()), 
				Objects.equal(fileEdit.getNewFile().getPath(), fileEdit.getOldPath()));
		target.appendJavaScript(script);
	}
	
	@Override
	protected void onDetach() {
		depotModel.detach();
		
		super.onDetach();
	}

}
