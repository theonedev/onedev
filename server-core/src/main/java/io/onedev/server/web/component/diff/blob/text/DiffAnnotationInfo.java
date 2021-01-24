package io.onedev.server.web.component.diff.blob.text;

import java.io.Serializable;

import io.onedev.server.web.util.AnnotationInfo;

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
	
}
