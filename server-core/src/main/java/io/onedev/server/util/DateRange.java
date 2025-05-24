package io.onedev.server.util;

import java.io.Serializable;
import java.time.LocalDate;

import io.onedev.commons.utils.StringUtils;

public class DateRange implements Serializable {

	private final LocalDate from;

	private final LocalDate to;

	public DateRange(LocalDate from, LocalDate to) {
		this.from = from;
		this.to = to;
	}
	
	public LocalDate getFrom() {
		return from;
	}
	
	public LocalDate getTo() {
		return to;
	}

    public String toString() {
        return from.format(DateUtils.DATE_FORMATTER) + "~" + to.format(DateUtils.DATE_FORMATTER);
    }

    public static DateRange fromString(String value) {
        if (value.contains("~")) {
            var fromDate = LocalDate.from(DateUtils.DATE_FORMATTER.parse(StringUtils.substringBefore(value, "~")));
            var toDate = LocalDate.from(DateUtils.DATE_FORMATTER.parse(StringUtils.substringAfter(value, "~")));
            return new DateRange(fromDate, toDate);
        } else {
            var date = LocalDate.from(DateUtils.DATE_FORMATTER.parse(value)); 
            return new DateRange(date, date);
        }
    }

}