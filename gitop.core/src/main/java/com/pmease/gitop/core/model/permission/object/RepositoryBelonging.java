package com.pmease.gitop.core.model.permission.object;

import com.pmease.gitop.core.model.Repository;

public interface RepositoryBelonging extends ProtectedObject {
	Repository getOwner();
}
