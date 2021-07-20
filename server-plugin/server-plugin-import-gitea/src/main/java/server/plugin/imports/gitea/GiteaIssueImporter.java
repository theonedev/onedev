package server.plugin.imports.gitea;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;

import io.onedev.server.imports.IssueImporter2;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.util.WicketUtils;

public class GiteaIssueImporter extends IssueImporter2<ImportServer, IssueImportSource, IssueImportOption> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return ImportUtils.NAME;
	}

	@Override
	public IssueImportSource getWhat(ImportServer where, SimpleLogger logger) {
		WicketUtils.getPage().setMetaData(ImportServer.META_DATA_KEY, where);
		return new IssueImportSource();
	}

	@Override
	public IssueImportOption getHow(ImportServer where, IssueImportSource what, SimpleLogger logger) {
		return ImportUtils.buildIssueImportOption(where, Lists.newArrayList(what.getRepository()), logger);
	}

	@Override
	public String doImport(ImportServer where, IssueImportSource what, IssueImportOption how, 
			Project project, boolean dryRun, SimpleLogger logger) {
		logger.log("Importing issues from repository " + what.getRepository() + "...");
		Map<String, Optional<User>> users = new HashMap<>();
		return ImportUtils.importIssues(where, what.getRepository(), project, false, how, users, dryRun, logger)
				.toHtml("Issues imported successfully");
	}

}
