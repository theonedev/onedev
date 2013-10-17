package com.pmease.gitop.web.common.datatype;

public class TypeCastException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public TypeCastException(String msg) {
    super(msg);
  }
  
  public TypeCastException(DataType dataType, Object value) {
    this("Cannot cast object [" + value + "] to data type [" + dataType + "]");
  }
}
