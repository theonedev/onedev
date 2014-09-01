package com.pmease.gitplex.core.comment;

import java.util.List;

import com.pmease.commons.git.Change;

public interface ChangeLoader {
	List<Change> loadChanges(String fromRev, String toRev);
}
