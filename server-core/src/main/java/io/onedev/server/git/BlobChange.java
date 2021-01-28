package io.onedev.server.git;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;

import io.onedev.commons.jsyntax.Tokenized;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.code.CodeProblem;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.Mark;
import io.onedev.server.util.Pair;
import io.onedev.server.util.diff.DiffBlock;
import io.onedev.server.util.diff.DiffMatchPatch.Operation;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.util.DiffPlanarRange;

@SuppressWarnings("serial")
public abstract class BlobChange implements Serializable {

	protected final ChangeType type;
	
	protected final WhitespaceOption whitespaceOption;
	
	protected final BlobIdent oldBlobIdent;
	
	protected final BlobIdent newBlobIdent;
	
	private transient List<DiffBlock<Tokenized>> diffBlocks;
	
	public BlobChange(ChangeType type, BlobIdent oldBlobIdent, BlobIdent newBlobIdent, 
			WhitespaceOption whitespaceOption) {
		this.type = type;
		this.oldBlobIdent = oldBlobIdent;
		this.newBlobIdent = newBlobIdent;
		this.whitespaceOption = whitespaceOption;
	}

	public ChangeType getType() {
		return type;
	}

	public BlobIdent getOldBlobIdent() {
		return oldBlobIdent;
	}

	public BlobIdent getNewBlobIdent() {
		return newBlobIdent;
	}
	
	public BlobIdent getBlobIdent() {
		return newBlobIdent.path!=null? newBlobIdent: oldBlobIdent;
	}

	public String getPath() {
		return newBlobIdent.path != null? newBlobIdent.path: oldBlobIdent.path;
	}
	
	public List<DiffBlock<Tokenized>> getDiffBlocks() {
		if (diffBlocks == null) {
			try {
				if (type == ChangeType.ADD || type == ChangeType.COPY) {
					if (getNewText() != null) {
						List<String> newLines = getNewText().getLines();
						if (newLines.size() <= DiffUtils.MAX_DIFF_SIZE) {
							List<String> oldLines = new ArrayList<>();
							diffBlocks = DiffUtils.diff(
									oldLines, "a.txt", 
									newLines, newBlobIdent.isFile()?newBlobIdent.path:"b.txt", 
									WhitespaceOption.DEFAULT);
						} else {
							diffBlocks = new ArrayList<>();
						}
					} else {
						diffBlocks = new ArrayList<>();
					}
				} else if (type == ChangeType.DELETE) {
					if (getOldText() != null) {
						List<String> oldLines = getOldText().getLines();
						if (oldLines.size() <= DiffUtils.MAX_DIFF_SIZE) {
							List<String> newLines = new ArrayList<>();
							diffBlocks = DiffUtils.diff(
									oldLines, oldBlobIdent.isFile()?oldBlobIdent.path:"a.txt", 
									newLines, "b.txt", 
									WhitespaceOption.DEFAULT);
						} else {
							diffBlocks = new ArrayList<>();
						}
					} else {
						diffBlocks = new ArrayList<>();
					}
				} else {
					if (getOldText() != null && getNewText() != null) {
						List<String> oldLines = getOldText().getLines();
						List<String> newLines = getNewText().getLines();
						if (oldLines.size() + newLines.size() <= DiffUtils.MAX_DIFF_SIZE) {
							diffBlocks = DiffUtils.diff(
									oldLines, oldBlobIdent.isFile()?newBlobIdent.path:"a.txt", 
									newLines, newBlobIdent.isFile()?newBlobIdent.path:"b.txt", 
									whitespaceOption);
						} else { 
							diffBlocks = new ArrayList<>();
						}
					} else {
						diffBlocks = new ArrayList<>();
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("Error calculating diff of file: " + getPath(), e);
			}
		}
		return diffBlocks;
	}
	
	public int getAdditions() {
		int additions = 0;
		for (DiffBlock<Tokenized> diff: getDiffBlocks()) {
			if (diff.getOperation() == Operation.INSERT)
				additions += diff.getUnits().size();
		}
		return additions;
	}

	public int getDeletions() {
		int deletions = 0;
		for (DiffBlock<Tokenized> diff: getDiffBlocks()) {
			if (diff.getOperation() == Operation.DELETE)
				deletions += diff.getUnits().size();
		}
		return deletions;
	}
	
	public Blob getOldBlob() {
		Preconditions.checkNotNull(oldBlobIdent.path);
		return getProject().getBlob(oldBlobIdent, true);
	}
	
	public Blob getNewBlob() {
		Preconditions.checkNotNull(newBlobIdent.path);
		return getProject().getBlob(newBlobIdent, true);
	}
	
	@Nullable
	public Blob.Text getOldText() {
		return getOldBlob().getText();
	}
	
	@Nullable
	public Blob.Text getNewText() {
		return getNewBlob().getText();
	}
	
	public WhitespaceOption getWhitespaceOption() {
		return whitespaceOption;
	}

	public Collection<String> getPaths() {
		Collection<String> paths = new HashSet<>();
		if (oldBlobIdent.path != null)
			paths.add(oldBlobIdent.path);
		if (newBlobIdent.path != null)
			paths.add(newBlobIdent.path);
		return paths;
	}
	
	public boolean isVisible(DiffPlanarRange range) {
		for (int line = range.getFromRow(); line<=range.getToRow(); line++) {
			if (!DiffUtils.isVisible(getDiffBlocks(), range.isLeftSide(), line, WebConstants.DIFF_CONTEXT_SIZE))
				return false;
		}
		return true;
	}
	
	public boolean isVisible(boolean leftSide, int line) {
		return DiffUtils.isVisible(getDiffBlocks(), leftSide, line, WebConstants.DIFF_CONTEXT_SIZE);
	}
	
	public abstract Project getProject();
	
	@Nullable
	public AnnotationSupport getAnnotationSupport() {
		return null;
	}
	
	public ObjectId getOldCommitId() {
		if (oldBlobIdent.revision.equals(ObjectId.zeroId().name().toString())) 
			return ObjectId.zeroId();
		else 
			return getProject().getRevCommit(oldBlobIdent.revision, true);
	}
	
	public ObjectId getNewCommitId() {
		if (newBlobIdent.revision.equals(ObjectId.zeroId().name().toString())) 
			return ObjectId.zeroId();
		else 
			return getProject().getRevCommit(newBlobIdent.revision, true);
	}

	public Mark getMark(DiffPlanarRange range) {
		Mark mark = new Mark();
		mark.setRange(new PlanarRange(range));
		if (range.isLeftSide()) {
			mark.setCommitHash(getOldCommitId().name());
			mark.setPath(oldBlobIdent.path);
		} else {
			mark.setCommitHash(getNewCommitId().name());
			mark.setPath(newBlobIdent.path);
		}
		return mark;
	}
	
	public static interface AnnotationSupport extends Serializable {
		
		@Nullable 
		DiffPlanarRange getMarkRange();
		
		String getMarkUrl(DiffPlanarRange markRange);
		
		Map<CodeComment, PlanarRange> getOldComments();
		
		Map<CodeComment, PlanarRange> getNewComments();
		
		Collection<CodeProblem> getOldProblems();
		
		Collection<CodeProblem> getNewProblems();
		
		Map<Integer, Integer> getOldCoverages();
		
		Map<Integer, Integer> getNewCoverages();
		
		DiffPlanarRange getCommentRange(CodeComment comment);
		
		@Nullable 
		Pair<CodeComment, DiffPlanarRange> getOpenComment();

		void onOpenComment(AjaxRequestTarget target, CodeComment comment, DiffPlanarRange commentRange);
		
		void onAddComment(AjaxRequestTarget target, DiffPlanarRange commentRange);
		
		Component getCommentContainer();
		
	}
	
}
