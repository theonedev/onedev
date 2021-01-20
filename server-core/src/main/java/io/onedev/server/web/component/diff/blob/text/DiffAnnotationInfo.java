package io.onedev.server.web.component.diff.blob.text;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.CodeComment;
import io.onedev.server.web.util.AnnotationInfo;
import io.onedev.server.web.util.CodeCommentInfo;
import io.onedev.server.web.util.DiffPlanarRange;

public class DiffAnnotationInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private final AnnotationInfo oldAnnotations;
	
	private final AnnotationInfo newAnnotations;
	
	public DiffAnnotationInfo(AnnotationInfo oldAnnotations, AnnotationInfo newAnnotations) {
		this.oldAnnotations = oldAnnotations;
		this.newAnnotations = newAnnotations;
	}
	
	public AnnotationInfo getOldAnnotations() {
		return oldAnnotations;
	}

	public AnnotationInfo getNewAnnotations() {
		return newAnnotations;
	}

	@Nullable
	public DiffPlanarRange getMarkRange(CodeComment comment) {
		for (List<CodeCommentInfo> eachList: oldAnnotations.getComments().values()) {
			for (CodeCommentInfo eachInfo: eachList) {
				if (eachInfo.getId() == comment.getId()) {
					return new DiffPlanarRange(true, 
							eachInfo.getRange().getFromRow(), eachInfo.getRange().getFromColumn(), 
							eachInfo.getRange().getToRow(), eachInfo.getRange().getToColumn());
				}
			}
		}
		for (List<CodeCommentInfo> eachList: newAnnotations.getComments().values()) {
			for (CodeCommentInfo eachInfo: eachList) {
				if (eachInfo.getId() == comment.getId()) {
					return new DiffPlanarRange(false, 
							eachInfo.getRange().getFromRow(), eachInfo.getRange().getFromColumn(), 
							eachInfo.getRange().getToRow(), eachInfo.getRange().getToColumn());
				}
			}
		}
		return null;
	}
	
}
