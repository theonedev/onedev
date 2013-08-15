package com.pmease.commons.util.dependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

public class DependencyHelper {
	
	/**
	 * Whether or not specified "dependent" object depends on specified 
	 * "dependency" object transitively in specified "dependencyContext".
	 * @param dependencyContext
	 * 			Map of dependency id to dependency object.
	 * @param dependent
	 * 			The object to check the transitive dependency against. 
	 * @param dependency
	 * 			The object to define the transitive dependency. This should not be the same 
	 * 			as the dependent object.
	 * @return
	 * 			true if "dependent" has a transitive "dependency", false otherwise. 
	 */
	public static <T extends Dependency> boolean hasTransitiveDependency(Map<String, T> dependencyContext, 
			T dependent, T dependency) {
		Preconditions.checkArgument(!dependent.getId().equals(dependency.getId()), 
				"Dependent and dependency object should not be the same.");
		
		return hasTransitiveDependency(dependencyContext, dependent, dependency, 
				ImmutableSet.of(dependent.getId()));
	}
	
	private static <T extends Dependency> boolean hasTransitiveDependency(Map<String, T> dependencyContext, 
			T dependent, T dependency, Set<String> visited) {
		for (String dependencyId: dependent.getDependencyIds()) {
			if (dependencyId.equals(dependency.getId()))
				return true;

			if (visited.contains(dependencyId))
				throw new DependencyException("Circular dependency found: " + visited);
			
			dependent = dependencyContext.get(dependencyId);
			if (dependent == null)
				throw new DependencyException("Can not find dependency '" + dependencyId + "'.");
			
			visited = new HashSet<String>(visited);
			visited.add(dependencyId);
			if (hasTransitiveDependency(dependencyContext, dependent, dependency, visited))
				return true;
		}
		return false;
	}
	
	/**
	 * Get all objects directly depends on specified dependency in specified context.
	 * @param dependencyContext
	 * 			A map of dependency identifier to dependency
	 * @param dependency
	 * 			The dependency to look for dependents
	 * @return
	 * 			A set of objects who has a direct dependency to passed "dependency" param in specified 
	 * 			"dependencyContext". An empty set will be returned if no objects depends on specified dependency.
	 */
	public static <T extends Dependency> Set<T> getDependents(Map<String, T> dependencyContext, T dependency) {
		Set<T> dependents = new HashSet<T>();
		for (T each: dependencyContext.values()) {
			if (each.getDependencyIds().contains(dependency.getId()))
				dependents.add(each);
		}
		return dependents;
	}

	/**
	 * Sort dependencies in context. If "dependency1" depends on "dependency2", "dependency2" will 
	 * appear before "dependency1" in the result list. 
	 * @param dependencyContext
	 * @return 
	 * 			Sorted dependency list with elements without dependencies appear before elements
	 * 			with dependencies.
	 * @throws
	 * 			{@link DependencyException} if circular dependencies are found in the context, or 
	 * 			if some dependency object refers to non-existent dependencies in the context.
	 */
	public static <T extends Dependency> List<T> sortDependencies(Map<String, T> dependencyContext) {
		List<T> sorted = new ArrayList<T>();
		
		Map<String, Collection<String>> dependencyIds = new HashMap<String, Collection<String>>();
		for (Map.Entry<String, T> entry: dependencyContext.entrySet())
			dependencyIds.put(entry.getKey(), new HashSet<String>(entry.getValue().getDependencyIds()));
		
		while (!dependencyIds.isEmpty()) {
			Set<String> leafs = new HashSet<String>();
			for (String key: dependencyIds.keySet()) {
				if (dependencyIds.get(key).isEmpty()) {
					leafs.add(key);
					T dependency = dependencyContext.get(key);
					Preconditions.checkNotNull(dependency);
					sorted.add(dependency);
				}
			}
			if (leafs.isEmpty()) {
				Set<String> unprocessed = new HashSet<String>();
				for (String id: dependencyIds.keySet())
					unprocessed.add(id);
				throw new DependencyException("Unable to process dependencies: " + unprocessed 
						+ ". This is either because circular exists in these dependencies, or because " 
						+ "some of these dependencies depends on non-existent dependencies.");
			}
			for (String key: leafs) 
				dependencyIds.remove(key);
			for (Collection<String> value: dependencyIds.values())
				value.removeAll(leafs);
		}		
		
		return sorted;
	}
}
