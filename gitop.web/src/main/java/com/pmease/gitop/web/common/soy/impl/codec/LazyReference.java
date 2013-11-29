package com.pmease.gitop.web.common.soy.impl.codec;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Lazily loaded reference that is not constructed until required. This class is used to maintain a
 * reference to an object that is expensive to create and must be constructed once and once only.
 * This reference behaves as though the <code>final</code> keyword has been used (you cannot reset
 * it once it has been constructed). Object creation is guaranteed to be thread-safe and the first
 * thread that calls {@link #get()} will be the one that creates it.
 * <p>
 * Usage: clients need to implement the {@link #create()} method to return the object this reference
 * will hold.
 * <p>
 * For instance:
 * <p>
 * 
 * <pre>
 * final LazyReference&lt;MyObject&gt; ref = new LazyReference() {
 *   protected MyObject create() throws Exception {
 *     // Do expensive object construction here
 *     return new MyObject();
 *   }
 * };
 * </pre>
 * 
 * Then call {@link #get()} to get a reference to the referenced object:
 * 
 * <pre>
 * MyObject myLazyLoadedObject = ref.get()
 * </pre>
 * 
 * NOTE: Interruption policy is that if you want to be cancellable while waiting for another thread
 * to create the value, instead of calling {@link #get()} call {@link #getInterruptibly()}. However,
 * If your {@link #create()} method is interrupted and throws an {@link InterruptedException}, it is
 * treated as an application exception and will be the causal exception inside the runtime
 * {@link InitializationException} that {@link #get()} or {@link #getInterruptibly()} throws and
 * your {@link #create()} will not be called again.
 * <p>
 * This class is NOT {@link Serializable}.
 * <p>
 * Implementation note. This class extends {@link WeakReference} as {@link Reference} does not have
 * a public constructor. WeakReference is preferable as it does not have any members and therefore
 * doesn't increase the memory footprint. As we never pass a referent through to the super-class and
 * override {@link #get()}, the garbage collection semantics of WeakReference are irrelevant. The
 * referenced object will not become eligible for GC unless the object holding the reference to this
 * object is collectible.
 * 
 * @param T the type of the contained element.
 */
public abstract class LazyReference<T> extends WeakReference<T>
    implements
      com.google.common.base.Supplier<T> {

  private final Sync sync = new Sync();

  public LazyReference() {
    super(null);
  }

  /**
   * The object factory method, guaranteed to be called once and only once.
   * 
   * @return the object that {@link #get()} and {@link #getInterruptibly()} will return.
   * @throws Exception if anything goes wrong, rethrown as an InitializationException from
   *         {@link #get()} and {@link #getInterruptibly()}
   */
  protected abstract T create() throws Exception;

  /**
   * Get the lazily loaded reference in a non-cancellable manner. If your <code>create()</code>
   * method throws an Exception calls to <code>get()</code> will throw an InitializationException
   * which wraps the previously thrown exception.
   * 
   * @return the object that {@link #create()} created.
   * @throws InitializationException if the {@link #create()} method throws an exception. The
   *         {@link InitializationException#getCause()} will contain the exception thrown by the
   *         {@link #create()} method
   */
  @Override
  public final T get() {
    boolean interrupted = false;
    try {
      while (true) {
        try {
          return getInterruptibly();
        } catch (final InterruptedException ignore) {
          // ignore and try again
          interrupted = true;
        }
      }
    } finally {
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Get the lazily loaded reference in a cancellable manner. If your <code>create()</code> method
   * throws an Exception, calls to <code>get()</code> will throw a RuntimeException which wraps the
   * previously thrown exception.
   * 
   * @return the object that {@link #create()} created.
   * @throws InitializationException if the {@link #create()} method throws an exception. The
   *         {@link InitializationException#getCause()} will contain the exception thrown by the
   *         {@link #create()} method
   * @throws InterruptedException If the calling thread is Interrupted while waiting for another
   *         thread to create the value (if the creating thread is interrupted while blocking on
   *         something, the {@link InterruptedException} will be thrown as the causal exception of
   *         the {@link InitializationException} to everybody calling this method).
   */
  public final T getInterruptibly() throws InterruptedException {
    if (!sync.isDone()) {
      sync.run();
    }

    try {
      return sync.get();
    } catch (final ExecutionException e) {
      throw new InitializationException(e);
    }
  }

  /**
   * Has the {@link #create()} reference been initialized.
   * 
   * @return true if the task is complete
   */
  public boolean isInitialized() {
    return sync.isDone();
  }

  /**
   * Cancel the initializing operation if it has not already run. Will try and interrupt if it is
   * currently running.
   */
  public void cancel() {
    sync.cancel(true);
  }

  /**
   * If the factory {@link LazyReference#create()} method threw an exception, this wraps it.
   */
  public static class InitializationException extends RuntimeException {
    private static final long serialVersionUID = 3638376010285456759L;

    InitializationException(final ExecutionException e) {
      super((e.getCause() != null) ? e.getCause() : e);
    }
  }

  /**
   * Synchronization control for LazyReference. Note that this must be a non-static inner class in
   * order to invoke the protected <tt>create</tt> method. Taken from FutureTask AQS implementation
   * and pruned to be as compact as possible.
   * 
   * Uses AQS sync state to represent run status.
   */
  private final class Sync extends AbstractQueuedSynchronizer {

    /**
     * only here to shut up the compiler warnings, the outer class is NOT serializable
     */
    private static final long serialVersionUID = -1645412544240373524L;

    /** State value representing that task is running */
    private static final int RUNNING = 1;
    /** State value representing that task ran */
    private static final int RAN = 2;
    /** State value representing that task was cancelled */
    private static final int CANCELLED = 4;

    /** The result to return from get() */
    private T result;
    /** The exception to throw from get() */
    private Throwable exception;

    /**
     * The thread running task. When nulled after set/cancel, this indicates that the results are
     * accessible. Must be volatile, to ensure visibility upon completion.
     */
    private volatile Thread runner;

    private boolean ranOrCancelled(final int state) {
      return (state & (RAN | CANCELLED)) != 0;
    }

    /**
     * Implements AQS base acquire to succeed if ran or cancelled
     */
    @Override
    protected int tryAcquireShared(final int ignore) {
      return isDone() ? 1 : -1;
    }

    /**
     * Implements AQS base release to always signal after setting final done status by nulling
     * runner thread.
     */
    @Override
    protected boolean tryReleaseShared(final int ignore) {
      runner = null;
      return true;
    }

    boolean isDone() {
      return ranOrCancelled(getState()) && (runner == null);
    }

    T get() throws InterruptedException, ExecutionException {
      acquireSharedInterruptibly(0);
      if (getState() == CANCELLED) {
        throw new CancellationException();
      }
      if (exception != null) {
        throw new ExecutionException(exception);
      }
      return result;
    }

    void set(final T v) {
      for (;;) {
        final int s = getState();
        if (s == RAN) {
          return;
        }
        if (s == CANCELLED) {
          // aggressively release to set runner to null,
          // in case we are racing with a cancel request
          // that will try to interrupt runner
          releaseShared(0);
          return;
        }
        if (compareAndSetState(s, RAN)) {
          result = v;
          releaseShared(0);
          return;
        }
      }
    }

    void setException(final Throwable t) {
      for (;;) {
        final int s = getState();
        if (s == RAN) {
          return;
        }
        if (s == CANCELLED) {
          // aggressively release to set runner to null,
          // in case we are racing with a cancel request
          // that will try to interrupt runner
          releaseShared(0);
          return;
        }
        if (compareAndSetState(s, RAN)) {
          exception = t;
          result = null;
          releaseShared(0);
          return;
        }
      }
    }

    void cancel(final boolean mayInterruptIfRunning) {
      for (;;) {
        final int s = getState();
        if (ranOrCancelled(s)) {
          return;
        }
        if (compareAndSetState(s, CANCELLED)) {
          break;
        }
      }
      if (mayInterruptIfRunning) {
        final Thread r = runner;
        if (r != null) {
          r.interrupt();
        }
      }
      releaseShared(0);
    }

    void run() {
      if (!compareAndSetState(0, RUNNING)) {
        return;
      }
      try {
        runner = Thread.currentThread();
        if (getState() == RUNNING) {
          set(create());
        } else {
          releaseShared(0); // cancel
        }
      } catch (final Throwable ex) {
        setException(ex);
      }
    }
  }
}
