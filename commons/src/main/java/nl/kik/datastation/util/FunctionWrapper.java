package nl.kik.datastation.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FunctionWrapper {
	@FunctionalInterface
	public interface BiConsumerWithException<T, U, E extends Exception> {
		void accept(T t, U u) throws E;
	}

	@FunctionalInterface
	public interface BiFunctionWithException<T, U, R, E extends Exception> {
		R apply(T t, U u) throws E;
	}

	@FunctionalInterface
	public interface ConsumerWithException<T, E extends Exception> {
		void accept(T t) throws E;
	}

	@FunctionalInterface
	public interface FunctionWithException<T, R, E extends Exception> {
		R apply(T t) throws E;
	}

	@FunctionalInterface
	public interface SupplierWithException<T, E extends Exception> {
		T get() throws E;
	}

	@FunctionalInterface
	public interface TriConsumerWithException<T, U, V, E extends Exception> {
		void accept(T t, U u, V v) throws E;
	}

	public static <T, U, R, E extends Exception> BiConsumer<T, U> wrapper(final BiConsumerWithException<T, U, E> fe) {
		return (a1, a2) -> {
			try {
				fe.accept(a1, a2);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static <T, U, R, E extends Exception> BiFunction<T, U, R> wrapper(
			final BiFunctionWithException<T, U, R, E> fe) {
		return (a1, a2) -> {
			try {
				return fe.apply(a1, a2);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static <T, E extends Exception> Consumer<T> wrapper(final ConsumerWithException<T, E> fe) {
		return t -> {
			try {
				fe.accept(t);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static <T, R, E extends Exception> Function<T, R> wrapper(final FunctionWithException<T, R, E> fe) {
		return arg -> {
			try {
				return fe.apply(arg);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static <T, E extends Exception> Supplier<T> wrapper(final SupplierWithException<T, E> fe) {
		return () -> {
			try {
				return fe.get();
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		};
	}
}
