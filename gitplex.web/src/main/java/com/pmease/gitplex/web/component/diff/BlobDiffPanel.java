package com.pmease.gitplex.web.component.diff;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.BlobText;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.commons.util.MediaTypes;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.extensionpoint.DiffRenderer;
import com.pmease.gitplex.web.extensionpoint.DiffRendererProvider;

@SuppressWarnings("serial")
public class BlobDiffPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final RevAwareChange change;
	
	private final InlineCommentSupport commentSupport;
	
	public BlobDiffPanel(String id, IModel<Repository> repoModel, RevAwareChange change, 
			InlineCommentSupport commentSupport) {
		super(id);
		
		this.repoModel = repoModel;
		this.change = change;
		this.commentSupport = commentSupport;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);

		int oldBlobType = change.getOldMode() & FileMode.TYPE_MASK;
		int newBlobType = change.getNewMode() & FileMode.TYPE_MASK;

		MediaType oldMediaType, newMediaType;
		
		if (oldBlobType == FileMode.TYPE_GITLINK || oldBlobType == FileMode.TYPE_SYMLINK) {
			oldMediaType = MediaType.TEXT_PLAIN;
		} else if (oldBlobType == FileMode.TYPE_FILE) {
			byte[] oldContent = repoModel.getObject().getBlobContent(change.getOldBlobInfo());
			oldMediaType = MediaTypes.detectFrom(oldContent, change.getOldPath());
		} else {
			oldMediaType = null;
		}
		
		if (newBlobType == FileMode.TYPE_GITLINK || newBlobType == FileMode.TYPE_SYMLINK) {
			newMediaType = MediaType.TEXT_PLAIN;
		} else if (newBlobType == FileMode.TYPE_FILE) {
			byte[] newContent = repoModel.getObject().getBlobContent(change.getNewBlobInfo());
			newMediaType = MediaTypes.detectFrom(newContent, change.getNewPath());
		} else {
			newMediaType = null;
		}

		MediaType mediaType;
		if (oldMediaType != null) {
			if (newMediaType != null) {
				if (oldMediaType.equals(newMediaType)) 
					mediaType = oldMediaType;
				else
					mediaType = null;
			} else {
				mediaType = oldMediaType;
			}
		} else {
			mediaType = newMediaType;
		}

		boolean rendered = false;
		if (mediaType != null) {
			DiffRenderer renderer = null;
			for (DiffRendererProvider provider: GitPlex.getExtensions(DiffRendererProvider.class)) {
				renderer = provider.getDiffRenderer(mediaType);
				if (renderer != null)
					break;
			}
			if (renderer != null) {
				add(renderer.render("content", repoModel, change));
				rendered = true;
			}
		}
		
		if (!rendered) {
			BlobText oldText;
			if (change.getOldPath() != null)
				oldText = repoModel.getObject().getBlobText(change.getOldBlobInfo());
			else
				oldText = null;
			
			BlobText newText;
			if (change.getNewPath() != null)
				newText = repoModel.getObject().getBlobText(change.getNewBlobInfo());
			else
				newText = null;

			if (change.getOldPath() != null && oldText == null || change.getNewPath() != null && newText == null) {
				Fragment fragment = new Fragment("content", "binaryFrag", this);
				fragment.add(new FileDiffTitle("title", change));
				add(fragment);
			} else {
				add(new TextDiffPanel("content", repoModel, change, commentSupport));
			}
		}
		
	}
	
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
