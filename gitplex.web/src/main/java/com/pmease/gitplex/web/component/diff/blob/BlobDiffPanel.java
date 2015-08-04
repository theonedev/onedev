package com.pmease.gitplex.web.component.diff.blob;

import javax.annotation.Nullable;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobChange;
import com.pmease.commons.lang.diff.DiffUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.diff.blob.text.TextDiffPanel;
import com.pmease.gitplex.web.component.diff.difftitle.BlobDiffTitle;

@SuppressWarnings("serial")
public class BlobDiffPanel extends Panel {
	
	private static final String CONTENT_ID = "content";
	
	private final IModel<Repository> repoModel;
	
	private final BlobChange change;
	
	private final boolean unified;
	
	private final InlineCommentSupport commentSupport;
	
	public BlobDiffPanel(String id, IModel<Repository> repoModel, BlobChange change, 
			boolean unified, @Nullable InlineCommentSupport commentSupport) {
		super(id);
		
		this.repoModel = repoModel;
		this.change = change;
		this.unified = unified;
		this.commentSupport = commentSupport;
	}
	
	private Fragment newFragment(String message, boolean warning) {
		Fragment fragment = new Fragment(CONTENT_ID, "noDiffFrag", this);
		fragment.add(new BlobDiffTitle("title", change));
		if (warning)
			fragment.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", "fa fa-warning")));
		else
			fragment.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", "fa fa-info-circle")));
		fragment.add(new Label("message", message));
		return fragment;
	}
	
	private void showBlob(Blob blob) {
		if (blob.getText() != null) {
			if (blob.getText().getLines().size() > DiffUtils.MAX_DIFF_SIZE) {
				add(newFragment("Unable to diff as the file is too large.", true));
			} else if (change.getDiffBlocks().size() > TextDiffPanel.MAX_DISPLAY_SIZE) {
				add(newFragment("Diff is too large to be displayed.", true));
			} else if (change.getDiffBlocks().isEmpty()) {
				if (change.getOldBlobIdent().path != null)
					add(newFragment("Empty file removed.", false));
				else
					add(newFragment("Empty file added.", false));
			} else {
				add(new TextDiffPanel(CONTENT_ID, repoModel, change, unified, commentSupport));
			}
		} else if (blob.isPartial()) {
			add(newFragment("File is too large to be loaded.", true));
		} else {
			Panel diffPanel = null;
			for (DiffRenderer renderer: GitPlex.getExtensions(DiffRenderer.class)) {
				diffPanel = renderer.render(CONTENT_ID, blob.getMediaType(), change);
				if (diffPanel != null)
					break;
			}
			if (diffPanel != null)
				add(diffPanel);
			else
				add(newFragment("Binary file.", false));
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (change.getType() == ChangeType.ADD) {
			showBlob(change.getNewBlob());
		} else if (change.getType() == ChangeType.DELETE) {
			showBlob(change.getOldBlob());
		} else {
			if (change.getOldText() != null && change.getNewText() != null) {
				if (change.getOldText().getLines().size() + change.getNewText().getLines().size() > DiffUtils.MAX_DIFF_SIZE) {
					add(newFragment("Unable to diff as the file is too large.", true));
				} else if (change.getDiffBlocks().size() > TextDiffPanel.MAX_DISPLAY_SIZE) {
					add(newFragment("Diff is too large to be displayed.", true));
				} else if (change.getDiffBlocks().isEmpty()) {
					add(newFragment("File is identical if " + change.getLineProcessor().getName() + ".", false));
				} else {
					add(new TextDiffPanel(CONTENT_ID, repoModel, change, unified, commentSupport));
				}
			} else if (change.getOldBlob().isPartial() || change.getNewBlob().isPartial()) {
				add(newFragment("File is too large to be loaded.", true));
			} else if (change.getOldBlob().getMediaType().equals(change.getNewBlob().getMediaType())) {
				Panel diffPanel = null;
				for (DiffRenderer renderer: GitPlex.getExtensions(DiffRenderer.class)) {
					diffPanel = renderer.render(CONTENT_ID, change.getNewBlob().getMediaType(), change);
					if (diffPanel != null)
						break;
				}
				if (diffPanel != null)
					add(diffPanel);
				else
					add(newFragment("Binary file.", false));
			} else {
				add(newFragment("Binary file.", false));
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
