package io.onedev.server.plugin.report.coverage;

enum CoverageOrderBy {
	DEFAULT {

		@Override
		public int compare(Coverage obj1, Coverage obj2) {
			return 0;
		}

	}, 
	LEAST_BRANCH_COVERAGE {

		@Override
		public int compare(Coverage obj1, Coverage obj2) {
			return obj1.getBranchPercentage() - obj2.getBranchPercentage();
		}
		
	}, 
	MOST_BRANCH_COVERAGE {

		@Override
		public int compare(Coverage obj1, Coverage obj2) {
			return LEAST_BRANCH_COVERAGE.compare(obj2, obj1);
		}
		
	}, 
	LEAST_LINE_COVERAGE {

		@Override
		public int compare(Coverage obj1, Coverage obj2) {
			return obj1.getLinePercentage() - obj2.getLinePercentage();
		}
		
	}, 
	MOST_LINE_COVERAGE {

		@Override
		public int compare(Coverage obj1, Coverage obj2) {
			return LEAST_LINE_COVERAGE.compare(obj2, obj1);
		}
		
	}; 
	
	public abstract int compare(Coverage obj1, Coverage obj2);
	
}