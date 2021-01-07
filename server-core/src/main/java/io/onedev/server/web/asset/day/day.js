onedev.server.day = {
	format: function(day) {
		return day.year%100 + "-" + (day.monthOfYear + 1) + "-" + day.dayOfMonth;
	},
	compare: function(day1, day2) {
		if (day1.year < day2.year)
			return -1;
		else if (day1.year > day2.year)
			return 1;
		else if (day1.monthOfYear < day2.monthOfYear)
			return -1;
		else if (day1.monthOfYear > day2.monthOfYear)
			return 1;
		else
			return day1.dayOfMonth - day2.dayOfMonth;
	},
	fromDate: function(date) {
		return {
			year: date.getFullYear(),
			monthOfYear: date.getMonth(),
			dayOfMonth: date.getDate()
		}
	},
	toDate: function(day) {
		return new Date(day.year, day.monthOfYear, day.dayOfMonth, 0, 0, 0, 0);
	},
	fromValue: function(dayValue) {
		return {
			year: dayValue>>>16, 
			monthOfYear: ((dayValue&0x0000ffff)>>>8),
			dayOfMonth: dayValue&0x000000ff
		}
	},
	toValue: function(day) {
		return (day.year<<16) | (day.monthOfYear<<8) | day.dayOfMonth;
	},
	plus(day, numberOfDays) {
		return onedev.server.day.fromDate(onedev.server.day.toDate({
			year: day.year,
			monthOfYear: day.monthOfYear,
			dayOfMonth: day.dayOfMonth+numberOfDays
		}));
	}
}