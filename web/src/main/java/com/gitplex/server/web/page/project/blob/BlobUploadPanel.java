package com.gitplex.server.web.page.project.blob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.GitPlex;
import com.gitplex.server.git.BlobContent;
import com.gitplex.server.git.BlobEdits;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.git.exception.NotTreeException;
import com.gitplex.server.git.exception.ObjectAlreadyExistsException;
import com.gitplex.server.git.exception.ObsoleteCommitException;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.manager.ReviewManager;
import com.gitplex.server.model.User;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.component.dropzonefield.DropzoneField;
import com.gitplex.server.web.page.project.blob.render.BlobRenderContext;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
abstract class BlobUploadPanel extends Panel {

	private static final int MAX_FILE_SIZE = 10; // In meta bytes
	
	private final BlobRenderContext context;
	
	private String summaryCommitMessage;
	
	private String detailCommitMessage;
	
	private final Collection<FileUpload> uploads = new ArrayList<>();
	
	public BlobUploadPanel(String id, BlobRenderContext context) {
		super(id);
		this.context = context;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		form.setMultiPart(true);
		form.setFileMaxSize(Bytes.megabytes(MAX_FILE_SIZE));
		add(form);
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		NotificationPanel feedback = new NotificationPanel("feedback", form);
		feedback.setOutputMarkupPlaceholderTag(true);
		form.add(feedback);
		
		form.add(new DropzoneField("files", 
				new PropertyModel<Collection<FileUpload>>(this, "uploads"), MAX_FILE_SIZE).setRequired(true));
		form.add(new AjaxButton("upload") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				ReviewManager reviewManager = GitPlex.getInstance(ReviewManager.class);
				Map<String, BlobContent> newBlobs = new HashMap<>();
				for (FileUpload upload: uploads) {
					String blobPath = upload.getClientFileName();
					if (context.getBlobIdent().path != null)
						blobPath = context.getBlobIdent().path + "/" + blobPath;
					
					if (!reviewManager.canModify(SecurityUtils.getUser(), context.getProject(), 
							context.getBlobIdent().revision, blobPath)) {
						form.error("Adding of file '" + blobPath + "' need to be reviewed. "
								+ "Please submit pull request instead");
						target.add(feedback);
						return;
					}
					BlobContent blobContent = new BlobContent.Immutable(upload.getBytes(), FileMode.REGULAR_FILE);
					newBlobs.put(blobPath, blobContent);
				}

				BlobEdits blobEdits = new BlobEdits(Sets.newHashSet(), newBlobs);
				String refName = GitUtils.branch2ref(context.getBlobIdent().revision);

				String commitMessage = summaryCommitMessage;
				if (StringUtils.isBlank(commitMessage))
					commitMessage = "Add files via upload";
				
				if (StringUtils.isNotBlank(detailCommitMessage))
					commitMessage += "\n\n" + detailCommitMessage;
				User user = Preconditions.checkNotNull(GitPlex.getInstance(UserManager.class).getCurrent());

				ObjectId prevCommitId = context.getProject().getObjectId(context.getBlobIdent().revision);

				ObjectId newCommitId = null;
				while (newCommitId == null) {
					try {
						newCommitId = blobEdits.commit(context.getProject().getRepository(), refName, prevCommitId, 
								prevCommitId, user.asPerson(), commitMessage);
					} catch (ObjectAlreadyExistsException e) {
						form.error(e.getMessage());
						target.add(feedback);
						break;
					} catch (NotTreeException e) {
						form.error(e.getMessage());
						target.add(feedback);
						break;
					} catch (ObsoleteCommitException e) {
						prevCommitId = e.getOldCommitId();
					}
				}
				 
				if (newCommitId != null)
					onCommitted(target, prevCommitId, newCommitId);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(feedback);
			}
			
		});
		
		form.add(new TextField<String>("summaryCommitMessage", 
				new PropertyModel<String>(this, "summaryCommitMessage")));
		form.add(new TextArea<String>("detailCommitMessage", new PropertyModel<String>(this, "detailCommitMessage")));
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
	}

	abstract void onCommitted(AjaxRequestTarget target, ObjectId oldCommit, ObjectId newCommit);
	
	abstract void onCancel(AjaxRequestTarget target);
}
