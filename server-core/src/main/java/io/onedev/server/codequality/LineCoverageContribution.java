package io.onedev.server.codequality;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.onedev.commons.loader.ExtensionPoint;
import io.onedev.server.model.Build;

@ExtensionPoint
public interface LineCoverageContribution {

	Map<Integer, CoverageStatus> getLineCoverages(Build build, String blobPath, @Nullable String reportName); 
	
}
