package io.onedev.server.web.component.diff.blob;

import javax.annotation.Nullable;

import org.apache.tika.mime.MediaType;
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
import io.onedev.server.git.LfsObject;
import io.onedev.server.git.LfsPointer;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.diff.DiffRenderer;
import io.onedev.server.web.component.diff.blob.text.BlobTextDiffPanel;
import io.onedev.server.web.component.diff.difftitle.BlobDiffTitle;
import io.onedev.server.web.component.diff.revision.DiffViewMode;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.util.DiffPlanarRange;

import static io.onedev.server.util.diff.DiffUtils.MAX_LINE_LEN;

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
		Component diffPanel = null;
		for (DiffRenderer renderer: OneDev.getExtensions(DiffRenderer.class)) {
			if (blob.getLfsPointer() != null 
					&& !new LfsObject(change.getProject().getId(), blob.getLfsPointer().getObjectId()).exists()) {
				diffPanel = newFragment("Storage file missing", true);
				break;
			}
			MediaType mediaType = change.getProject().detectMediaType(blob.getIdent());
			diffPanel = renderer.render(CONTENT_ID, mediaType, change, diffMode);
			if (diffPanel != null)
				break;
		}
		
		if (diffPanel != null) {
			add(diffPanel);
		} else if (blob.getText() != null) {
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
				add(new BlobTextDiffPanel(CONTENT_ID, change, diffMode, blameModel) {

					@Override
					protected PullRequest getPullRequest() {
						return BlobDiffPanel.this.getPullRequest();
					}
					
				});
			}
		} else {
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
			LfsPointer oldLfsPointer = change.getOldBlob().getLfsPointer();
			LfsPointer newLfsPointer = change.getNewBlob().getLfsPointer();
			if (oldLfsPointer != null && !new LfsObject(change.getProject().getId(), oldLfsPointer.getObjectId()).exists()
					|| newLfsPointer != null && !new LfsObject(change.getProject().getId(), newLfsPointer.getObjectId()).exists()) {
				add(newFragment("Storage file missing", true));
			} else {
				MediaType oldMediaType = change.getProject().detectMediaType(change.getOldBlobIdent());
				MediaType newMediaType = change.getProject().detectMediaType(change.getNewBlobIdent());
				
				Component diffPanel = null;
				
				if (oldMediaType.equals(newMediaType)) {
					for (DiffRenderer renderer: OneDev.getExtensions(DiffRenderer.class)) {
						diffPanel = renderer.render(CONTENT_ID, newMediaType, change, diffMode);
						if (diffPanel != null)
							break;
					}
				}
				
				if (diffPanel != null) {
					add(diffPanel);
				} else if (change.getOldText() != null && change.getNewText() != null) {
					if (change.getOldText().getLines().size() + change.getNewText().getLines().size() > DiffUtils.MAX_DIFF_SIZE) {
						add(newFragment("Unable to diff as the file is too large.", true));
					} else if (change.getOldText().getLines().stream().anyMatch(it->it.length()>MAX_LINE_LEN)
							|| change.getOldText().getLines().stream().anyMatch(it->it.length()>MAX_LINE_LEN)) {
						add(newFragment("Unable to diff as some line is too long.", true));
					} else if (change.getAdditions() + change.getDeletions() > WebConstants.MAX_SINGLE_DIFF_LINES) {
						add(newFragment("Diff is too large to be displayed.", true));
					} else if (change.getAdditions() + change.getDeletions() == 0) {
						add(newFragment("Content is identical", false));
					} else {
						add(new BlobTextDiffPanel(CONTENT_ID, change, diffMode, blameModel) {

							@Override
							protected PullRequest getPullRequest() {
								return BlobDiffPanel.this.getPullRequest();
							}
							
						});
					}
				} else {
					add(newFragment("Binary file.", false));
				}
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
		if (content instanceof BlobTextDiffPanel) 
			((BlobTextDiffPanel) content).onCommentDeleted(target);
	}

	public void onCommentClosed(AjaxRequestTarget target) {
		Component content = get(CONTENT_ID);
		if (content instanceof BlobTextDiffPanel)
			((BlobTextDiffPanel) content).onCommentClosed(target);
	}

	public void onCommentAdded(AjaxRequestTarget target, CodeComment comment, DiffPlanarRange range) {
		Component content = get(CONTENT_ID);
		if (content instanceof BlobTextDiffPanel) 
			((BlobTextDiffPanel) content).onCommentAdded(target, comment, range);
	}

	public void mark(AjaxRequestTarget target, DiffPlanarRange markRange) {
		Component content = get(CONTENT_ID);
		if (content instanceof BlobTextDiffPanel)
			((BlobTextDiffPanel) content).mark(target, markRange);
	}

	public void unmark(AjaxRequestTarget target) {
		Component content = get(CONTENT_ID);
		if (content instanceof BlobTextDiffPanel)
			((BlobTextDiffPanel) content).unmark(target);
	}
	
	public void onUnblame(AjaxRequestTarget target) {
		Component content = get(CONTENT_ID);
		if (content instanceof BlobTextDiffPanel) 
			((BlobTextDiffPanel) content).onUnblame(target);
	}

}
