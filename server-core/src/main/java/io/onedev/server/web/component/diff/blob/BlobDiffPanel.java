package io.onedev.server.web.component.diff.blob;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import io.onedev.server.OneDev;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobChange;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.diff.DiffRenderer;
import io.onedev.server.web.component.diff.blob.text.TextDiffPanel;
import io.onedev.server.web.component.diff.difftitle.BlobDiffTitle;
import io.onedev.server.web.component.diff.revision.DiffViewMode;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.util.DiffPlanarRange;

@SuppressWarnings("serial")
public class BlobDiffPanel extends Panel {

	private static final String CONTENT_ID = "content";
	
	private final BlobChange change;
	
	private final IModel<Boolean> blameModel;
	
	private final DiffViewMode diffMode;
	
	public BlobDiffPanel(String id, BlobChange change, DiffViewMode diffMode, @Nullable IModel<Boolean> blameModel) {
		super(id);
		
		this.change = change;
		this.blameModel = blameModel;
		this.diffMode = diffMode;
	}
	
	private Fragment newFragment(String message, boolean warning) {
		Fragment fragment = new Fragment(CONTENT_ID, "noDiffFrag", this);
		fragment.add(new BlobDiffTitle("title", change));
		if (warning)
			fragment.add(new SpriteImage("icon", "warning"));
		else
			fragment.add(new SpriteImage("icon", "info-circle"));
		fragment.add(new Label("message", message));
		return fragment;
	}
	
	@Nullable
	protected PullRequest getPullRequest() {
		return null;
	}
	
	private void showBlob(Blob blob) {
		if (blob.getText() != null) {
			if (blob.getText().getLines().size() > DiffUtils.MAX_DIFF_SIZE) {
				add(newFragment("Unable to diff as the file is too large.", true));
			} else if (change.getAdditions()+change.getDeletions() > WebConstants.MAX_SINGLE_DIFF_LINES) {
				add(newFragment("Diff is too large to be displayed.", true));
			} else if (change.getDiffBlocks().isEmpty()) {
				if (change.getNewBlobIdent().path != null)
					add(newFragment("Empty file added.", false));
				else
					add(newFragment("Empty file removed.", false));
			} else {
				add(new TextDiffPanel(CONTENT_ID, change, diffMode, blameModel) {

					@Override
					protected PullRequest getPullRequest() {
						return BlobDiffPanel.this.getPullRequest();
					}
					
				});
			}
		} else if (blob.isPartial()) {
			add(newFragment("File is too large to be loaded.", true));
		} else {
			Panel diffPanel = null;
			for (DiffRenderer renderer: OneDev.getExtensions(DiffRenderer.class)) {
				diffPanel = renderer.render(CONTENT_ID, blob.getMediaType(), change, diffMode);
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
				} else if (change.getAdditions() + change.getDeletions() > WebConstants.MAX_SINGLE_DIFF_LINES) {
					add(newFragment("Diff is too large to be displayed.", true));
				} else if (change.getAdditions() + change.getDeletions() == 0) {
					add(newFragment("Content is identical", false));
				} else {
					add(new TextDiffPanel(CONTENT_ID, change, diffMode, blameModel) {

						@Override
						protected PullRequest getPullRequest() {
							return BlobDiffPanel.this.getPullRequest();
						}
						
					});
				}
			} else if (change.getOldBlob().isPartial() || change.getNewBlob().isPartial()) {
				add(newFragment("File is too large to be loaded.", true));
			} else if (change.getOldBlob().getMediaType().equals(change.getNewBlob().getMediaType())) {
				Panel diffPanel = null;
				for (DiffRenderer renderer: OneDev.getExtensions(DiffRenderer.class)) {
					diffPanel = renderer.render(CONTENT_ID, change.getNewBlob().getMediaType(), change, diffMode);
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
	protected void onDetach() {
		if (blameModel != null)
			blameModel.detach();
		
		super.onDetach();
	}

	public void onCommentDeleted(AjaxRequestTarget target) {
		Component content = get(CONTENT_ID);
		if (content instanceof TextDiffPanel) 
			((TextDiffPanel) content).onCommentDeleted(target);
	}

	public void onCommentClosed(AjaxRequestTarget target) {
		Component content = get(CONTENT_ID);
		if (content instanceof TextDiffPanel)
			((TextDiffPanel) content).onCommentClosed(target);
	}

	public void onCommentAdded(AjaxRequestTarget target, CodeComment comment, DiffPlanarRange range) {
		Component content = get(CONTENT_ID);
		if (content instanceof TextDiffPanel) 
			((TextDiffPanel) content).onCommentAdded(target, comment, range);
	}

	public void mark(AjaxRequestTarget target, DiffPlanarRange markRange) {
		Component content = get(CONTENT_ID);
		if (content instanceof TextDiffPanel)
			((TextDiffPanel) content).mark(target, markRange);
	}

	public void unmark(AjaxRequestTarget target) {
		Component content = get(CONTENT_ID);
		if (content instanceof TextDiffPanel)
			((TextDiffPanel) content).unmark(target);
	}
	
	public void onUnblame(AjaxRequestTarget target) {
		Component content = get(CONTENT_ID);
		if (content instanceof TextDiffPanel) 
			((TextDiffPanel) content).onUnblame(target);
	}

}
