package io.onedev.server.web.page.project.issues.boards;

import io.onedev.server.model.Milestone;

import javax.annotation.Nullable;

public interface MilestoneSelection {
	
	@Nullable
	Milestone getMilestone();
	
	class Specified implements MilestoneSelection {
		
		private final Milestone milestone;
		
		public Specified(Milestone milestone) {
			this.milestone = milestone;
		}

		@Override
		public Milestone getMilestone() {
			return milestone;
		}
	}
	
	class Unscheduled implements MilestoneSelection {
		@Override
		public Milestone getMilestone() {
			return null;
		}
	}
	
	class All implements MilestoneSelection {
		@Override
		public Milestone getMilestone() {
			return null;
		}
	}
}
