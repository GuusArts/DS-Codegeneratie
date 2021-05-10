package nl.kik.datastation.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FunctionWrapper {
	@FunctionalInterface
	public static interface FunctionWithException<T, R, E extends Exception> {
		R apply(T t) throws E;
	}

	@FunctionalInterface
	public static interface BiFunctionWithException<T, U, R, E extends Exception> {
		R apply(T t, U u) throws E;
	}

	@FunctionalInterface
	public static interface SupplierWithException<T, E extends Exception> {
		T get() throws E;
	}

	@FunctionalInterface
	public static interface ConsumerWithException<T, E extends Exception> {
		void accept(T t) throws E;
	}

	@FunctionalInterface
	public static interface BiConsumerWithException<T, U, E extends Exception> {
		void accept(T t, U u) throws E;
	}

	@FunctionalInterface
	public static interface TriConsumerWithException<T, U, V, E extends Exception> {
		void accept(T t, U u, V v) throws E;
	}

	public static <T, R, E extends Exception> Function<T, R> wrapper(FunctionWithException<T, R, E> fe) {
		return arg -> {
			try {
				return fe.apply(arg);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static <T, U, R, E extends Exception> BiFunction<T, U, R> wrapper(BiFunctionWithException<T, U, R, E> fe) {
		return (a1, a2) -> {
			try {
				return fe.apply(a1, a2);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static <T, U, R, E extends Exception> BiConsumer<T, U> wrapper(BiConsumerWithException<T, U, E> fe) {
		return (a1, a2) -> {
			try {
				fe.accept(a1, a2);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static <T, E extends Exception> Supplier<T> wrapper(SupplierWithException<T, E> fe) {
		return () -> {
			try {
				return fe.get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static <T, E extends Exception> Consumer<T> wrapper(ConsumerWithException<T, E> fe) {
		return t -> {
			try {
				fe.accept(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}
}
