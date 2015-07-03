package com.pmease.gitplex.web.component.savechange;

import java.io.IOException;

import javax.annotation.Nullable;

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
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.ObsoleteOldCommitException;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public abstract class SaveChangePanel extends Panel {

	private IModel<Repository> repoModel;
	
	private BlobIdent blobIdent;
	
	private ObjectId prevCommitId;
	
	private ObjectId currentCommitId;
	
	private String summaryCommitMessage;
	
	private String detailCommitMessage;
	
	private String defaultCommitMessage;
	
	private byte[] content;
	
	public SaveChangePanel(String id, IModel<Repository> repoModel, BlobIdent blobIdent, 
			ObjectId prevCommitId, @Nullable byte[] content) {
		super(id);
	
		this.repoModel = repoModel;
		this.blobIdent = blobIdent;
		this.prevCommitId = prevCommitId;
		this.content = content;
		
		if (content != null) {
			try (	FileRepository jgitRepo = repoModel.getObject().openAsJGitRepo();
					RevWalk revWalk = new RevWalk(jgitRepo)) {
				RevTree revTree = revWalk.parseCommit(prevCommitId).getTree();
				TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, blobIdent.path, revTree);
				if (treeWalk != null)
					defaultCommitMessage = "Change " + blobIdent.getName();
				else
					defaultCommitMessage = "Add " + blobIdent.getName();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			defaultCommitMessage = "Delete " + blobIdent.getName();
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final WebMarkupContainer hasChangesContainer = new WebMarkupContainer("hasChanges");
		hasChangesContainer.setVisibilityAllowed(false);
		hasChangesContainer.setOutputMarkupPlaceholderTag(true);
		hasChangesContainer.add(new AjaxLink<Void>("changes") {

			@Override
			public void onClick(AjaxRequestTarget target) {
			}
			
		});
		
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
				tag.put("placeholder", defaultCommitMessage);
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
				
				try (FileRepository jgitRepo = repoModel.getObject().openAsJGitRepo()) {
					String commitMessage = summaryCommitMessage;
					if (StringUtils.isBlank(commitMessage))
						commitMessage = defaultCommitMessage;
					if (StringUtils.isNotBlank(detailCommitMessage))
						commitMessage += "\n\n" + detailCommitMessage;
					User user = Preconditions.checkNotNull(GitPlex.getInstance(UserManager.class).getCurrent());
					String refName = blobIdent.revision;
					if (!refName.startsWith("refs/"))
						refName = Git.REFS_HEADS + refName;
							
					ObjectId newCommitId = null;
					while(newCommitId == null) {
						try {
							newCommitId = GitUtils.commitFile(jgitRepo, refName, prevCommitId, prevCommitId, 
									user.asPerson(), commitMessage, blobIdent.path, content);
						} catch (ObsoleteOldCommitException e) {
							currentCommitId = e.getOldCommitId();
							try (RevWalk revWalk = new RevWalk(jgitRepo)) {
								RevCommit prevCommit = revWalk.parseCommit(prevCommitId);
								RevCommit currentCommit = revWalk.parseCommit(currentCommitId);
								TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, blobIdent.path, 
										prevCommit.getTree().getId(), currentCommit.getTree().getId());
								if (treeWalk == null || treeWalk.getObjectId(0).equals(treeWalk.getObjectId(1))) {
									// the new commit introduced by other user does not affect our file, so 
									// pick up the new commit and try again
									prevCommitId = currentCommitId;
								} else {
									hasChangesContainer.setVisibilityAllowed(true);
									break;
								}
							} catch (IOException e2) {
								throw new RuntimeException(e2);
							}
						}
					}
					if (newCommitId != null) {
						repoModel.getObject().cacheObjectId(blobIdent.revision, newCommitId);
						onSaved(target);
					}
				}
			}
			
		});

		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(SaveChangePanel.class, "save-change.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(SaveChangePanel.class, "save-change.css")));
		response.render(OnDomReadyHeaderItem.forScript(String.format("gitplex.saveChange.init('%s');", getMarkupId())));
	}
	
	protected abstract void onSaved(AjaxRequestTarget target);

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
