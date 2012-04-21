package com.pmease.commons.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class DependencyHelperTest {

	@Test
	public void shouldHandleTransitiveDependency() {
		Dependency dependency1 = new Dependency() {

			@Override
			public String getId() {
				return "dependency1";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency2", "dependency3");
			}
			
		};

		Dependency dependency2 = new Dependency() {

			@Override
			public String getId() {
				return "dependency2";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency4");
			}
			
		};

		Dependency dependency3 = new Dependency() {

			@Override
			public String getId() {
				return "dependency3";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency4");
			}
			
		};

		Dependency dependency4 = new Dependency() {

			@Override
			public String getId() {
				return "dependency4";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of();
			}
			
		};
		
		Map<String, Dependency> context = ImmutableMap.of(
				"dependency1", dependency1, 
				"dependency2", dependency2, 
				"dependency3", dependency3, 
				"dependency4", dependency4
		);
		
		assertTrue(DependencyHelper.hasTransitiveDependency(context, dependency1, dependency4));
		assertTrue(DependencyHelper.hasTransitiveDependency(context, dependency2, dependency4));
		assertFalse(DependencyHelper.hasTransitiveDependency(context, dependency4, dependency1));
		assertFalse(DependencyHelper.hasTransitiveDependency(context, dependency4, dependency3));
		assertFalse(DependencyHelper.hasTransitiveDependency(context, dependency2, dependency3));
		assertFalse(DependencyHelper.hasTransitiveDependency(context, dependency3, dependency2));
	}

	@Test
	public void shouldReportCircularDependencyWhenHandleTransitive() {
		Dependency dependency1 = new Dependency() {

			@Override
			public String getId() {
				return "dependency1";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency2");
			}
			
		};

		Dependency dependency2 = new Dependency() {

			@Override
			public String getId() {
				return "dependency2";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency1");
			}
			
		};

		Dependency dependency3 = new Dependency() {

			@Override
			public String getId() {
				return "dependency3";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency1");
			}
			
		};

		Map<String, Dependency> context = ImmutableMap.of(
				"dependency1", dependency1, 
				"dependency2", dependency2,
				"dependency3", dependency3
		);

		try {
			DependencyHelper.hasTransitiveDependency(context, dependency1, dependency3);
			fail("Can not detect circular dependency.");
		} catch (DependencyException e) {
			assertTrue(e.getMessage().toLowerCase().contains("circular"));
		}
	}

	@Test
	public void shouldReportMissedDependencyWhenHandleTransitive() {
		Dependency dependency1 = new Dependency() {

			@Override
			public String getId() {
				return "dependency1";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency2");
			}
			
		};

		Dependency dependency2 = new Dependency() {

			@Override
			public String getId() {
				return "dependency2";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency3");
			}
			
		};

		Map<String, Dependency> context = ImmutableMap.of(
				"dependency1", dependency1, 
				"dependency2", dependency2
		);

		try {
			DependencyHelper.hasTransitiveDependency(context, dependency2, dependency1);
			fail("Can not detect missed dependency.");
		} catch (DependencyException e) {
			assertTrue(e.getMessage().toLowerCase().contains("can not find dependency"));
		}
	}
	
	@Test
	public void shouldGetDependentsCorrectly() {
		Dependency dependency1 = new Dependency() {

			@Override
			public String getId() {
				return "dependency1";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency2");
			}
			
		};

		Dependency dependency2 = new Dependency() {

			@Override
			public String getId() {
				return "dependency2";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of();
			}
			
		};
		Dependency dependency3 = new Dependency() {

			@Override
			public String getId() {
				return "dependency3";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency2");
			}
			
		};

		Map<String, Dependency> context = ImmutableMap.of(
				"dependency1", dependency1, 
				"dependency2", dependency2,
				"dependency3", dependency3
		);		
		
		assertTrue(DependencyHelper.getDependents(context, dependency2)
				.equals(ImmutableSet.of(dependency1, dependency3)));
		assertTrue(DependencyHelper.getDependents(context, dependency1).isEmpty());
	}

	@Test
	public void shouldSortDependencies() {
		Dependency dependency1 = new Dependency() {

			@Override
			public String getId() {
				return "dependency1";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency2", "dependency3");
			}
			
		};

		Dependency dependency2 = new Dependency() {

			@Override
			public String getId() {
				return "dependency2";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency3");
			}
			
		};
		Dependency dependency3 = new Dependency() {

			@Override
			public String getId() {
				return "dependency3";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of();
			}
			
		};

		Map<String, Dependency> context = ImmutableMap.of(
				"dependency1", dependency1, 
				"dependency2", dependency2,
				"dependency3", dependency3
		);				
		
		List<Dependency> sorted = DependencyHelper.sortDependencies(context);
		List<Dependency> expected = ImmutableList.of(dependency3, dependency2, dependency1);
		assertTrue(sorted.equals(expected));
	}

	@Test
	public void shouldDetectCircularDependenciesInContext() {
		Dependency dependency1 = new Dependency() {

			@Override
			public String getId() {
				return "dependency1";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency2");
			}
			
		};

		Dependency dependency2 = new Dependency() {

			@Override
			public String getId() {
				return "dependency2";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency3");
			}
			
		};
		Dependency dependency3 = new Dependency() {

			@Override
			public String getId() {
				return "dependency3";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency2");
			}
			
		};

		Map<String, Dependency> context = ImmutableMap.of(
				"dependency1", dependency1, 
				"dependency2", dependency2,
				"dependency3", dependency3
		);				
		
		try {
			DependencyHelper.sortDependencies(context);
			fail("Can not detect circular dependencies");
		} catch (DependencyException e) {
		}
	}

	@Test
	public void shouldDetectMissedDependenciesInContext() {
		Dependency dependency1 = new Dependency() {

			@Override
			public String getId() {
				return "dependency1";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency2");
			}
			
		};

		Dependency dependency2 = new Dependency() {

			@Override
			public String getId() {
				return "dependency2";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency3");
			}
			
		};
		Dependency dependency3 = new Dependency() {

			@Override
			public String getId() {
				return "dependency3";
			}

			@Override
			public Set<String> getDependencyIds() {
				return ImmutableSet.of("dependency4");
			}
			
		};

		Map<String, Dependency> context = ImmutableMap.of(
				"dependency1", dependency1, 
				"dependency2", dependency2,
				"dependency3", dependency3
		);				
		
		try {
			DependencyHelper.sortDependencies(context);
			fail("Can not detect missed dependencies");
		} catch (DependencyException e) {
		}
	}
}
