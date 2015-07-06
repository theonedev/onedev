package com.pmease.gitplex.web.component.editsave;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.FileEdit;
import com.pmease.commons.git.PathAndContent;
import com.pmease.commons.git.exception.NotTreeException;
import com.pmease.commons.git.exception.ObjectAlreadyExistException;
import com.pmease.commons.git.exception.ObsoleteCommitException;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public abstract class EditSavePanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String refName;
	
	private final String oldPath;
	
	private final PathAndContent newFile;
	
	private final CancelListener cancelListener;
	
	private ObjectId prevCommitId;
	
	private ObjectId currentCommitId;
	
	private String summaryCommitMessage;
	
	private String detailCommitMessage;
	
	public EditSavePanel(String id, IModel<Repository> repoModel, String refName, 
			@Nullable String oldPath, @Nullable PathAndContent newFile, 
			ObjectId prevCommitId, @Nullable CancelListener cancelListener) {
		super(id);
	
		this.repoModel = repoModel;
		this.refName = refName;
		this.oldPath = oldPath;
		this.newFile = newFile;
		this.cancelListener = cancelListener;
		this.prevCommitId = prevCommitId;
	}

	private String getDefaultCommitMessage() {
		String oldName;
		if (oldPath != null && oldPath.contains("/"))
			oldName = StringUtils.substringAfterLast(oldPath, "/");
		else
			oldName = oldPath;
		
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
				return "Edit " + oldPath;
			} else {
				return "Rename " + oldPath;
			}
		}
			
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final FeedbackPanel feedback = new FeedbackPanel("feedback", this);
		feedback.setOutputMarkupPlaceholderTag(true);
		add(feedback);
				
		final WebMarkupContainer hasChangesContainer = new WebMarkupContainer("hasChanges");
		hasChangesContainer.setVisibilityAllowed(false);
		hasChangesContainer.setOutputMarkupPlaceholderTag(true);
		hasChangesContainer.add(new AjaxLink<Void>("changes") {

			@Override
			public void onClick(AjaxRequestTarget target) {
			}
			
		});
		add(hasChangesContainer);
		
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
		
		form.add(new AjaxSubmitLink("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				if (newFile != null && StringUtils.isBlank(newFile.getPath())) {
					EditSavePanel.this.error("Please specify file name.");
					target.add(feedback);
					return;
				}
				
				try (FileRepository jgitRepo = repoModel.getObject().openAsJGitRepo()) {
					String commitMessage = summaryCommitMessage;
					if (StringUtils.isBlank(commitMessage))
						commitMessage = getDefaultCommitMessage();
					if (StringUtils.isNotBlank(detailCommitMessage))
						commitMessage += "\n\n" + detailCommitMessage;
					User user = Preconditions.checkNotNull(GitPlex.getInstance(UserManager.class).getCurrent());

					FileEdit edit = new FileEdit(oldPath, newFile);
					ObjectId newCommitId = null;
					while(newCommitId == null) {
						try {
							newCommitId = edit.commit(jgitRepo, refName, 
									prevCommitId, prevCommitId, user.asPerson(), commitMessage);
						} catch (ObsoleteCommitException e) {
							currentCommitId = e.getOldCommitId();
							try (RevWalk revWalk = new RevWalk(jgitRepo)) {
								RevCommit prevCommit = revWalk.parseCommit(prevCommitId);
								RevCommit currentCommit = revWalk.parseCommit(currentCommitId);

								prevCommitId = currentCommitId;

								hasChangesContainer.setVisibilityAllowed(false);
								if (edit.getOldPath() != null) {
									TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, edit.getOldPath(), 
											prevCommit.getTree().getId(), currentCommit.getTree().getId());
									if (treeWalk != null) {
										if (!treeWalk.getObjectId(0).equals(treeWalk.getObjectId(1)) 
												|| !treeWalk.getFileMode(0).equals(treeWalk.getFileMode(1))) {
											if (treeWalk.getObjectId(1).equals(ObjectId.zeroId())) {
												if (edit.getNewFile() != null) {
													edit = new FileEdit(null, edit.getNewFile());
													hasChangesContainer.setVisibilityAllowed(true);
													target.add(hasChangesContainer);
													break;
												} else {
													newCommitId = currentCommitId;
													break;
												}
											} else {
												hasChangesContainer.setVisibilityAllowed(true);
												target.add(hasChangesContainer);
												break;
											}
										}
									}
								}
								if (edit.getNewFile() != null && !edit.getNewFile().getPath().equals(edit.getOldPath())) { 
									TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, edit.getNewFile().getPath(), 
											prevCommit.getTree().getId(), currentCommit.getTree().getId());
									if (treeWalk != null) {
										if (!treeWalk.getObjectId(0).equals(treeWalk.getObjectId(1)) 
												|| !treeWalk.getFileMode(0).equals(treeWalk.getFileMode(1))) {
											hasChangesContainer.setVisibilityAllowed(true);
											target.add(hasChangesContainer);
											break;
										}
									}
								} 

							} catch (IOException e2) {
								throw new RuntimeException(e2);
							}
						} catch (ObjectAlreadyExistException e) {
							EditSavePanel.this.error("A file with same name already exists. "
									+ "Please choose a different name and try again.");
							target.add(feedback);
						} catch (NotTreeException e) {
							EditSavePanel.this.error("A file exists where you’re trying to create a subdirectory. "
									+ "Choose a new path and try again..");
							target.add(feedback);
						}
					}
					if (newCommitId != null)
						onCommitted(target, newCommitId);
				}
			}
			
		});
		
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

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(EditSavePanel.class, "edit-save.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(EditSavePanel.class, "edit-save.css")));
		response.render(OnDomReadyHeaderItem.forScript(String.format("gitplex.editSave.init('%s');", getMarkupId())));
	}
	
	protected abstract void onCommitted(AjaxRequestTarget target, ObjectId newCommitId);
	
	public void onNewPathChange(AjaxRequestTarget target) {
		target.appendJavaScript(String.format("gitplex.editSave.updateDefaultCommitMessage('%s', '%s');", 
				getMarkupId(), StringEscapeUtils.escapeEcmaScript(getDefaultCommitMessage())));
	}
	
	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
