package io.onedev.server.web.page.project.issues.boards;

import io.onedev.server.model.Iteration;

import javax.annotation.Nullable;

public interface IterationSelection {
	
	@Nullable
	Iteration getIteration();
	
	class Specified implements IterationSelection {
		
		private final Iteration iteration;
		
		public Specified(Iteration iteration) {
			this.iteration = iteration;
		}

		@Override
		public Iteration getIteration() {
			return iteration;
		}
	}
	
	class Unscheduled implements IterationSelection {
		@Override
		public Iteration getIteration() {
			return null;
		}
	}
	
	class All implements IterationSelection {
		@Override
		public Iteration getIteration() {
			return null;
		}
	}
}
