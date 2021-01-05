package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.Map;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.search.buildmetric.BuildMetricQuery;

public interface BuildMetricManager {

	<T extends AbstractEntity> Map<Integer, T> queryStats(Project project, Class<T> metricClass, BuildMetricQuery query);
	
	Map<String, Collection<String>> getAccessibleReportNames(Project project, Class<?> metricClass);
	
}
