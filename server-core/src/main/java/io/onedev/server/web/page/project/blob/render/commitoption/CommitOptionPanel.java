package io.onedev.server.web.page.project.blob.render.commitoption;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.unbescape.javascript.JavaScriptEscape;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;
import io.onedev.server.git.BlobChange;
import io.onedev.server.git.BlobContent;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.exception.NotTreeException;
import io.onedev.server.git.exception.ObjectAlreadyExistsException;
import io.onedev.server.git.exception.ObsoleteCommitException;
import io.onedev.server.git.service.GitService;
import io.onedev.server.git.service.PathChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Provider;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.ajaxlistener.TrackViewStateListener;
import io.onedev.server.web.component.diff.blob.BlobDiffPanel;
import io.onedev.server.web.component.diff.revision.DiffViewMode;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.blob.RevisionResolved;
import io.onedev.server.web.page.project.blob.navigator.BlobNameChanging;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.util.CommitMessageBean;

@SuppressWarnings("serial")
public class CommitOptionPanel extends Panel {

	private final BlobRenderContext context;
	
	private final Provider<byte[]> newContentProvider;
	
	private CommitMessageBean commitMessageBean = new CommitMessageBean();
	
	private BlobChange changesOfOthers;
	
	private boolean contentModified;
	
	private Set<String> oldPaths;
	
	private Form<?> form;
	
	public CommitOptionPanel(String id, BlobRenderContext context, @Nullable Provider<byte[]> newContentProvider) {
		super(id);

		this.context = context;
		this.newContentProvider = newContentProvider;

		oldPaths = new HashSet<>();
		String oldPath = getOldPath();
		if (oldPath != null)
			oldPaths.add(oldPath);
	}

	@Nullable
	private String getOldPath() {
		return context.getMode()!=Mode.ADD? context.getBlobIdent().path: null;
	}
	
	private String getDefaultCommitMessage() {
		String oldPath = getOldPath();
		String oldName;
		if (oldPath != null && oldPath.contains("/"))
			oldName = StringUtils.substringAfterLast(oldPath, "/");
		else
			oldName = oldPath;
		
		if (newContentProvider == null) { 
			return "Delete " + oldName;
		} else {
			String newPath = context.getNewPath();

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
	
	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}
	
	private void newChangesOfOthersContainer(@Nullable AjaxRequestTarget target) {
		Component changesOfOthersContainer;
		if (changesOfOthers != null) 
			changesOfOthersContainer = new BlobDiffPanel("changesOfOthers", changesOfOthers, DiffViewMode.UNIFIED, null);
		else 
			changesOfOthersContainer = new WebMarkupContainer("changesOfOthers").setVisible(false);
		if (target != null) {
			form.replace(changesOfOthersContainer);
			target.add(form);
			if (changesOfOthers != null) {
				String script = String.format("$('#%s .commit-option input[type=submit]').val('Commit and overwrite');", 
						getMarkupId());
				target.appendJavaScript(script);
			}
		} else {
			form.add(changesOfOthersContainer);		
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		add(form);

		form.add(new FencedFeedbackPanel("feedback", form));
		newChangesOfOthersContainer(null);
		commitMessageBean.setCommitMessage(getDefaultCommitMessage());
		form.add(BeanContext.edit("commitMessage", commitMessageBean));

		AjaxButton saveButton = new AjaxButton("save") {

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setMethod(Method.POST);

				attributes.getAjaxCallListeners().add(new TrackViewStateListener(true));
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				if (!isBlobModified())
					tag.put("disabled", "disabled");
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				if (save(target)) {
					String script = String.format(""
							+ "$('#%s').attr('disabled', 'disabled').val('Please wait...');"
							+ "onedev.server.form.markClean($('form'));", getMarkupId());
					target.appendJavaScript(script);
				}
			}
			
		};
		saveButton.setOutputMarkupId(true);
		form.add(saveButton);
		
		form.add(new ViewStateAwareAjaxLink<Void>("cancel", true) {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(newContentProvider == null);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				context.onModeChange(target, Mode.VIEW, null);
			}
			
		});

		setOutputMarkupId(true);
	}
	
	private boolean isBlobModified() {
		return !context.getBlobIdent().isFile() 
				|| context.getMode() == Mode.DELETE
				|| contentModified 
				|| !Objects.equal(context.getBlobIdent().path, context.getNewPath());
	}
	
	private boolean save(AjaxRequestTarget target) {
		changesOfOthers = null;
		
		if (newContentProvider != null && StringUtils.isBlank(context.getNewPath())) {
			form.error("Please specify file name.");
			target.add(form);
			return false;
		} else {
			String commitMessage = commitMessageBean.getCommitMessage();
			User user = Preconditions.checkNotNull(SecurityUtils.getUser());

			String revision = context.getBlobIdent().revision;
			ObjectId prevCommitId;
			if (revision != null)
				prevCommitId = context.getProject().getObjectId(revision, true);
			else
				prevCommitId = ObjectId.zeroId();

			if (revision == null)
				revision = "main";
			
			String refName = GitUtils.branch2ref(revision);
			
			ObjectId newCommitId = null;
			while(newCommitId == null) {
				try {
					Map<String, BlobContent> newBlobs = new HashMap<>();
					if (newContentProvider != null) {
						String newPath = context.getNewPath();
						if (context.getProject().isReviewRequiredForModification(user, revision, newPath)) {
							form.error("Review required for this change. Please submit pull request instead");
							target.add(form);
							return false;
						} else if (context.getProject().isBuildRequiredForModification(user, revision, newPath)) {
							form.error("Build required for this change. Please submit pull request instead");
							target.add(form);
							return false;
						} else if (context.getProject().isCommitSignatureRequiredButNoSigningKey(user, revision)) {
							form.error("Signature required for this change, but no signing key is specified");
							target.add(form);
							return false;
						}
						
						int mode;
						if (context.getBlobIdent().isFile())
							mode = context.getBlobIdent().mode;
						else
							mode = FileMode.REGULAR_FILE.getBits();
						newBlobs.put(context.getNewPath(), new BlobContent(newContentProvider.get(), mode));
					}

					newCommitId = getGitService().commit(context.getProject(), 
							new BlobEdits(oldPaths, newBlobs), refName, prevCommitId, prevCommitId, 
							user.asPerson(), commitMessage, false);
				} catch (Exception e) {
					ObjectAlreadyExistsException objectAlreadyExistsException = 
							ExceptionUtils.find(e, ObjectAlreadyExistsException.class);
					NotTreeException notTreeException = ExceptionUtils.find(e, NotTreeException.class);
					ObsoleteCommitException obsoleteCommitException = 
							ExceptionUtils.find(e, ObsoleteCommitException.class);
					
					if (objectAlreadyExistsException != null) {
						form.error("A path with same name already exists. "
								+ "Please choose a different name and try again.");
						target.add(form);
						break;
					} else if (notTreeException != null) {
						form.error("A file exists where you’re trying to create a subdirectory. "
								+ "Choose a new path and try again..");
						target.add(form);
						break;
					} else if (obsoleteCommitException != null) {
						send(this, Broadcast.BUBBLE, new RevisionResolved(target, obsoleteCommitException.getOldCommitId()));
						ObjectId lastPrevCommitId = prevCommitId;
						prevCommitId = obsoleteCommitException.getOldCommitId();
						if (!oldPaths.isEmpty()) {
							String path = oldPaths.iterator().next();
							PathChange pathChange = getGitService().getPathChange(context.getProject(), 
									lastPrevCommitId, prevCommitId, path);
							Preconditions.checkNotNull(pathChange);
							if (!pathChange.getOldObjectId().equals(pathChange.getNewObjectId()) 
									|| pathChange.getOldMode() != pathChange.getNewMode()) {
								// mark changed if original file exists and content or mode has been modified
								// by others
								if (pathChange.getNewObjectId().equals(ObjectId.zeroId())) {
									if (newContentProvider != null) {
										oldPaths.clear();
										changesOfOthers = getBlobChange(path, pathChange, lastPrevCommitId, prevCommitId);
										form.warn("Someone made below change since you started editing");
										break;
									} else {
										newCommitId = obsoleteCommitException.getOldCommitId();
									}
								} else {
									changesOfOthers = getBlobChange(path, pathChange, lastPrevCommitId, prevCommitId);
									form.warn("Someone made below change since you started editing");
									break;
								}
							} 
						}
					} else {
						throw ExceptionUtils.unchecked(e);
					}
				}
			}
			if (newCommitId != null) {
				context.onCommitted(target, newCommitId);
				target.appendJavaScript("$(window).resize();");
				return true;
			} else {
				newChangesOfOthersContainer(target);
				return false;
			}
		}
	}
	
	private BlobChange getBlobChange(String path, PathChange pathChange, 
			ObjectId oldCommitId, ObjectId newCommitId) {
		DiffEntry.ChangeType changeType = DiffEntry.ChangeType.MODIFY;
		BlobIdent oldBlobIdent;
		if (!pathChange.getOldObjectId().equals(ObjectId.zeroId())) {
			oldBlobIdent = new BlobIdent(oldCommitId.name(), path, pathChange.getOldMode());
		} else {
			oldBlobIdent = new BlobIdent(oldCommitId.name(), null, FileMode.TREE.getBits());
			changeType = DiffEntry.ChangeType.ADD;
		}
		
		BlobIdent newBlobIdent;
		if (!pathChange.getNewObjectId().equals(ObjectId.zeroId())) {
			newBlobIdent = new BlobIdent(newCommitId.name(), path, pathChange.getNewMode());
		} else {
			newBlobIdent = new BlobIdent(newCommitId.name(), null, FileMode.TREE.getBits());
			changeType = DiffEntry.ChangeType.DELETE;
		}
		
		return new BlobChange(changeType, oldBlobIdent, newBlobIdent, WhitespaceOption.DEFAULT) {

			@Override
			public Project getProject() {
				return context.getProject();
			}

		};
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CommitOptionResourceReference()));
	}
	
	public void onContentChange(IPartialPageRequestHandler partialPageRequestHandler) {
		Preconditions.checkNotNull(newContentProvider);
		
		if (context.getMode() == Mode.EDIT) {
			contentModified = !Arrays.equals(
					newContentProvider.get(), 
					context.getProject().getBlob(context.getBlobIdent(), true).getBytes());
		} else {
			contentModified = newContentProvider.get().length != 0;
		}
		onBlobChange(partialPageRequestHandler);
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		if (event.getPayload() instanceof BlobNameChanging) {
			BlobNameChanging payload = (BlobNameChanging) event.getPayload();
			onBlobChange(payload.getHandler());
		}
	}

	private void onBlobChange(IPartialPageRequestHandler partialPageRequestHandler) {
		String script = String.format("onedev.server.commitOption.onBlobChange('%s', '%s', %b);", getMarkupId(), 
				JavaScriptEscape.escapeJavaScript(getDefaultCommitMessage()), isBlobModified());
		partialPageRequestHandler.appendJavaScript(script);
	}
	
}
