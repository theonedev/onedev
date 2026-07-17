package io.onedev.server.web.page.project.stats.code;

import static io.onedev.server.web.translation.Translation._T;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

enum ContributionPeriod {
	
	SIX_MONTHS, ONE_YEAR, FIVE_YEARS, ALL;
	
	String getDisplayName() {
		switch (this) {
			case SIX_MONTHS:
				return _T("6 Months");
			case ONE_YEAR:
				return _T("1 Year");
			case FIVE_YEARS:
				return _T("5 Years");
			case ALL:
				return _T("All");
			default:
				throw new RuntimeException("Unexpected period: " + this);
		}
	}
	
	@Nullable
	Integer getFromDay() {
		var today = LocalDate.now();
		switch (this) {
			case SIX_MONTHS:
				return (int) today.minusMonths(6).toEpochDay();
			case ONE_YEAR:
				return (int) today.minusYears(1).toEpochDay();
			case FIVE_YEARS:
				return (int) today.minusYears(5).toEpochDay();
			case ALL:
				return null;
			default:
				throw new RuntimeException("Unexpected period: " + this);
		}
	}
	
}
