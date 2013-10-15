package com.pmease.gitop.web.common.datatype;

import java.util.Locale;

import org.apache.commons.validator.routines.BigDecimalValidator;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.PeriodFormatter;

import com.google.common.base.Strings;

public class DurationType extends LongType {

  public static final String FMT_WORD = "WORD";
  public static final String FMT_ISO = "ISO";
  public static final String FMT_SHORT_WORD = "SHORT";

  @Override
  public Object fromString(String str, String pattern, Locale locale) {
    if (Strings.isNullOrEmpty(str)) {
      return null;
    }

    if ("NaN".equalsIgnoreCase(str)) {
      return new Long(0);
    }

    if (Strings.isNullOrEmpty(pattern)) {
      try {
        return Long.valueOf(str);
      } catch (NumberFormatException e) {
        Number number = BigDecimalValidator.getInstance().validate(str, locale);
        if (number != null) {
          return number.longValue();
        } else {
          return 0;
        }
      }
    }

    PeriodFormatter formatter = getFormatter(pattern, locale);

    Period period = formatter.parsePeriod(str);
    return Long.valueOf(period.toStandardDuration().getMillis());
  }

  @Override
  public String asString(Object value, String pattern, Locale locale) {
    Long mills = (Long) typeCast(value);
    if (mills == null) {
      return null;
    }

    if (Strings.isNullOrEmpty(pattern)) {
      return mills.toString();
    }

    if (mills == 0) {
      return "0 mil";
    } else if (mills < DateTimeConstants.MILLIS_PER_SECOND) {
      return mills + " mils";
    }

    PeriodFormatter fmt = getFormatter(pattern, locale);
    Duration duration = new Duration(mills);
    return fmt.print(duration.toPeriod(PeriodType.yearMonthDayTime(),
        ISOChronology.getInstanceUTC()));
  }

  protected PeriodFormatter getFormatter(String pattern, Locale locale) {
    PeriodFormatter fmt = null;
    if (FMT_WORD.equalsIgnoreCase(pattern)) {
      fmt = DurationFormatUtils.wordFormatter();
    } else if (FMT_ISO.equalsIgnoreCase(pattern)) {
      fmt = DurationFormatUtils.isoFormatter();
    } else if (FMT_SHORT_WORD.equalsIgnoreCase(pattern)) {
      fmt = DurationFormatUtils.shortWordFormatter();
    } else {
      fmt = DurationFormatUtils.createPeriodFormatter(pattern);
    }
    if (locale != null) {
      fmt = fmt.withLocale(locale);
    }
    return fmt;
  }


}
