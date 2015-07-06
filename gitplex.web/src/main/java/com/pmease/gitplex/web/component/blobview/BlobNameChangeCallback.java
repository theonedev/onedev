package com.pmease.gitplex.web.component.blobview;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface BlobNameChangeCallback {
	void onChange(AjaxRequestTarget target, String blobName);
}
