package io.onedev.server.code;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.model.Build;

@ExtensionPoint
public interface LineCoverageContribution {

	List<LineCoverage> getLineCoverages(Build build, String blobPath, @Nullable String reportName); 
	
}
