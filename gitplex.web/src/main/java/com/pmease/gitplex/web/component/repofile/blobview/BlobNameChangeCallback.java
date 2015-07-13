package com.pmease.gitplex.web.component.repofile.blobview;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface BlobNameChangeCallback extends Serializable {
	
	void onChange(AjaxRequestTarget target, String blobName);
	
}
