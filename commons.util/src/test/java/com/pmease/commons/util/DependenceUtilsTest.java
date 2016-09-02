package com.pmease.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.pmease.commons.util.DependencyAware;
import com.pmease.commons.util.DependencyUtils;

public class DependenceUtilsTest {

	@Test
	public void shouldDetectCircularDependenciesInContext() {
		DependencyAware<String> dependency1 = new DependencyAware<String>() {

			@Override
			public String getId() {
				return "dependency1";
			}

			@Override
			public Set<String> getDependencies() {
				return ImmutableSet.of("dependency2");
			}
			
		};

		DependencyAware<String> dependency2 = new DependencyAware<String>() {

			@Override
			public String getId() {
				return "dependency2";
			}

			@Override
			public Set<String> getDependencies() {
				return ImmutableSet.of("dependency3");
			}
			
		};
		DependencyAware<String> dependency3 = new DependencyAware<String>() {

			@Override
			public String getId() {
				return "dependency3";
			}

			@Override
			public Set<String> getDependencies() {
				return ImmutableSet.of("dependency2");
			}
			
		};

		Map<String, DependencyAware<String>> context = ImmutableMap.of(
				"dependency1", dependency1, 
				"dependency2", dependency2,
				"dependency3", dependency3
		);				
		
		try {
			DependencyUtils.sortDependencies(context);
			fail("Can not detect circular dependencies");
		} catch (Exception e) {
		}
	}

	@Test
	public void shouldGetTransitiveDependents() {
		DependencyAware<String> dependency1 = new DependencyAware<String>() {

			@Override
			public String getId() {
				return "dependency1";
			}

			@Override
			public Set<String> getDependencies() {
				return ImmutableSet.of("dependency2", "dependency3");
			}
			
		};

		DependencyAware<String> dependency2 = new DependencyAware<String>() {

			@Override
			public String getId() {
				return "dependency2";
			}

			@Override
			public Set<String> getDependencies() {
				return ImmutableSet.of("dependency3");
			}
			
		};
		DependencyAware<String> dependency3 = new DependencyAware<String>() {

			@Override
			public String getId() {
				return "dependency3";
			}

			@Override
			public Set<String> getDependencies() {
				return new HashSet<>();
			}
			
		};

		Map<String, DependencyAware<String>> context = ImmutableMap.of(
				"dependency1", dependency1, 
				"dependency2", dependency2,
				"dependency3", dependency3
		);				
		
		assertEquals(Sets.newHashSet("dependency1", "dependency2"),
				DependencyUtils.getTransitiveDependents(DependencyUtils.getDependentMap(context), "dependency3"));
	}

	@Test
	public void shouldGetTransitiveDependentsWithCircular() {
		DependencyAware<String> dependency1 = new DependencyAware<String>() {

			@Override
			public String getId() {
				return "dependency1";
			}

			@Override
			public Set<String> getDependencies() {
				return ImmutableSet.of("dependency2");
			}
			
		};

		DependencyAware<String> dependency2 = new DependencyAware<String>() {

			@Override
			public String getId() {
				return "dependency2";
			}

			@Override
			public Set<String> getDependencies() {
				return ImmutableSet.of("dependency3");
			}
			
		};
		DependencyAware<String> dependency3 = new DependencyAware<String>() {

			@Override
			public String getId() {
				return "dependency3";
			}

			@Override
			public Set<String> getDependencies() {
				return ImmutableSet.of("dependency2");
			}
			
		};

		Map<String, DependencyAware<String>> context = ImmutableMap.of(
				"dependency1", dependency1, 
				"dependency2", dependency2,
				"dependency3", dependency3
		);				
		
		assertEquals(Sets.newHashSet("dependency1", "dependency2", "dependency3"),
				DependencyUtils.getTransitiveDependents(DependencyUtils.getDependentMap(context), "dependency3"));
	}
	
	@Test
	public void shouldDetectMissedDependenciesInContext() {
		DependencyAware<String> dependency1 = new DependencyAware<String>() {

			@Override
			public String getId() {
				return "dependency1";
			}

			@Override
			public Set<String> getDependencies() {
				return ImmutableSet.of("dependency2");
			}
			
		};

		DependencyAware<String> dependency2 = new DependencyAware<String>() {

			@Override
			public String getId() {
				return "dependency2";
			}

			@Override
			public Set<String> getDependencies() {
				return ImmutableSet.of("dependency3");
			}
			
		};
		DependencyAware<String> dependency3 = new DependencyAware<String>() {

			@Override
			public String getId() {
				return "dependency3";
			}

			@Override
			public Set<String> getDependencies() {
				return ImmutableSet.of("dependency4");
			}
			
		};

		Map<String, DependencyAware<String>> context = ImmutableMap.of(
				"dependency1", dependency1, 
				"dependency2", dependency2,
				"dependency3", dependency3
		);				
		
		try {
			DependencyUtils.sortDependencies(context);
			fail("Can not detect missed dependencies");
		} catch (Exception e) {
		}
	}
}
