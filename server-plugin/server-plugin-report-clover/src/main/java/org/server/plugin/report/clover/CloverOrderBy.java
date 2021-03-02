package org.server.plugin.report.clover;

import io.onedev.commons.utils.StringUtils;

enum CloverOrderBy {
	DEFAULT {

		@Override
		public int compare(CoverageInfo obj1, CoverageInfo obj2) {
			return 0;
		}

	}, 
	LEAST_METHOD_COVERAGE {

		@Override
		public int compare(CoverageInfo obj1, CoverageInfo obj2) {
			return obj1.getMethodCoverage().getPercent() - obj2.getMethodCoverage().getPercent();
		}
		
	}, 
	MOST_METHOD_COVERAGE {

		@Override
		public int compare(CoverageInfo obj1, CoverageInfo obj2) {
			return LEAST_METHOD_COVERAGE.compare(obj2, obj1);
		}
		
	}, 
	LEAST_BRANCH_COVERAGE {

		@Override
		public int compare(CoverageInfo obj1, CoverageInfo obj2) {
			return obj1.getBranchCoverage().getPercent() - obj2.getBranchCoverage().getPercent();
		}
		
	}, 
	MOST_BRANCH_COVERAGE {

		@Override
		public int compare(CoverageInfo obj1, CoverageInfo obj2) {
			return LEAST_BRANCH_COVERAGE.compare(obj2, obj1);
		}
		
	}, 
	LEAST_STATEMENT_COVERAGE {

		@Override
		public int compare(CoverageInfo obj1, CoverageInfo obj2) {
			return obj1.getStatementCoverage().getPercent() - obj2.getStatementCoverage().getPercent();
		}
		
	}, 
	MOST_STATEMENT_COVERAGE {

		@Override
		public int compare(CoverageInfo obj1, CoverageInfo obj2) {
			return LEAST_STATEMENT_COVERAGE.compare(obj2, obj1);
		}
		
	}, 
	LEAST_LINE_COVERAGE {

		@Override
		public int compare(CoverageInfo obj1, CoverageInfo obj2) {
			return obj1.getLineCoverage().getPercent() - obj2.getLineCoverage().getPercent();
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