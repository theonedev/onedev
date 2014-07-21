package com.pmease.gitplex.web.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Utilities for working with {@link Class}es and reflection.
 */
public class Classes {

  /**
   * Creates a new instance of the given {@link Class}, using the given arguments.
   * <p/>
   * A new instance object of the given {@link Class} is created, using reflection, providing the
   * given arguments to the constructor.
   * 
   * @param clazz the {@link Class} of the object to instantiate.
   * @param args the arguments to pass to the constructor, if any.
   * @param <T> the type of the {@link Class} to return.
   * @return a new instance of the given {@link Class}, constructed using the given <i>args</i>.
   * 
   * @throws NoSuchMethodException if the given {@link Class} doesn't provide a constructor that can
   *         be applied to the given arguments.
   * @throws IllegalAccessException if the appropriate constructor is inaccessible.
   * @throws IllegalArgumentException if the number of actual and formal parameters differ; if an
   *         unwrapping conversion for primitive arguments fails; or if, after possible unwrapping,
   *         a parameter value cannot be converted to the corresponding formal parameter type by a
   *         method invocation conversion; if this constructor pertains to an enum type.
   * @throws InstantiationException if the given {@link Class} is <i>abstract</i>, an
   *         <i>interface</i>, an array or doesn't define a nullary constructor.
   * @throws InvocationTargetException if the underlying constructor throws an exception.
   * @throws ExceptionInInitializerError if the initialization provoked by this method fails.
   */
  public static <T> T newInstance(final Class<T> clazz, final Object... args)
      throws NoSuchMethodException, InstantiationException, IllegalAccessException,
      InvocationTargetException {
    return getApplicableConstructor(clazz, Classes.of(args)).newInstance(args);
  }

  /**
   * Creates a new instance of the same {@link Class} as the given <i>template</i>, using the given
   * constructor arguments.
   * <p/>
   * Given an object of type <code>T</code>, a new instance of {@link Class<T>} will be created,
   * passing the given <i>args</i> to the constructor.
   * 
   * @param template an object that provides the {@link Class} to instantiate.
   * @param args the arguments to pass to the constructor, if any.
   * @param <T> the type of the object to return
   * @return a new instance of the same {@link Class} as <i>template</i>, constructed using the
   *         given <i>args</i>.
   * 
   * @throws NoSuchMethodException if the given {@link Class} doesn't provide a constructor that can
   *         be applied to the given arguments.
   * @throws IllegalAccessException if the appropriate constructor is inaccessible.
   * @throws IllegalArgumentException if the number of actual and formal parameters differ; if an
   *         unwrapping conversion for primitive arguments fails; or if, after possible unwrapping,
   *         a parameter value cannot be converted to the corresponding formal parameter type by a
   *         method invocation conversion; if this constructor pertains to an enum type.
   * @throws InstantiationException if the given {@link Class} is <i>abstract</i>, an
   *         <i>interface</i>, an array or doesn't define a nullary constructor.
   * @throws InvocationTargetException if the underlying constructor throws an exception.
   * @throws ExceptionInInitializerError if the initialization provoked by this method fails.
   */
  @SuppressWarnings("unchecked")
  public static <T> T newInstanceFrom(final T template, final Object... args)
      throws NoSuchMethodException, InstantiationException, IllegalAccessException,
      InvocationTargetException {
    return newInstance(((Class<? extends T>) template.getClass()), args);
  }

  /**
   * Creates a new instance of the given {@link Class}, using the given arguments, ignoring
   * visibility.
   * <p/>
   * A new instance object of the given {@link Class} is created, using reflection, providing the
   * given arguments to the constructor.
   * <p/>
   * The visibility of the {@link Constructor} defined by the arguments is ignored and a new
   * instance created irrespective of the defined visibility. This is potentially dangerous, as the
   * API likely makes no guarantee as to the behaviour when instantiating from a non-public
   * constructor.
   * 
   * @param clazz the {@link Class} of the object to instantiate.
   * @param args the arguments to pass to the constructor, if any.
   * @param <T> the type of the {@link Class} to return.
   * @return a new instance of the given {@link Class}, constructed using the given <i>args</i>.
   * 
   * @throws NoSuchMethodException if the given {@link Class} doesn't provide a constructor that can
   *         be applied to the given arguments.
   * @throws IllegalArgumentException if the number of actual and formal parameters differ; if an
   *         unwrapping conversion for primitive arguments fails; or if, after possible unwrapping,
   *         a parameter value cannot be converted to the corresponding formal parameter type by a
   *         method invocation conversion; if this constructor pertains to an enum type.
   * @throws InstantiationException if the given {@link Class} is <i>abstract</i>, an
   *         <i>interface</i>, an array or doesn't define a nullary constructor.
   * @throws InvocationTargetException if the underlying constructor throws an exception.
   * @throws ExceptionInInitializerError if the initialization provoked by this method fails.
   */
  public static <T> T unsafeNewInstance(final Class<T> clazz, final Object... args)
      throws NoSuchMethodException, InstantiationException, InvocationTargetException {
    final Constructor<T> constructor = getApplicableConstructor(clazz, Classes.of(args));
    constructor.setAccessible(true);
    try {
      return constructor.newInstance(args);
    } catch (final IllegalAccessException e) {
      throw new AssertionError("IllegalAccessError while instantiating " + clazz
          + " with access controls disabled");
    }
  }

  /**
   * Creates a new instance of the same {@link Class} as the given <i>template</i>, using the given
   * constructor arguments and ignoring visibility.
   * <p/>
   * Given an object of type <code>T</code>, a new instance of {@link Class<T>} will be created,
   * passing the given <i>args</i> to the constructor.
   * <p/>
   * The visibility of the {@link Constructor} defined by the arguments is ignored and a new
   * instance created irrespective of the defined visibility. This is potentially dangerous, as the
   * API likely makes no guarantee as to the behaviour when instantiating from a non-public
   * constructor.
   * 
   * @param template an object that provides the {@link Class} to instantiate.
   * @param args the arguments to pass to the constructor, if any.
   * @param <T> the type of the object to return
   * @return a new instance of the same {@link Class} as <i>template</i>, constructed using the
   *         given <i>args</i>.
   * 
   * @throws NoSuchMethodException if the given {@link Class} doesn't provide a constructor that can
   *         be applied to the given arguments.
   * @throws IllegalArgumentException if the number of actual and formal parameters differ; if an
   *         unwrapping conversion for primitive arguments fails; or if, after possible unwrapping,
   *         a parameter value cannot be converted to the corresponding formal parameter type by a
   *         method invocation conversion; if this constructor pertains to an enum type.
   * @throws InstantiationException if the given {@link Class} is <i>abstract</i>, an
   *         <i>interface</i>, an array or doesn't define a nullary constructor.
   * @throws InvocationTargetException if the underlying constructor throws an exception.
   * @throws ExceptionInInitializerError if the initialization provoked by this method fails.
   */
  @SuppressWarnings("unchecked")
  public static <T> T unsafeNewInstanceFrom(final T template, final Object... args)
      throws NoSuchMethodException, InstantiationException, InvocationTargetException {
    return unsafeNewInstance((Class<? extends T>) template.getClass(), args);
  }

  /**
   * Gets the {@link Class} for multiple objects.
   * <p/>
   * The resulting array of {@link Class} objects that are ordered in parallel with the argument
   * list that produced it. This is especially useful for getting a {@link Constructor} for a given
   * set of arguments:
   * <p/>
   * <code>
   *     clazz.getConstructor(Classes.of("abc", 123));
   * </code>
   * 
   * @param arguments the objects to generate {@link Class}es for
   * @return an array of {@link Class} objects, in the same order as the argument list
   */
  @SuppressWarnings("rawtypes")
  public static Class[] of(final Object... arguments) {
    final Object[] args = resolveVarArgs(arguments);
    final Class[] classes = new Class[args.length];
    for (int i = 0; i < args.length; i++) {
      classes[i] = args[i] == null ? Null.class : args[i].getClass();
    }
    return classes;
  }

  /**
   * Ensures a variable argument list has been properly passed.
   * <p/>
   * Sometimes, you want to pass a single array-typed argument to a method that accepts variable
   * arguments. In these situations, that array will be unwrapped in to a list of multiple
   * arguments, instead of a single argument that is an array.
   * <p/>
   * Example: <code>
   *     Classes.of(TableNotFoundException.class, tableName.getBytes());
   * </code>
   * <p/>
   * Resolving will differentiate a variable argument list from a single argument of the following
   * types:
   * <ul>
   * <li>All primitive array types: e.g. <i>byte[]</i>, <i>int[]</i>, <i>boolean[]</i>, etc.</li>
   * <li>Arrays of reference types that are not <i>Object</i>: e.g. <i>String[]</i>,
   * <i>java.lang.Integer[]</i>, <i>Class[]</i>; but <i>not</i> <i>Object[]</i></li>
   * </ul>
   * An argument of type <i>Object[]</i> is considered to be a variable list of arguments, each with
   * their own distinct type.
   * 
   * @param args the argument list to resolve
   * @return the argument list, wrapped to protect single array-typed arguments
   */
  public static Object[] resolveVarArgs(final Object... args) {
    return args != null && args.getClass() == Object[].class ? args : new Object[] {args};
  }

  /**
   * Gets a {@link Constructor} from the given {@link Class} that is applicable to the given
   * arguments.
   * <p/>
   * If the types of the given arguments are not an exact match for any declared {@link Constructor}
   * s, a Constructor that will accept the arguments (e.g. because they are sub-types of the
   * parameters) will be searched for.
   * <p/>
   * If the given {@link Class} has no {@link Constructor} that is applicable to the given
   * arguments, a {@link NoSuchMethodException} will be thrown.
   * <p/>
   * No guarantees are made about the visibility of the {@link Constructor} returned; it may not be
   * accessible from the calling scope.
   * 
   * @param clazz the {@link Class} to search for a {@link Constructor} for.
   * @param args the types the {@link Constructor} must be applicable to.
   * @param <T> the type of the objects instantiated by the {@link Constructor}.
   * @return a {@link Constructor} that is applicable to the given arguments for constructing
   *         objects of type {@link T}.
   * @throws NoSuchMethodException if no {@link Constructor} is found that is applicable to the
   *         given arguments.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T> Constructor<T> getApplicableConstructor(final Class<T> clazz,
      final Class... args) throws NoSuchMethodException {
    final Constructor[] constructors = clazz.getDeclaredConstructors();
    for (final Constructor<T> constructor : constructors) {
      if (isAssignableFrom(constructor.getParameterTypes(), args)) {
        return constructor;
      }
    }
    throw new NoSuchMethodException(String.format(
        "No applicable constructor for %s found for argument types: %s", clazz.getName(),
        Arrays.toString(args)));
  }

  /**
   * Gets the {@link Method} of the given name from the given {@link Class} that is applicable to
   * the given argument types.
   * <p/>
   * If the types of the given arguments are not an exact match for any declared {@link Method}s, a
   * {@link Method} that will accept the arguments (e.g. because they are sub-types of the
   * parameters) will be searched for.
   * <p/>
   * If the given {@link Class} has no {@link Method} with the given name that is applicable to the
   * given arguments, a {@link NoSuchMethodException} will be thrown.
   * <p/>
   * No guarantees are made about the visibility of the {@link Method} returned; it may not be
   * accessible from the calling scope. Similarly, the {@link Method} may be static or may be an
   * instance method.
   * 
   * @param clazz the {@link Class} to get the {@link Method} from.
   * @param name the name of the {@link Method} to get.
   * @param args the types the {@link Method} must be applicable to.
   * @return a {@link Method} of the given name from the given {@link Class} that is applicable to
   *         the given arguments.
   * @throws NoSuchMethodException if no {@link Method} is found that is applicable to the given
   *         arguments.
   */
  @SuppressWarnings("rawtypes")
  public static Method getApplicableMethod(final Class clazz, final String name,
      final Class... args) throws NoSuchMethodException {
    final Method[] methods = clazz.getDeclaredMethods();
    for (final Method m : methods) {
      if (m.getName().equals(name) && isAssignableFrom(m.getParameterTypes(), args)) {
        return m;
      }
    }
    throw new NoSuchMethodException(String.format(
        "No applicable method overload %s#%s found for argument types: %s", clazz.getName(), name,
        Arrays.toString(args)));
  }

  /**
   * Determines if the {@link Class}s for the given target types can be assigned to from the types
   * represented by the given source {@link Class}s.
   * <p/>
   * Each target type will be matched up with the corresponding source type. If the length of the
   * two arrays differ, they are considered not assignable.
   * 
   * @param targets the {@link Class}s representing the types to assign to.
   * @param sources the {@link Class}s representing the types to assign from.
   * @return true if all source {@link Class}es represent a type that can be assigned to their
   *         corresponding target {@link Class}.
   */
  @SuppressWarnings("rawtypes")
  public static boolean isAssignableFrom(final Class[] targets, final Class[] sources) {
    // must have same number of types
    if (sources.length != targets.length) {
      return false;
    }

    // quick check for exact matches
    if (Arrays.equals(sources, targets)) {
      return true;
    }

    // check if each source type can be assigned to each target type
    for (int i = 0; i < targets.length; i++) {
      if (!isAssignableFrom(targets[i], sources[i])) {
        return false;
      }
    }

    return true;
  }

  /***
   * Determines if the given source {@link Class} represents a type that can be assigned to the
   * given target {@link Class}.
   * <p/>
   * If the source {@link Class} represents null (via {@link Null}), it can be assigned to any
   * {@link Class} that represents a reference type.
   * 
   * @param target the {@link Class} of the type to assign to.
   * @param source the {@link Class} of the type to assign from.
   * @return true if the source {@link Class} represents a type that can be assigned to the type
   *         represented by the target {@link Class}.
   */
  public static boolean isAssignableFrom(final Class<?> target, final Class<?> source) {
    if (target.equals(Null.class)) {
      throw new NullPointerException("target for type assignment may not be the null type");
    }

    return Primitives.isPrimitive(target) ? Primitives.isAssignableFrom(target, source) : source
        .equals(Null.class) || target.isAssignableFrom(source);
  }

  /**
   * A placeholder Null type.
   * <p/>
   * This Null type provides a means to get a {@link Class} for null values. This should never be
   * used except for special-casing nulls.
   */
  static class Null {
    private Null() {}
  }
}