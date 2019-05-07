package io.onedev.server.web.page.project.blob.render.commitoption;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.unbescape.javascript.JavaScriptEscape;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.commons.utils.Provider;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobChange;
import io.onedev.server.git.BlobContent;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.exception.NotTreeException;
import io.onedev.server.git.exception.ObjectAlreadyExistsException;
import io.onedev.server.git.exception.ObsoleteCommitException;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.ajaxlistener.TrackViewStateListener;
import io.onedev.server.web.component.diff.blob.BlobDiffPanel;
import io.onedev.server.web.component.diff.revision.DiffViewMode;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.page.project.blob.RevisionResolved;
import io.onedev.server.web.page.project.blob.navigator.BlobNameChanging;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;

@SuppressWarnings("serial")
public class CommitOptionPanel extends Panel {

	private final BlobRenderContext context;
	
	private final Provider<byte[]> newContentProvider;
	
	private String commitSummary;
	
	private String commitDetail;
	
	private BlobChange change;
	
	private boolean contentModified;
	
	private Set<String> oldPaths;
	
	private final String autosaveKey;
	
	public CommitOptionPanel(String id, BlobRenderContext context, @Nullable Provider<byte[]> newContentProvider) {
		super(id);

		this.context = context;
		this.newContentProvider = newContentProvider;

		oldPaths = new HashSet<>();
		String oldPath = getOldPath();
		if (oldPath != null)
			oldPaths.add(oldPath);
		
		if (context.getMode() != Mode.DELETE)
			autosaveKey = context.getAutosaveKey();
		else
			autosaveKey = null;
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
	
	private void newChangedContainer(@Nullable AjaxRequestTarget target) {
		WebMarkupContainer changedContainer = new WebMarkupContainer("changed");
		changedContainer.setVisible(change != null);
		changedContainer.setOutputMarkupPlaceholderTag(true);
		if (change != null) {
			changedContainer.add(new BlobDiffPanel("changes", new AbstractReadOnlyModel<Project>() {

				@Override
				public Project getObject() {
					return context.getProject();
				}
				
			}, new Model<PullRequest>(null), change, DiffViewMode.UNIFIED, null, null));
		} else {
			changedContainer.add(new WebMarkupContainer("changes"));
		}
		if (target != null) {
			replace(changedContainer);
			target.add(changedContainer);
			if (change != null) {
				String script = String.format("$('#%s .commit-option input[type=submit]').val('Commit and overwrite');", 
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
		
		FeedbackPanel feedback = new NotificationPanel("feedback", this);
		feedback.setOutputMarkupPlaceholderTag(true);
		add(feedback);
				
		newChangedContainer(null);
		
		Form<?> form = new Form<Void>("form");
		add(form);

		form.add(new TextField<String>("commitSummary", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return commitSummary;
			}

			@Override
			public void setObject(String object) {
				commitSummary = object;
			}
			
		}) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("placeholder", getDefaultCommitMessage());
			}
			
		});
		
		form.add(new TextArea<String>("commitDetail", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return commitDetail;
			}

			@Override
			public void setObject(String object) {
				commitDetail = object;
			}
			
		}));
		
		AjaxButton saveButton = new AjaxButton("save") {

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
				if (save(target, feedback)) {
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
				context.onModeChange(target, Mode.VIEW);
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
	
	private boolean save(AjaxRequestTarget target, FeedbackPanel feedback) {
		change = null;
		
		if (newContentProvider != null && StringUtils.isBlank(context.getNewPath())) {
			CommitOptionPanel.this.error("Please specify file name.");
			target.add(feedback);
			return false;
		} else {
			String commitMessage = commitSummary;
			if (StringUtils.isBlank(commitMessage))
				commitMessage = getDefaultCommitMessage();
			if (StringUtils.isNotBlank(commitDetail))
				commitMessage += "\n\n" + commitDetail;
			User user = Preconditions.checkNotNull(OneDev.getInstance(UserManager.class).getCurrent());

			String refName = GitUtils.branch2ref(context.getBlobIdent().revision);
			ObjectId prevCommitId = context.getProject().getObjectId(context.getBlobIdent().revision, true);
			
			Repository repository = context.getProject().getRepository();
			ObjectId newCommitId = null;

			Map<String, BlobContent> newBlobs = new HashMap<>();
			if (newContentProvider != null) {
				if (OneDev.getInstance(ProjectManager.class).isModificationNeedsQualityCheck(SecurityUtils.getUser(), 
						context.getProject(), context.getBlobIdent().revision, context.getNewPath())) {
					CommitOptionPanel.this.error("Adding of file '" + context.getNewPath() + "' need to be reviewed/verified. "
							+ "Please submit pull request instead");
					target.add(feedback);
					return false;
				}
				
				newBlobs.put(context.getNewPath(), new BlobContent() {

					@Override
					public byte[] getBytes() {
						return newContentProvider.get();
					}

					@Override
					public FileMode getMode() {
						if (context.getBlobIdent().isFile())
							return FileMode.fromBits(context.getBlobIdent().mode);
						else
							return FileMode.REGULAR_FILE;
					}

				});
			}
			
			while(newCommitId == null) {
				try {
					newCommitId = new BlobEdits(oldPaths, newBlobs).commit(repository, refName, 
							prevCommitId, prevCommitId, user.asPerson(), commitMessage);
				} catch (ObjectAlreadyExistsException e) {
					CommitOptionPanel.this.error("A path with same name already exists. "
							+ "Please choose a different name and try again.");
					target.add(feedback);
					break;
				} catch (NotTreeException e) {
					CommitOptionPanel.this.error("A file exists where you’re trying to create a subdirectory. "
							+ "Choose a new path and try again..");
					target.add(feedback);
					break;
				} catch (ObsoleteCommitException e) {
					try (RevWalk revWalk = new RevWalk(repository)) {
						RevCommit prevCommit = revWalk.parseCommit(prevCommitId);
						send(this, Broadcast.BUBBLE, new RevisionResolved(target, e.getOldCommitId()));
						RevCommit currentCommit = revWalk.parseCommit(e.getOldCommitId());
						prevCommitId = e.getOldCommitId();

						if (!oldPaths.isEmpty()) {
							TreeWalk treeWalk = TreeWalk.forPath(repository, oldPaths.iterator().next(), 
									prevCommit.getTree().getId(), currentCommit.getTree().getId());
							Preconditions.checkNotNull(treeWalk);
							if (!treeWalk.getObjectId(0).equals(treeWalk.getObjectId(1)) 
									|| !treeWalk.getFileMode(0).equals(treeWalk.getFileMode(1))) {
								// mark changed if original file exists and content or mode has been modified
								// by others
								if (treeWalk.getObjectId(1).equals(ObjectId.zeroId())) {
									if (newContentProvider != null) {
										oldPaths.clear();
										change = getChange(treeWalk, prevCommit, currentCommit);
										break;
									} else {
										newCommitId = e.getOldCommitId();
									}
								} else {
									change = getChange(treeWalk, prevCommit, currentCommit);
									break;
								}
							} 
						} 
					} catch (IOException e2) {
						throw new RuntimeException(e2);
					}
				}
			}
			if (newCommitId != null) {
				RefUpdated refUpdated = new RefUpdated(context.getProject(), refName, prevCommitId, newCommitId);
				context.onCommitted(target, refUpdated);
				if (autosaveKey != null)
					target.appendJavaScript(String.format("localStorage.removeItem('%s');", autosaveKey));
				return true;
			} else {
				newChangedContainer(target);
				return false;
			}
		}
	}
	
	private BlobChange getChange(TreeWalk treeWalk, RevCommit oldCommit, RevCommit newCommit) {
		DiffEntry.ChangeType changeType = DiffEntry.ChangeType.MODIFY;
		BlobIdent oldBlobIdent;
		if (!treeWalk.getObjectId(0).equals(ObjectId.zeroId())) {
			oldBlobIdent = new BlobIdent(oldCommit.name(), treeWalk.getPathString(), treeWalk.getRawMode(0));
		} else {
			oldBlobIdent = new BlobIdent(oldCommit.name(), null, FileMode.TREE.getBits());
			changeType = DiffEntry.ChangeType.ADD;
		}
		
		BlobIdent newBlobIdent;
		if (!treeWalk.getObjectId(1).equals(ObjectId.zeroId())) {
			newBlobIdent = new BlobIdent(newCommit.name(), treeWalk.getPathString(), treeWalk.getRawMode(1));
		} else {
			newBlobIdent = new BlobIdent(newCommit.name(), null, FileMode.TREE.getBits());
			changeType = DiffEntry.ChangeType.DELETE;
		}
		
		return new BlobChange(changeType, oldBlobIdent, newBlobIdent, WhitespaceOption.DEFAULT) {

			@Override
			public Blob getBlob(BlobIdent blobIdent) {
				return context.getProject().getBlob(blobIdent, true);
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
