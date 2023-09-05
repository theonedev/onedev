package io.onedev.server.plugin.report.coverage;

import io.onedev.commons.utils.StringUtils;

enum CoverageOrderBy {
	DEFAULT {

		@Override
		public int compare(CoverageInfo obj1, CoverageInfo obj2) {
			return 0;
		}

	}, 
	LEAST_BRANCH_COVERAGE {

		@Override
		public int compare(CoverageInfo obj1, CoverageInfo obj2) {
			return obj1.getBranchCoverage() - obj2.getBranchCoverage();
		}
		
	}, 
	MOST_BRANCH_COVERAGE {

		@Override
		public int compare(CoverageInfo obj1, CoverageInfo obj2) {
			return LEAST_BRANCH_COVERAGE.compare(obj2, obj1);
		}
		
	}, 
	LEAST_LINE_COVERAGE {

		@Override
		public int compare(CoverageInfo obj1, CoverageInfo obj2) {
			return obj1.getLineCoverage() - obj2.getLineCoverage();
		}
		
	}, 
	MOST_LINE_COVERAGE {

		@Override
		public int compare(CoverageInfo obj1, CoverageInfo obj2) {
			return LEAST_LINE_COVERAGE.compare(obj2, obj1);
		}
		
	}; 
	
	public String getDisplayName() {
		return StringUtils.capitalize(name().replace('_', ' ').toLowerCase());
	}

	public abstract int compare(CoverageInfo obj1, CoverageInfo obj2);
	
}