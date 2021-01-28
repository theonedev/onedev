package io.onedev.server.code;

import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.model.Build;

@ExtensionPoint
public interface LineCoverageContribution {

	Map<Integer, Integer> getLineCoverages(Build build, String blobPath, @Nullable String reportName); 
	
}
