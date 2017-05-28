package com.gitplex.server.web.component.markdown;

import java.io.Serializable;

import com.gitplex.server.model.Project;

public interface BlobReferenceSupport extends Serializable {
	
	Project getProject();
	
	String getRevision();
	
}