package com.pmease.gitplex.web.component.diff.blob;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
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
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.diff.DiffRenderer;
import com.pmease.gitplex.web.component.diff.blob.text.TextDiffPanel;
import com.pmease.gitplex.web.component.diff.difftitle.BlobDiffTitle;
import com.pmease.gitplex.web.component.diff.revision.BlobMarkSupport;
import com.pmease.gitplex.web.component.diff.revision.DiffMark;
import com.pmease.gitplex.web.component.diff.revision.DiffViewMode;

@SuppressWarnings("serial")
public class BlobDiffPanel extends Panel implements MarkAware {

	private static final String CONTENT_ID = "content";
	
	private final IModel<Depot> depotModel;
	
	private final IModel<PullRequest> requestModel;
	
	private final BlobChange change;
	
	private final DiffViewMode diffMode;
	
	private final BlobMarkSupport markSupport;
	
	public BlobDiffPanel(String id, IModel<Depot> depotModel, IModel<PullRequest> requestModel, 
			BlobChange change, DiffViewMode diffMode, @Nullable BlobMarkSupport markSupport) {
		super(id);
		
		this.depotModel = depotModel;
		this.requestModel = requestModel;
		this.change = change;
		this.diffMode = diffMode;
		this.markSupport = markSupport;
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
			} else if (change.getAdditions()+change.getDeletions() > Constants.MAX_SINGLE_FILE_DIFF_LINES) {
				add(newFragment("Diff is too large to be displayed.", true));
			} else if (change.getDiffBlocks().isEmpty()) {
				if (change.getNewBlobIdent().path != null)
					add(newFragment("Empty file added.", false));
				else
					add(newFragment("Empty file removed.", false));
			} else {
				add(new TextDiffPanel(CONTENT_ID, depotModel, requestModel, change, diffMode, markSupport));
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
		
		if (change.getType() == ChangeType.ADD || change.getType() == ChangeType.COPY) {
			showBlob(change.getNewBlob());
		} else if (change.getType() == ChangeType.DELETE) {
			showBlob(change.getOldBlob());
		} else {
			if (change.getOldText() != null && change.getNewText() != null) {
				if (change.getOldText().getLines().size() + change.getNewText().getLines().size() > DiffUtils.MAX_DIFF_SIZE) {
					add(newFragment("Unable to diff as the file is too large.", true));
				} else if (change.getAdditions() + change.getDeletions() > Constants.MAX_SINGLE_FILE_DIFF_LINES) {
					add(newFragment("Diff is too large to be displayed.", true));
				} else if (change.getAdditions() + change.getDeletions() == 0 
						&& (markSupport == null || markSupport.getComments().isEmpty())) {
					add(newFragment("Content is identical", false));
				} else {
					add(new TextDiffPanel(CONTENT_ID, depotModel, requestModel, change, diffMode, markSupport));
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
		depotModel.detach();
		requestModel.detach();
		
		super.onDetach();
	}

	@Override
	public void onCommentDeleted(AjaxRequestTarget target, CodeComment comment) {
		Component content = get(CONTENT_ID);
		if (content instanceof MarkAware) {
			MarkAware markAware = (MarkAware) content;
			markAware.onCommentDeleted(target, comment);
		}
	}

	@Override
	public void onCommentClosed(AjaxRequestTarget target, CodeComment comment) {
		Component content = get(CONTENT_ID);
		if (content instanceof MarkAware) {
			MarkAware markAware = (MarkAware) content;
			markAware.onCommentClosed(target, comment);
		}
	}

	@Override
	public void onCommentAdded(AjaxRequestTarget target, CodeComment comment) {
		Component content = get(CONTENT_ID);
		if (content instanceof MarkAware) {
			MarkAware markAware = (MarkAware) content;
			markAware.onCommentAdded(target, comment);
		}
	}

	@Override
	public void mark(AjaxRequestTarget target, DiffMark mark) {
		Component content = get(CONTENT_ID);
		if (content instanceof MarkAware) {
			MarkAware markAware = (MarkAware) content;
			markAware.mark(target, mark);
		}
	}
	
	@Override
	public void clearMark(AjaxRequestTarget target) {
		Component content = get(CONTENT_ID);
		if (content instanceof MarkAware) {
			MarkAware markAware = (MarkAware) content;
			markAware.clearMark(target);
		}
	}
	
}
