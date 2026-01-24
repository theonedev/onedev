package io.onedev.server.plugin.imports.gitea;

import io.onedev.server.annotation.Editable;

@Editable
public class ChildrenImportRepositories extends ImportRepositories {

	@Override
	public String getParentOneDevProject() {
		return super.getParentOneDevProject();
	}

}
