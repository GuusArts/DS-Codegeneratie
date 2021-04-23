package nl.kik.datastation.service;

import java.net.URL;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.datatypes.xsd.impl.XSDDateTimeType;
import org.apache.jena.datatypes.xsd.impl.XSDDayTimeDurationType;
import org.apache.jena.datatypes.xsd.impl.XSDDurationType;
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
		saveDetails(g, object);
		return object;
	}

	protected void saveRDFObject(Graph<? extends Model> g, Resource resource, RDFObject object) {
	}

	protected Resource saveDetails(Graph<? extends Model> g, RDFObject object) {
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
			Resource resource = g.getModel().createResource(object.getId());
			saveDetails(g, resource, object);
			g.commit();
			return resource;
		} finally {
			g.end();
		}
	}

	protected abstract void saveDetails(Graph<? extends Model> g, Resource resource, RDFObject object);

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

	protected abstract <U extends RDFObject> U getObject(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource);

	/**
	 * @param g
	 * @param resource
	 * @param object
	 */
	protected void addAll(Graph<? extends Model> g, Resource resource, Property property,
			Collection<? extends RDFObject> list) {
		for (RDFObject object : CollectionUtils.emptyIfNull(list)) {
			addObject(g, resource, property, object);
		}
	}

	protected void addAllProperties(Graph<? extends Model> g, Resource resource, Property property,
			Collection<?> list) {
		for (Object value : CollectionUtils.emptyIfNull(list)) {
			addProperty(g, resource, property, value);
		}
	}

	protected void addAllURLs(Graph<? extends Model> g, Resource resource, Property property,
			Collection<? extends URL> list) {
		for (URL value : CollectionUtils.emptyIfNull(list)) {
			addURL(g, resource, property, value);
		}
	}

	/**
	 * @param g
	 * @param resource
	 * @param property
	 * @param object
	 */
	protected void addObject(Graph<? extends Model> g, Resource resource, Property property, RDFObject object) {
		if (object != null) {
			Resource r = saveDetails(g, object);
			g.getModel().add(resource, property, r);
		}
	}

	protected void addProperty(Graph<? extends Model> g, Resource resource, Property property, Object value) {
		if (value != null) {
			if (value instanceof Duration) {
				g.getModel().add(resource, property,
						g.getModel().createTypedLiteral(value.toString(), new XSDDurationType()));
			} else if (value instanceof ZonedDateTime) {
				g.getModel().add(resource, property,
						g.getModel().createTypedLiteral(value.toString(), new XSDDateTimeType("dateTime")));
			} else {
				g.getModel().add(resource, property, g.getModel().createTypedLiteral(value));
			}
		}
	}

	/**
	 * @param g
	 * @param resource
	 * @param object
	 */
	protected void addURL(Graph<? extends Model> g, Resource resource, Property property, URL url) {
		if (url != null) {
			g.getModel().add(resource, property, g.getModel().createResource(url.toString()));
		}
	}

}
