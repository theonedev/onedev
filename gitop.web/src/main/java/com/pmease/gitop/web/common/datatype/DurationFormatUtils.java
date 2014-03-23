package com.pmease.gitop.web.common.datatype;

import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class DurationFormatUtils {
  // public static final String ISO8601 = "yyyy-MM-dd'T'HH:mm:ssZ";

  public static PeriodFormatter createPeriodFormatter(String pattern) {
    PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
    parsePatternTo(builder, pattern);
    return builder.toFormatter();
  }

  private static void parsePatternTo(PeriodFormatterBuilder builder, String pattern) {
    int length = pattern.length();
    int[] indexRef = new int[1];

    builder.printZeroAlways();

    for (int i = 0; i < length; i++) {
      indexRef[0] = i;
      String token = parseToken(pattern, indexRef);
      i = indexRef[0];

      int tokenLen = token.length();
      if (tokenLen == 0) {
        break;
      }
      char c = token.charAt(0);

      switch (c) {
        case 'y': // year (number)
          builder.appendYears();
          break;
        case 'd': // days
          builder.minimumPrintedDigits(tokenLen);
          builder.appendDays();
          break;
        case 'H': // hours
          builder.minimumPrintedDigits(tokenLen);
          builder.appendHours();
          break;
        case 'm': // minutes
          builder.minimumPrintedDigits(tokenLen);
          builder.appendMinutes();
          break;
        case 's': // seconds
          builder.minimumPrintedDigits(tokenLen);
          builder.appendSeconds();
          break;
        case 'S': // milliseconds
          builder.minimumPrintedDigits(tokenLen);
          builder.appendMillis();
          break;
        case '\'': // literal text
          String sub = token.substring(1);
          builder.appendLiteral(new String(sub));
          break;
        default:
          throw new IllegalArgumentException("Illegal pattern component: " + token);
      }
    }
  }

  /**
   * Parses an individual token.
   * 
   * @param pattern the pattern string
   * @param indexRef a single element array, where the input is the start location and the output is
   *        the location after parsing the token
   * @return the parsed token
   */
  private static String parseToken(String pattern, int[] indexRef) {
    StringBuffer buf = new StringBuffer();

    int i = indexRef[0];
    int length = pattern.length();

    char c = pattern.charAt(i);
    if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
      // Scan a run of the same character, which indicates a time
      // pattern.
      buf.append(c);

      while (i + 1 < length) {
        char peek = pattern.charAt(i + 1);
        if (peek == c) {
          buf.append(c);
          i++;
        } else {
          break;
        }
      }
    } else {
      // This will identify token as text.
      buf.append('\'');

      boolean inLiteral = false;

      for (; i < length; i++) {
        c = pattern.charAt(i);

        if (c == '\'') {
          if (i + 1 < length && pattern.charAt(i + 1) == '\'') {
            // '' is treated as escaped '
            i++;
            buf.append(c);
          } else {
            inLiteral = !inLiteral;
          }
        } else if (!inLiteral && (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
          i--;
          break;
        } else {
          buf.append(c);
        }
      }
    }

    indexRef[0] = i;
    return buf.toString();
  }

  public static PeriodFormatter isoFormatter() {
    return ISOPeriodFormat.standard();
  }

  static PeriodFormatter wordFormatter = new PeriodFormatterBuilder()
  		.appendYears().appendSuffix(" year", " years").appendSeparator(" ")
  		.appendMonths().appendSuffix(" month", " months").appendSeparator(" ")
  		.appendDays().appendSuffix(" day", " days").appendSeparator(" ")
  		.appendHours().appendSuffix(" hour", " hours").appendSeparator(" ")
  		.appendMinutes().appendSuffix(" minute", " minutes").appendSeparator(" ")
  		.appendSeconds().appendSuffix(" second", " seconds").appendSeparator(" ")
  		.appendMillis3Digit()
  		.toFormatter();

  public static PeriodFormatter wordFormatter() {
    return wordFormatter;
  }

  static PeriodFormatter shortWordFormatter = new PeriodFormatterBuilder()
  		.appendDays().appendSuffix("d").appendSeparator(", ")
  		.appendHours().appendSuffix("h").appendSeparator(":")
  		.appendMinutes().appendSuffix("m").appendSeparator(":")
  		.appendSeconds().appendSuffix("s").appendSeparator(", ")
  		.appendMillis()
  		.toFormatter();

  public static PeriodFormatter shortWordFormatter() {
    return shortWordFormatter;
  }

  static PeriodFormatter timeFormatter = new PeriodFormatterBuilder().printZeroAlways()
      .appendHours().appendSuffix(":").appendMinutes().appendSuffix(":").appendSeconds()
      .toFormatter();

  public static PeriodFormatter getTimeFormatter() {
    return timeFormatter;
  }
}
