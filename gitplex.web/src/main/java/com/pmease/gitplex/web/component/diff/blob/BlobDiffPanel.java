package com.pmease.gitplex.web.component.diff.blob;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.Blob;
import com.pmease.commons.git.Change;
import com.pmease.commons.util.ContentDetector;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.diff.difftitle.FileDiffTitle;
import com.pmease.gitplex.web.component.diff.text.TextDiffPanel;
import com.pmease.gitplex.web.extensionpoint.DiffRenderer;
import com.pmease.gitplex.web.extensionpoint.DiffRendererProvider;

@SuppressWarnings("serial")
public class BlobDiffPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final Change change;
	
	private final InlineCommentSupport commentSupport;
	
	public BlobDiffPanel(String id, IModel<Repository> repoModel, 
			Change change, InlineCommentSupport commentSupport) {
		super(id);
		
		this.repoModel = repoModel;
		this.change = change;
		this.commentSupport = commentSupport;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);

		int oldBlobType = change.getOldBlobIdent().mode & FileMode.TYPE_MASK;
		int newBlobType = change.getNewBlobIdent().mode & FileMode.TYPE_MASK;

		MediaType oldMediaType, newMediaType;
		
		if (oldBlobType == FileMode.TYPE_GITLINK || oldBlobType == FileMode.TYPE_SYMLINK) {
			oldMediaType = MediaType.TEXT_PLAIN;
		} else if (oldBlobType == FileMode.TYPE_FILE) {
			byte[] oldContent = repoModel.getObject().getBlob(change.getOldBlobIdent()).getBytes();
			oldMediaType = ContentDetector.detectMediaType(oldContent, change.getOldBlobIdent().path);
		} else {
			oldMediaType = null;
		}
		
		if (newBlobType == FileMode.TYPE_GITLINK || newBlobType == FileMode.TYPE_SYMLINK) {
			newMediaType = MediaType.TEXT_PLAIN;
		} else if (newBlobType == FileMode.TYPE_FILE) {
			byte[] newContent = repoModel.getObject().getBlob(change.getNewBlobIdent()).getBytes();
			newMediaType = ContentDetector.detectMediaType(newContent, change.getNewBlobIdent().path);
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
			Blob.Text oldText;
			if (change.getOldBlobIdent().path != null)
				oldText = repoModel.getObject().getBlob(change.getOldBlobIdent()).getText();
			else
				oldText = null;
			
			Blob.Text newText;
			if (change.getNewBlobIdent().path != null)
				newText = repoModel.getObject().getBlob(change.getNewBlobIdent()).getText();
			else
				newText = null;

			if (change.getOldBlobIdent().path != null && oldText == null || change.getNewBlobIdent().path != null && newText == null) {
				Fragment fragment = new Fragment("content", "binaryFrag", this);
				fragment.add(new FileDiffTitle("title", change));
				add(fragment);
			} else {
				add(new TextDiffPanel("content", repoModel, change, commentSupport));
			}
		}
		
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(BlobDiffPanel.class, "blob-diff.css")));
	}

	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
