package io.onedev.server.web.component.diff.blob;

import org.apache.wicket.ajax.AjaxRequestTarget;

import java.io.Serializable;

public interface BlobDiffReviewSupport extends Serializable {
	
	boolean isReviewed();
	
	void setReviewed(AjaxRequestTarget target, boolean reviewed);
	
}
