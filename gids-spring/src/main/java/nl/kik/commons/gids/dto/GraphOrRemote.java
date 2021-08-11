package nl.kik.commons.gids.dto;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import nl.kik.commons.dto.Graph;

public class GraphOrRemote implements nl.kik.commons.dto.Source {
	private final Graph<? extends Model> graph;
	private final String remote;
	private final String auth;

	public GraphOrRemote(final Graph<? extends Model> graph) {
		Objects.requireNonNull(graph);
		this.graph = graph;
		remote = null;
		auth = null;
	}

	public GraphOrRemote(final String remote) {
		this(remote, null);
	}

	public GraphOrRemote(final String remote, final String auth) {
		Objects.requireNonNull(remote);
		graph = null;
		this.remote = remote;
		this.auth = StringUtils.trimToNull(auth);
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

	public String getAuth() {
		return auth;
	}

	public Graph<? extends Model> getGraph() {
		return graph;
	}

	public String getRemote() {
		return remote;
	}

	@Override
	public Resource getResource(final String uri) {
		if (isGraph())
			return getGraph().getResource(uri);
		if (isRemote())
			return new ResourceImpl(uri);
		throw new IllegalArgumentException(); // Should never reach here
	}

	public boolean hasAuth() {
		return auth != null;
	}

	public boolean isGraph() {
		return graph != null;
	}

	public boolean isRemote() {
		return remote != null;
	}

}
