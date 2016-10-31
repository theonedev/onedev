package com.gitplex.core.commitmessagetransform;

import com.gitplex.core.entity.Depot;
import com.gitplex.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface CommitMessageTransformer {
	
	String transform(Depot depot, String commitMessage);
	
}
