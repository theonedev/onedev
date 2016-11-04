package com.gitplex.server.core.commitmessagetransform;

import com.gitplex.commons.loader.ExtensionPoint;
import com.gitplex.server.core.entity.Depot;

@ExtensionPoint
public interface CommitMessageTransformer {
	
	String transform(Depot depot, String commitMessage);
	
}
