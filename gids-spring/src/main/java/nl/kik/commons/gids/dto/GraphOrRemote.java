package nl.kik.commons.gids.dto;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import nl.kik.commons.dto.Graph;

public class GraphOrRemote implements nl.kik.commons.dto.Source {
	private final Graph<? extends Model> graph;
	private final String remote;
	private final String auth;
	private final Map<Resource, MultiValuedMap<Property, RDFNode>> cache;
	private final Map<Resource, MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>>> sources;
	private final Map<RDFNode, Object> objectCache;
	private final Map<Pair<Resource, Property>, GidsAttribute<?>> alternativesCache;

	public GraphOrRemote(final Graph<? extends Model> graph) {
		Objects.requireNonNull(graph);
		this.graph = graph;
		remote = null;
		auth = null;
		cache = null;
		sources = null;
		objectCache = null;
		alternativesCache = null;
	}

	public GraphOrRemote(final Map<Resource, MultiValuedMap<Property, RDFNode>> cache,
			final Map<Resource, MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>>> sources) {
		Objects.requireNonNull(cache);
		Objects.requireNonNull(sources);
		this.cache = cache;
		this.sources = sources;
		objectCache = new HashMap<>();
		alternativesCache = new HashMap<>();
		remote = null;
		auth = null;
		graph = null;
	}

	public GraphOrRemote(final String remote) {
		this(remote, null);
	}

	public GraphOrRemote(final String remote, final String auth) {
		Objects.requireNonNull(remote);
		graph = null;
		this.remote = remote;
		this.auth = StringUtils.trimToNull(auth);
		cache = null;
		sources = null;
		objectCache = null;
		alternativesCache = null;
	}

	@Override
	public void beginRead() {
		if (isGraph()) {
			getGraph().beginRead();
		}
	}

	@Override
	public void beginWrite() {
		if (isGraph()) {
			getGraph().beginWrite();
		}
	}

	@Override
	public void commit() {
		if (isGraph()) {
			getGraph().commit();
		}
	}

	@Override
	public void end() {
		if (isGraph()) {
			getGraph().end();
		}
	}

	public Map<Pair<Resource, Property>, GidsAttribute<?>> getAlternativesCache() {
		return alternativesCache;
	}

	public String getAuth() {
		return auth;
	}

	public Map<Resource, MultiValuedMap<Property, RDFNode>> getCache() {
		return cache;
	}

	public Graph<? extends Model> getGraph() {
		return graph;
	}

	public Map<RDFNode, Object> getObjectCache() {
		return objectCache;
	}

	public String getRemote() {
		return remote;
	}

	@Override
	public Resource getResource(final String uri) {
		if (isGraph())
			return getGraph().getResource(uri);
		if (isRemote() || isCache())
			return new ResourceImpl(uri);
		throw new IllegalArgumentException(); // Should never reach here
	}

	public Map<Resource, MultiValuedMap<Pair<Property, RDFNode>, Triple<LocalDate, LocalDate, Resource>>> getSources() {
		return sources;
	}

	public boolean hasAuth() {
		return auth != null;
	}

	public boolean isCache() {
		return getCache() != null;
	}

	public boolean isGraph() {
		return graph != null;
	}

	public boolean isRemote() {
		return remote != null;
	}

}
