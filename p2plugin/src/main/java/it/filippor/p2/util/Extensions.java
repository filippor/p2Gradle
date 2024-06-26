package it.filippor.p2.util;

/**
 * Utility class
 * @author filippo.rossoni
 */
public class Extensions {
	private Extensions() {}
	/**
	 *
	 * shamelessly taken from Project Lombok
	 * https://github.com/rzwitserloot/lombok/blob/master/src/core/lombok/Lombok.java
	 *
	 * Throws the given exception and sneaks it through any compiler checks. This
	 * allows to throw checked exceptions without the need to declare it. Clients
	 * should use the following idiom to trick static analysis and dead code checks:
	 *
	 * <pre>
	 * throw sneakyThrow(new CheckedException("Catch me if you can ;-)")).
	 * </pre>
	 *
	 * This method is heavily inspired by project <a href=
	 * "https://github.com/rzwitserloot/lombok/blob/master/src/core/lombok/Lombok.java">Lombok</a>.
	 *
	 * @param t the throwable that should be sneaked through compiler checks. May
	 *          not be <code>null</code>.
	 * @return never returns anything since {@code t} is always thrown.
	 * @throws NullPointerException if {@code t} is <code>null</code>.
	 *
	 */
	public static RuntimeException sneakyThrow(Throwable t) {
		if (t == null) {
            throw new NullPointerException("t");
        }
		return sneakyThrow0(t);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> T sneakyThrow0(Throwable t) throws T {
		throw (T) t;
	}
}
