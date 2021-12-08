package nl.kik.commons.dto;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.ProfileRegistry;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
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
@ToString
@JsonInclude(Include.NON_NULL)
@Slf4j
public class Graph<G extends Model> implements Source {
	/**
	 * @param lock
	 *
	 */
	public static void begin(final Model model, final Transactional t, final boolean lock) {
		if (model.supportsTransactions()) {
			if (t == null) {
				model.begin();
			} else {
				t.begin(lock == Lock.READ ? TxnType.READ : TxnType.WRITE);
			}
		}
		model.enterCriticalSection(lock);
	}

	public static <T extends Model> Graph<T> create(final Pair<T, Transactional> p) {
		return Graph.create(p.getLeft(), p.getRight(), null);
	}

	public static <T extends Model> Graph<T> create(final T model) {
		return Graph.create(model, null, null);
	}

	public static <T extends Model> Graph<T> create(final T model, final Graph<? extends Model> delegate) {
		return Graph.create(model, null, delegate);
	}

	public static <T extends Model> Graph<T> create(final T model, final Transactional t) {
		return Graph.create(model, t, null);
	}

	public static <T extends Model> Graph<T> create(final T model, final Transactional t,
			final Graph<? extends Model> delegate) {
		return Graph.<T>creator() //
				.model(model) //
				.transactional(t) //
				.delegate(delegate) //
				.build();
	}

	/**
	 *
	 */
	public static void end(final Model model, final Transactional t, final boolean failed) {
		try {
			model.leaveCriticalSection();
			if (model.supportsTransactions()) {
				if (!failed) {
					if (t == null) {
						model.commit();
					} else {
						t.commit();
					}
				} else if (t == null) {
					model.abort();
				} else {
					t.abort();
				}
			}
		} catch (final Throwable e) {
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

	@ToString.Exclude
	private G model;

	@ToString.Exclude
	private Transactional transactional;

	private Graph<? extends Model> delegate;

	@ToString.Exclude
	private OntModel ontModel;

	@Getter(AccessLevel.NONE)
	private final ThreadLocal<Integer> nesting = ThreadLocal.withInitial(() -> 0);

	@Getter(AccessLevel.NONE)
	private final ThreadLocal<Boolean> locktype = ThreadLocal.withInitial(() -> false);

	@Getter(AccessLevel.NONE)
	private final ThreadLocal<Boolean> failed = ThreadLocal.withInitial(() -> false);

	/**
	 *
	 */
	@Override
	public void beginRead() {
		if (delegate != null) {
			delegate.beginRead();
		} else {
			final int n = nesting.get() + 1;
			nesting.set(n);
			Graph.log.trace("Locking for READ at level {} in {}", n, Thread.currentThread());
			if (n == 1) {
				locktype.set(Lock.READ);
				Graph.begin(model, transactional, Lock.READ);
			}
		}
	}

	/**
	 *
	 */
	@Override
	public void beginWrite() {
		if (delegate != null) {
			delegate.beginWrite();
		} else {
			final int n = nesting.get() + 1;
			nesting.set(n);
			Graph.log.trace("Locking for WRITE at level {} in {}", n, Thread.currentThread());
			if (n == 1) {
				locktype.set(Lock.WRITE);
				failed.set(true);
				Graph.begin(model, transactional, Lock.WRITE);
			} else if (Lock.WRITE != locktype.get()) {
				throw new RuntimeException("Fout bij wisselen naar schrijfmodus in transactie");
			}
		}
	}

	@Override
	public void commit() {
		if (delegate != null) {
			delegate.commit();
		} else if (nesting.get() == 1 && Lock.WRITE == locktype.get()) {
			failed.set(false);
		}
	}

	/**
	 *
	 */
	@Override
	public void end() {
		if (delegate != null) {
			delegate.end();
		} else {
			final int n = nesting.get() - 1;
			nesting.set(n);
			Graph.log.trace("Unlocking at level {} in {}", n + 1, Thread.currentThread());
			if (n < 0) {
				throw new RuntimeException("Begin-einde is niet in balans");
			}
			if (n == 0) {
				final boolean write = Lock.WRITE == locktype.get();
				final boolean fail = failed.get();
				Graph.end(model, transactional, write ? fail : false);
			}
		}
	}

	public Dataset getDataset() {
		if (transactional instanceof Dataset) {
			return (Dataset) transactional;
		}
		Graph.log.warn("Trying to obtain dataset from graph that has none");
		return null;
	}

	public OntModel getOntModel() {
		if (ontModel == null) {
			ModelFactory.createOntologyModel(OntModelSpec.getDefaultSpec(ProfileRegistry.OWL_LANG), getModel());
		}
		return ontModel;
	}

	@Override
	public Resource getResource(final String uri) {
		return getModel().getResource(uri);
	}

}
