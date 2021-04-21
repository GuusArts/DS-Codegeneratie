package nl.kik.datastation.service;

import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import lombok.extern.slf4j.Slf4j;
import nl.kik.datastation.dto.Graph;
import nl.kik.datastation.dto.RDFObject;

@Slf4j
public abstract class AbstractService extends RDFService {
	public <U extends RDFObject> U save(Graph<? extends Model> g, U object) {
		g.beginWrite();
		try {
			String id = object.getId();
			if (id == null) {
				id = getId(getPrefix(), UUID.randomUUID().toString());
				log.trace("Saving {} as {}", object, id);
				object.setId(id);
			} else {
				log.trace("Recreating {}", id);
				delete(g, object);
			}
			saveDetails(g, object);
			g.commit();
			return object;
		} finally {
			g.end();
		}
	}

	protected void saveRDFObject(Graph<? extends Model> g, Resource resource, RDFObject object) {
	}

	protected abstract Resource saveDetails(Graph<? extends Model> g, RDFObject object);

	protected abstract @NotNull String getPrefix();

	/**
	 * @param <U>
	 * @param g
	 * @param object
	 */
	protected void delete(Graph<? extends Model> g, RDFObject object, boolean purge) {
		g.beginWrite();
		try {
			Resource resource = g.getModel().createResource(object.getId());
			g.getModel().removeAll(resource, null, null);
			deleteDetails(g, resource, object, purge);
			if (purge) {
				g.getModel().removeAll(null, null, resource);
			}
			g.commit();
		} finally {
			g.end();
		}
	}

	public void delete(Graph<? extends Model> g, RDFObject object) {
		delete(g, object, false);
	}

	protected abstract void deleteDetails(Graph<? extends Model> g, Resource resource, RDFObject object, boolean purge);
	
	@SuppressWarnings("unchecked")
	public <U extends RDFObject> Optional<U> lookupById(Graph<? extends Model> graph, @NotNull String id) {
		graph.beginRead();
		try {
			return Optional.ofNullable(graph.getModel().getResource(getId(getPrefix(), id))).stream()//
					.map(r -> Pair.of(r, getProperties(graph, r))) //
					.map(p -> Pair.of(p.getRight(), (U) getObject(graph, p.getRight(), p.getLeft()))) //
					.filter(p -> p.getRight() != null) //
					.map(Pair::getRight) //
					.findFirst();
		} finally {
			graph.end();
		}
	}

	protected abstract <U extends RDFObject> U getObject(Graph<? extends Model> graph, MultiValuedMap<Property, RDFNode> properties,
			Resource resource);

}
