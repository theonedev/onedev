package com.pmease.gitop.web.common.datatype;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.validator.routines.DateValidator;
import org.joda.time.DateTime;
import org.joda.time.ReadableDateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.base.Strings;

public class DateType extends AbstractDataType {

  @Override
  public String asString(Object from, String pattern, Locale locale) {
    Date date = (Date) typeCast(from);
    
    if (date == null) {
      return null;
    }
    
    if (Strings.isNullOrEmpty(pattern)) {
      return ISODateTimeFormat.dateTime().withZoneUTC().print(new DateTime(date));
    }
    
    return DateValidator.getInstance().format(date, pattern, locale);
  }

  @Override
  public Object fromString(String value, String pattern, Locale locale) {
    if (Strings.isNullOrEmpty(pattern)) {
      return ISODateTimeFormat.dateTime().withZoneUTC().parseDateTime(value).toDate();
    }
    
    return DateValidator.getInstance().validate(value, pattern, locale);
  }

  @Override
  public Class<?> getReturnClass() {
    return Date.class;
  }

  @Override
  public Object typeCast(Object from) {
    if (from == null || (from instanceof Date)) {
      return from;
    }

    if (from instanceof Long) {
      return new Date((Long) from);
    }

    if (from instanceof Calendar) {
      return new Date(((Calendar) from).getTimeInMillis());
    }

    if (from instanceof java.sql.Timestamp) {
      return new Date(((java.sql.Timestamp) from).getTime());
    }

    if (from instanceof ReadableDateTime) {
      return ((ReadableDateTime) from).toDateTime().toDate();
    }
    
    if (from instanceof String) {
      DateTime dt = ISODateTimeFormat.dateTime().parseDateTime((String) from);
      if (dt != null) {
        return dt.toDate();
      }
    }

    throw new TypeCastException(this, from);
  }

  @Override
  public String getTypeName() {
    return "DATE";
  }

}
