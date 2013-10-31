package com.pmease.gitop.web.util;

/**
 * Three elements of the same type
 * if only java had tuples...
 *
 * @param <T>
 */
public class UniTriple<T> extends Triple<T,T,T> {
  public UniTriple(T t, T t1, T t2) {
    super(t, t1, t2);
  }
}
