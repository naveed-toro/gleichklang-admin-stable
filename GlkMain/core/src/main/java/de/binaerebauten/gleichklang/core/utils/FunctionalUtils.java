package de.binaerebauten.gleichklang.core.utils;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * This class contains helper functions supporting functional programming.
 */
public class FunctionalUtils
{
	/**
	 * This function calls the given supplier and wraps the result in an {@link Optional}.
	 * If an {@link NullPointerException} is thrown during the execution of the supllier,
	 * an empty optional is returned {@link Optional#empty()}.
	 * <p/>
	 * This function should be used with care, because throwing an exception isn't the
	 * fastest operation on the JVM.
	 *
	 * @param s   the supllier to call
	 * @param <T> the result type of the given supllier
	 * @return the optional result of calling the given supplier, can be empty
	 */
	public static <T> Optional<T> nullSafe(Supplier<T> s)
	{
		try
		{
			return Optional.ofNullable(s.get());
		}
		catch (NullPointerException e)
		{
			return Optional.empty();
		}
	}
	
	/**
	 * Recursive interface for lambdas.
	 *
	 * @param <I> lambda function
	 */
	public static class Recursive<I>
	{
		public I func;
	}
	
}
