/*
 * This is free and unencumbered software released into the public domain.
 * See UNLICENSE.
 */
package scala_maven_executions;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Helpers for running a Scala compiler's non-exiting {@code process(String[])} entry point
 * in-process. Unlike {@code main} (which ends in {@code System.exit}), {@code process} returns, so
 * an in-process build survives it.
 */
public class ProcessHelper {

  /** Runs {@code mainClassName}'s {@code process(String[])} in-process; {@code true} on success. */
  static boolean runProcess(String mainClassName, List<String> args, ClassLoader cl)
      throws Exception {
    if (cl == null) {
      cl = Thread.currentThread().getContextClassLoader();
    }
    // The compiler objects (nsc Main, dotty Main) expose process as a static forwarder.
    Method process = cl.loadClass(mainClassName).getMethod("process", String[].class);
    Object result = process.invoke(null, new Object[] {args.toArray(new String[] {})});
    // nsc's process returns a Boolean success flag; dotty's returns a Reporter.
    if (result instanceof Boolean) {
      return (Boolean) result;
    }
    return !reporterHasErrors(result);
  }

  /** Whether the {@code Reporter} returned by Scala 3's {@code process} recorded errors. */
  private static boolean reporterHasErrors(Object reporter) throws ReflectiveOperationException {
    return (Boolean) reporter.getClass().getMethod("hasErrors").invoke(reporter);
  }
}
