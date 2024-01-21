package io.onedev.server.entitymanager;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.search.buildmetric.BuildMetricQuery;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public interface BuildMetricManager {
	
	@Nullable
	<T extends AbstractEntity> T find(Class<T> metricClass, Build build, String reportName);
	
	<T extends AbstractEntity> Map<Integer, T> queryStats(Project project, Class<T> metricClass, BuildMetricQuery query);
	
	Map<String, Collection<String>> getAccessibleReportNames(Project project, Class<?> metricClass);
	
}
