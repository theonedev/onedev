package com.pmease.gitop.web.common.soy.api;

public class SoyException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public SoyException(String message) {
    super(message);
  }
  
  public SoyException(String message, Throwable e) {
    super(message, e);
  }
  
  public SoyException(Throwable e) {
    super(e);
  }
}
