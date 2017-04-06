package com.gitplex.server.web.component.markdown;

import java.io.Serializable;

import com.gitplex.server.model.Depot;

public interface BlobReferenceSupport extends Serializable {
	
	Depot getDepot();
	
	String getRevision();
	
}