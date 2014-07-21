package com.pmease.gitplex.web.common.datatype;

public class DataTypes {
  
  private static enum NULLE {
  }
  
  public static final DataType BOOLEAN = new BooleanType();
  public static final DataType BYTE = new ByteType();
  public static final DataType SHORT = new ShortType();
  public static final DataType INTEGER = new IntegerType();
  public static final DataType LONG = new LongType();
  public static final DataType FLOAT = new FloatType();
  public static final DataType DOUBLE = new DoubleType();
  public static final DataType PERCENT = new PercentType();
  public static final DataType DATE = new DateType();
  public static final DataType ENUM = new EnumType(NULLE.class);
  public static final DataType STRING = new StringType();
  
  static final DataType[] PRIMITIVES = new DataType[] {
    BOOLEAN, BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, PERCENT, ENUM, STRING                                                   
  };
  
  public static DataType getType(Class<?> clazz) {
    for (DataType each : PRIMITIVES) {
      if (each.getReturnClass().isAssignableFrom(clazz)) {
        return each;
      }
    }
    
    throw new UnsupportedOperationException("Unable to find the data type for class [" + clazz + "]");
  }
}
