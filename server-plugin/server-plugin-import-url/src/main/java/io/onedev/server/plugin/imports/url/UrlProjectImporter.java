package io.onedev.server.plugin.imports.url;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.web.util.ImportStep;

public class UrlProjectImporter implements ProjectImporter {

	private static final long serialVersionUID = 1L;
	
	private final ImportStep<ImportServer> serverStep = new ImportStep<ImportServer>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Specify URL to import from";
		}

		@Override
		protected ImportServer newSetting() {
			return new ImportServer();
		}
		
	};
	
	@Override
	public String getName() {
		return UrlModule.NAME;
	}
	
	@Override
	public String doImport(boolean dryRun, TaskLogger logger) {
		return serverStep.getSetting().importProject(dryRun, logger);
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep);
	}

}