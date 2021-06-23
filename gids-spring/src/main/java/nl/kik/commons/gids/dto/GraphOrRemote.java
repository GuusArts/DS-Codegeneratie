package nl.kik.commons.gids.dto;

import java.util.Objects;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import nl.kik.commons.dto.Graph;

public class GraphOrRemote implements nl.kik.commons.dto.Source {
	private final Graph<? extends Model> graph;
	private final String remote;

	public GraphOrRemote(Graph<? extends Model> graph) {
		Objects.requireNonNull(graph);
		this.graph = graph;
		this.remote = null;
	}

	public GraphOrRemote(String remote) {
		Objects.requireNonNull(remote);
		this.graph = null;
		this.remote = remote;
	}

	public boolean isGraph() {
		return graph != null;
	}

	public boolean isRemote() {
		return remote != null;
	}

	@Override
	public void beginWrite() {
		if (isGraph()) {
			getGraph().beginWrite();
		}
	}

	@Override
	public void beginRead() {
		if (isGraph()) {
			getGraph().beginRead();
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

	@Override
	public Resource getResource(String uri) {
		if (isGraph()) {
			return getGraph().getResource(uri);
		}
		if (isRemote()) {
			return new ResourceImpl(uri);
		}
		throw new IllegalArgumentException(); // Should never reach here
	}

	public String getRemote() {
		return remote;
	}

	public Graph<? extends Model> getGraph() {
		return graph;
	}

	public Resource getReified(Resource s, Property p, RDFNode o) {
		if (isGraph()) {
			return getGraph().getModel().getAnyReifiedStatement(getGraph().getModel().createStatement(s, p, o));
		}
		if (isRemote()) {
			throw new NotImplementedException(); // TODO
		}
		throw new IllegalArgumentException(); // Should never reach here
	}

}
