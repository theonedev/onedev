package com.pmease.gitplex.web.component.diff;

import static com.pmease.commons.git.Change.Status.UNCHANGED;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.git.BlobText;
import com.pmease.commons.util.MediaTypes;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.CommentAwareChange;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.gitlink.GitLink;
import com.pmease.gitplex.web.component.symbollink.SymbolLink;
import com.pmease.gitplex.web.component.view.BlobViewPanel;
import com.pmease.gitplex.web.extensionpoint.DiffRenderer;
import com.pmease.gitplex.web.extensionpoint.DiffRendererProvider;

@SuppressWarnings("serial")
public class BlobDiffPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final CommentAwareChange change;
	
	public BlobDiffPanel(String id, IModel<Repository> repoModel, CommentAwareChange change) {
		super(id);
		
		this.repoModel = repoModel;
		this.change = change;
	}
	
	public CommentAwareChange getChange() {
		return change;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		int oldBlobType = change.getOldMode() & FileMode.TYPE_MASK;
		int newBlobType = change.getNewMode() & FileMode.TYPE_MASK;
		
		if (change.getStatus() == UNCHANGED) {
			add(new WebMarkupContainer("oldContent").setVisible(false));
			add(new WebMarkupContainer("newContent").setVisible(false));
			Fragment fragment = new Fragment("blobContent", "notChangedFrag", this);
			fragment.add(new Label("title", change.getOldPath()));
			BlobInfo renderInfo = new BlobInfo(change.getNewRevision(), 
					change.getNewPath(), change.getNewMode());
			fragment.add(new BlobViewPanel("content", repoModel, renderInfo));
			add(fragment);
		} else if (oldBlobType == FileMode.TYPE_GITLINK && newBlobType == FileMode.TYPE_GITLINK) {
			add(new WebMarkupContainer("oldContent").setVisible(false));
			add(new WebMarkupContainer("newContent").setVisible(false));

			Fragment fragment = new Fragment("blobContent", "nonFileDiffFrag", this);
			fragment.add(new Label("renamedTitle", change.getOldPath())
					.setVisible(!change.getOldPath().equals(change.getNewPath())));
			fragment.add(new Label("title", change.getNewPath()));
			BlobText oldText = repoModel.getObject().getBlobText(change.getOldBlobInfo());
			BlobText newText = repoModel.getObject().getBlobText(change.getNewBlobInfo());
			fragment.add(new GitLink("oldContent", oldText.getLines().get(0)));
			fragment.add(new GitLink("newContent", newText.getLines().get(0)));
			add(fragment);
		} else if (oldBlobType == FileMode.TYPE_SYMLINK && newBlobType == FileMode.TYPE_SYMLINK) {
			add(new WebMarkupContainer("oldContent").setVisible(false));
			add(new WebMarkupContainer("newContent").setVisible(false));

			Fragment fragment = new Fragment("blobContent", "nonFileDiffFrag", this);
			fragment.add(new Label("renamedTitle", change.getOldPath())
					.setVisible(!change.getOldPath().equals(change.getNewPath())));
			fragment.add(new Label("title", change.getNewPath()));
			BlobText oldText = repoModel.getObject().getBlobText(change.getOldBlobInfo());
			BlobText newText = repoModel.getObject().getBlobText(change.getNewBlobInfo());
			fragment.add(new SymbolLink("oldContent", repoModel, change.getOldRevision(), 
					change.getOldPath(), oldText.getLines().get(0)));
			fragment.add(new SymbolLink("newContent", repoModel, change.getNewRevision(), 
					change.getNewPath(), newText.getLines().get(0)));
			add(fragment);
		} else if (oldBlobType == FileMode.TYPE_FILE && newBlobType == FileMode.TYPE_FILE) {
			add(new WebMarkupContainer("oldContent").setVisible(false));
			add(new WebMarkupContainer("newContent").setVisible(false));
			
			byte[] oldContent = repoModel.getObject().getBlobContent(change.getOldBlobInfo());
			byte[] newContent = repoModel.getObject().getBlobContent(change.getNewBlobInfo());
			MediaType oldMediaType = MediaTypes.detectFrom(oldContent, change.getOldPath());
			MediaType newMediaType = MediaTypes.detectFrom(newContent, change.getNewPath());
			
			DiffRenderer renderer = null;
			for (DiffRendererProvider provider: GitPlex.getExtensions(DiffRendererProvider.class)) {
				renderer = provider.getDiffRenderer(oldMediaType, newMediaType);
				if (renderer != null)
					break;
			}
			if (renderer != null) {
				add(renderer.render("blobContent", repoModel, change));
			} else {
				BlobText oldText = repoModel.getObject().getBlobText(change.getOldBlobInfo());
				BlobText newText = repoModel.getObject().getBlobText(change.getNewBlobInfo());
				if (oldText != null) {
					if (newText != null) {
						add(new TextDiffPanel("blobContent", repoModel, oldText, newText, change)); 
					} else if (newContent.length == 0) {
						add(new TextDiffPanel("blobContent", repoModel, oldText, new BlobText(), change)); 
					} else {
						Fragment fragment = new Fragment("blobContent", "binaryFileFrag", this);
						fragment.add(new FileDiffTitle("summary", change));
						add(fragment);
					}
				} else if (newText != null) {
					if (oldContent.length == 0) {
						add(new TextDiffPanel("blobContent", repoModel, new BlobText(), newText, change)); 
					} else {
						Fragment fragment = new Fragment("blobContent", "binaryFileFrag", this);
						fragment.add(new FileDiffTitle("summary", change));
						add(fragment);
					}
				} else if (oldContent.length == 0 && newContent.length == 0) {
					Fragment fragment = new Fragment("blobContent", "emptyFileFrag", this);
					fragment.add(new FileDiffTitle("summary", change));
					add(fragment);
				} else {
					Fragment fragment = new Fragment("blobContent", "binaryFileFrag", this);
					fragment.add(new FileDiffTitle("summary", change));
					add(fragment);
				}
			}
		} else {
			if (oldBlobType == FileMode.TYPE_SYMLINK) {
				BlobText oldText = repoModel.getObject().getBlobText(change.getOldBlobInfo());
				add(new SymbolLink("oldContent", repoModel, change.getOldRevision(), 
						change.getOldPath(), oldText.getLines().get(0)));
			} else if (oldBlobType == FileMode.TYPE_GITLINK) {
				BlobText oldText = repoModel.getObject().getBlobText(change.getOldBlobInfo());
				add(new GitLink("oldContent", oldText.getLines().get(0)));
			} else if (oldBlobType == FileMode.TYPE_FILE) {
				add(new BlobViewPanel("oldContent", repoModel, change.getOldBlobInfo()));
			} else {
				add(new WebMarkupContainer("oldContent").setVisible(false));
			}
			add(new Label("oldTitle", change.getOldPath()));
			
			if (newBlobType == FileMode.TYPE_SYMLINK) {
				BlobText newText = repoModel.getObject().getBlobText(change.getNewBlobInfo());
				add(new SymbolLink("newContent", repoModel, change.getNewRevision(), 
						change.getNewPath(), newText.getLines().get(0)));
			} else if (newBlobType == FileMode.TYPE_GITLINK) {
				BlobText newText = repoModel.getObject().getBlobText(change.getNewBlobInfo());
				add(new GitLink("newContent", newText.getLines().get(0)));
			} else if (newBlobType == FileMode.TYPE_FILE) {
				add(new BlobViewPanel("newContent", repoModel, change.getNewBlobInfo()));
			} else {
				add(new WebMarkupContainer("newContent").setVisible(false));
			}
			add(new Label("newTitle", change.getNewPath()));
			
			add(new WebMarkupContainer("blobContent").setVisible(false));
		}		
		
	}
	
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
