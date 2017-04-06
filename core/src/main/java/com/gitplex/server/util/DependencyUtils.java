package com.gitplex.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

public class DependencyUtils {
	
	public static <K, T extends DependencyAware<K>> Map<K, Set<K>> getDependentMap(Map<K, T> dependencyMap) {
		Map<K, Set<K>> dependentMap = new HashMap<>();
		for (T depenencyAware: dependencyMap.values()) {
			for (K dependencyId: depenencyAware.getDependencies()) {
				Set<K> dependents = dependentMap.get(dependencyId);
				if (dependents == null) {
					dependents = new HashSet<>();
					dependentMap.put(dependencyId, dependents);
				}
				dependents.add(depenencyAware.getId());
			}
		}
		for (K id: dependencyMap.keySet()) {
			if (!dependentMap.containsKey(id)) {
				dependentMap.put(id, new HashSet<>());
			}
		}
		return dependentMap;
	}
	
	public static <K, T extends DependencyAware<K>> Set<K> getTransitiveDependents(
			Map<K, Set<K>> dependentMap, K dependency) {
		Set<K> transitiveDependents = new HashSet<>();
		
		transitiveDependents.addAll(Preconditions.checkNotNull(dependentMap.get(dependency)));
		while (true) {
			Set<K> newTransitiveDependents = new HashSet<>(transitiveDependents);
			for (K dependent: transitiveDependents) {
				newTransitiveDependents.addAll(Preconditions.checkNotNull(dependentMap.get(dependent)));
			}
			if (!transitiveDependents.equals(newTransitiveDependents)) {
				transitiveDependents = newTransitiveDependents;
			} else {
				break;
			}
		}
		return transitiveDependents;
	}
	
	/**
	 * Sort dependencies in context. If "dependency1" depends on "dependency2", "dependency2" will 
	 * appear before "dependency1" in the result list. 
	 * @param dependencyMap
	 * @return 
	 * 			Sorted dependency list with elements without dependencies appear before elements
	 * 			with dependencies.
	 * @throws
	 * 			{@link DependencyException} if circular dependencies are found in the context, or 
	 * 			if some dependency object refers to non-existent dependencies in the context.
	 */
	public static <K, T extends DependencyAware<K>> List<K> sortDependencies(Map<K, T> dependencyMap) {
		List<K> sorted = new ArrayList<>();
		
		Map<K, Collection<K>> dependencyIds = new LinkedHashMap<K, Collection<K>>();
		for (Map.Entry<K, T> entry: dependencyMap.entrySet())
			dependencyIds.put(entry.getKey(), new HashSet<K>(entry.getValue().getDependencies()));
		
		while (!dependencyIds.isEmpty()) {
			Set<K> leafs = new LinkedHashSet<>();
			for (K id: dependencyIds.keySet()) {
				if (dependencyIds.get(id).isEmpty()) {
					leafs.add(id);
				}
			}
			if (leafs.isEmpty()) {
				Set<K> unprocessed = new HashSet<>();
				for (K id: dependencyIds.keySet())
					unprocessed.add(id);
				throw new RuntimeException("Unable to process dependencies: " + unprocessed 
						+ ". This is either because circular exists in these dependencies, or because " 
						+ "some of these dependencies depends on non-existent dependencies.");
			} else {
				sorted.addAll(leafs);
			}
			dependencyIds.keySet().removeAll(leafs);
			for (Collection<K> value: dependencyIds.values())
				value.removeAll(leafs);
		}		
		
		return sorted;
	}
}
