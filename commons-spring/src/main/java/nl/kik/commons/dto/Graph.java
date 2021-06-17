package nl.kik.commons.dto;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.Transactional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * An abstraction of an RDF graph. contains both the metadata and the actual
 * model
 * 
 * @param <G>
 */
@SuperBuilder(toBuilder = true, builderMethodName = "creator")
@Getter
@ToString(exclude = { "model", "transactional" })
@JsonInclude(Include.NON_NULL)
@Slf4j
public class Graph<G extends Model> {
	private G model;
	private Transactional transactional;
	private Graph<? extends Model> delegate;

	@Getter(AccessLevel.NONE)
	private final ThreadLocal<Integer> nesting = ThreadLocal.withInitial(() -> 0);
	@Getter(AccessLevel.NONE)
	private final ThreadLocal<Boolean> locktype = ThreadLocal.withInitial(() -> false);
	@Getter(AccessLevel.NONE)
	private final ThreadLocal<Boolean> failed = ThreadLocal.withInitial(() -> false);

	public Dataset getDataset() {
		if (transactional instanceof Dataset) {
			return (Dataset) transactional;
		}
		log.warn("Trying to obtain dataset from graph that has none");
		return null;
	}

	public static <T extends Model> Graph<T> create(T model) {
		return create(model, null);
	}

	public static <T extends Model> Graph<T> create(T model, Transactional t) {
		return Graph.<T>creator() //
				.model(model) //
				.transactional(t) //
				.build();
	}

	public static <T extends Model> Graph<T> create(Pair<T, Transactional> p) {
		return create(p.getLeft(), p.getRight());
	}

	public void commit() {
		if (delegate != null) {
			delegate.commit();
		} else {
			if (nesting.get() == 1 && Lock.WRITE == locktype.get()) {
				failed.set(false);
			}
		}
	}

	/**
	 * 
	 */
	public void end() {
		if (delegate != null) {
			delegate.end();
		} else {
			int n = nesting.get() - 1;
			nesting.set(n);
			log.trace("Unlocking at level {} in {}", n + 1, Thread.currentThread());
			if (n < 0) {
				throw new RuntimeException("Begin-einde is niet in balans");
			}
			if (n == 0) {
				boolean write = Lock.WRITE == locktype.get();
				boolean fail = failed.get();
				end(model, transactional, write ? fail : false);
			}
		}
	}

	/**
	 * 
	 */
	public static void end(Model model, Transactional t, boolean failed) {
		try {
			model.leaveCriticalSection();
			if (model.supportsTransactions()) {
				if (!failed) {
					if (t == null) {
						model.commit();
					} else {
						t.commit();
					}
				} else {
					if (t == null) {
						model.abort();
					} else {
						t.abort();
					}
				}
			}
		} catch (Throwable e) {
			if (model.supportsTransactions()) {
				if (t == null) {
					model.abort();
				} else {
					t.abort();
				}
			}
			throw e;
		}
	}

	/**
	 * 
	 */
	public void beginRead() {
		if (delegate != null) {
			delegate.beginRead();
		} else {
			int n = nesting.get() + 1;
			nesting.set(n);
			log.trace("Locking for READ at level {} in {}", n, Thread.currentThread());
			if (n == 1) {
				locktype.set(Lock.READ);
				begin(model, transactional, Lock.READ);
			}
		}
	}

	/**
	 * @param lock
	 * 
	 */
	public static void begin(Model model, Transactional t, boolean lock) {
		if (model.supportsTransactions()) {
			if (t == null) {
				model.begin();
			} else {
				t.begin(lock == Lock.READ ? TxnType.READ : TxnType.WRITE);
			}
		}
		model.enterCriticalSection(lock);
	}

	/**
	 * 
	 */
	public void beginWrite() {
		if (delegate != null) {
			delegate.beginWrite();
		} else {
			int n = nesting.get() + 1;
			nesting.set(n);
			log.trace("Locking for WRITE at level {} in {}", n, Thread.currentThread());
			if (n == 1) {
				locktype.set(Lock.WRITE);
				failed.set(true);
				begin(model, transactional, Lock.WRITE);
			} else {
				if (Lock.WRITE != locktype.get()) {
					throw new RuntimeException("Fout bij wisselen naar schrijfmodus in transactie");
				}
			}
		}
	}

}
