package io.onedev.server.plugin.github;

import io.onedev.server.web.page.project.imports.ProjectImportPanel;
import io.onedev.server.web.page.project.imports.ProjectImporter;

public class GitHubImporter implements ProjectImporter {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "GitHub";

	private final GitHubSetting setting;
	
	public GitHubImporter(GitHubSetting setting) {
		this.setting = setting;
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public ProjectImportPanel render(String componentId) {
		return new GitHubImportPanel(componentId, setting);
	}
	
}