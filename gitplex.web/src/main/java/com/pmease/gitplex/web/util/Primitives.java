package com.pmease.gitplex.web.util;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Utilities for working with primitive types and reflection.
 * <p/>
 * <b>Terminology:</b>
 * <p/>
 * Non-reference primitive types (e.g. <code>int</code>, <code>double</code>, <code>void</code>) are
 * considered <i>native primitives</i>, or sometimes, <i>unboxed primitives</i>.
 * <p/>
 * Reference primitive types (e.g. {@link Integer}, {@link Double}, {@link Void}) are considered
 * <i>boxed primitives</i>.
 * <p/>
 * Conversion between native primitves and boxed primitives can be done with
 * {@link Primitives#box(Class)} and {@link Primitives#unbox(Class)}.
 * <p/>
 * Whenever possible, boxing/unboxing will be implicit and transparent, with a preference for native
 * primitive types.
 */
@SuppressWarnings("rawtypes")
public class Primitives {

  // a mapping of primitive types that are assignable to a wider primitive
  private static final Map<Class, Set<Class>> ASSIGNABLES;
  static {
    final ImmutableMap.Builder<Class, Set<Class>> builder = ImmutableMap.builder();
    builder.put(Short.TYPE, ImmutableSet.<Class>of(Byte.TYPE));
    builder.put(Integer.TYPE, ImmutableSet.<Class>of(Byte.TYPE, Short.TYPE));
    builder.put(Long.TYPE, ImmutableSet.<Class>of(Byte.TYPE, Short.TYPE, Integer.TYPE));
    builder.put(Float.TYPE,
        ImmutableSet.<Class>of(Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Character.TYPE));
    builder.put(Double.TYPE, ImmutableSet.<Class>of(Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE,
        Float.TYPE, Character.TYPE));
    ASSIGNABLES = builder.build();
  }

  // a mapping of primitive Class instances to their boxed counterparts
  private static final BiMap<Class, Class> BOXED_CLASSES;
  static {
    final ImmutableBiMap.Builder<Class, Class> builder = ImmutableBiMap.builder();
    builder.put(Byte.TYPE, Byte.class);
    builder.put(Short.TYPE, Short.class);
    builder.put(Integer.TYPE, Integer.class);
    builder.put(Long.TYPE, Long.class);
    builder.put(Float.TYPE, Float.class);
    builder.put(Double.TYPE, Double.class);
    builder.put(Boolean.TYPE, Boolean.class);
    builder.put(Character.TYPE, Character.class);
    builder.put(Void.TYPE, Void.class);
    BOXED_CLASSES = builder.build();
  }

  /**
   * Produces the {@link Class} for the boxed counterpart of the given primitive {@link Class}.
   * 
   * @param clazz the {@link Class} of the primitive type to get the boxed counterpart for.
   * @return the {@link Class} of the boxed counterpart to the given primitive type {@link Class}
   *         or; if the argument is already a boxed primitive, the argument itself.
   * @throws IllegalArgumentException if the given {@link Class} is not for a primitive type.
   */
  public static Class box(final Class clazz) {
    return isNativePrimitive(clazz) && BOXED_CLASSES.containsKey(clazz)
        ? BOXED_CLASSES.get(clazz)
        : requirePrimitiveClass(clazz);
  }

  /**
   * Produces the {@link Class} for the native primitive counterpart of the given boxed primitive
   * {@link Class}.
   * 
   * @param clazz the {@link Class} of the boxed primitive type to unbox to a native primitive.
   * @return the {@link Class} of the native primitive counterpart to the given boxed primitive
   *         {@link Class} or; if the argument is already a native primitive, the argument itself.
   * @throws IllegalArgumentException if the given {@link Class} is not for a primitive type.
   */
  public static Class unbox(final Class clazz) {
    return isBoxedPrimitive(clazz)
        ? BOXED_CLASSES.inverse().get(clazz)
        : requirePrimitiveClass(clazz);
  }

  /**
   * Determines whether the objects of the given source {@link Class} can be assigned to the
   * primitive type of the given target {@link Class}.
   * <p/>
   * If either type is a boxed-primitive, it will be unboxed automatically; all comparisons will be
   * of the native primitive types.
   * 
   * @param target the {@link Class} of the type you wish to assign to.
   * @param source the {@link Class} of the type you wish to assign from.
   * @return true if objects of the source {@link Class} can be assigned to the type of the target
   *         {@link Class}; otherwise, false.
   */
  public static boolean isAssignableFrom(final Class target, final Class source) {
    if (target.equals(Classes.Null.class)) {
      throw new NullPointerException("target for type assignment may not be the null type");
    }

    // permit null sources for boxed targets
    if (Primitives.isBoxedPrimitive(target) && source.equals(Classes.Null.class)) {
      return true;
    }

    // ensure source type is a primitive type
    if (!Primitives.isPrimitive(source)) {
      return false;
    }

    // unbox both types for comparison
    final Class unboxedTarget = unbox(target);
    final Class unboxedSource = unbox(source);

    return unboxedTarget.equals(unboxedSource)
        || (ASSIGNABLES.containsKey(unboxedTarget) && ASSIGNABLES.get(unboxedTarget).contains(
            unboxedSource));
  }

  /**
   * Determines whether the given {@link Class} is for a primitive type; either native or boxed.
   * <p/>
   * Both boxed and native primitive types are considered "primitives". Example: <code>
   *     Primitives.isPrimitive(int.class) == true;
   *     Primitives.isPrimitive(Integer.class) == true;
   *     Primitives.isPrimitive(String.class) == false;
   * </code>
   * 
   * @param clazz the {@link Class} of the type to determine whether or not it is for a primitive
   *        type.
   * @return true if the given {@link Class} is for a primitive type, boxed or native; false if the
   *         given {@link Class} is for any other type.
   */
  public static boolean isPrimitive(final Class clazz) {
    return clazz.isPrimitive() || BOXED_CLASSES.containsValue(clazz);
  }

  /**
   * Determines whether the given {@link Class} is for a boxed primitive type.
   * <p/>
   * Only boxed primitive types are accepted. Example: <code>
   *     Primitives.isPrimitive(int.class) == false;
   *     Primitives.isPrimitive(Integer.class) == true;
   *     Primitives.isPrimitive(String.class) == false;
   * </code>
   * 
   * @param clazz the {@link Class} of the type to determine whether or not it is for a boxed
   *        primitive type.
   * @return true if the given {@link Class} is for a boxed primitive type; false if the given
   *         {@link Class} is for any other type, including native primitive types.
   */
  public static boolean isBoxedPrimitive(final Class clazz) {
    return !clazz.isPrimitive() && BOXED_CLASSES.containsValue(clazz);
  }

  /**
   * Determines whether the given {@link Class} is for a native primitive type.
   * <p/>
   * Only native primitive types are accepted. Example: <code>
   *     Primitives.isPrimitive(int.class) == true;
   *     Primitives.isPrimitive(Integer.class) == false;
   *     Primitives.isPrimitive(String.class) == false;
   * </code>
   * 
   * @param clazz the {@link Class} of the type to determine whether or not it is for a native
   *        primitive type.
   * @return true if the given {@link Class} is for a native primitive type; false if the given
   *         {@link Class} is for any other type, including boxed primitive types.
   */
  public static boolean isNativePrimitive(final Class clazz) {
    return clazz.isPrimitive();
  }

  /**
   * Asserts that the given {@link Class} is for a primitive type, either native or boxed.
   * 
   * @param clazz the {@link Class} to assert is for a primitive type.
   * @return the given {@link Class}, without modifications, to facilitate chaining.
   * @throws AssertionError if the given {@link Class} is for a non-primitive type.
   */
  public static Class requirePrimitiveClass(final Class clazz) {
    if (!isPrimitive(clazz)) {
      throw new AssertionError("Class must be for a primitive type; " + clazz + " given");
    }
    return clazz;
  }
}