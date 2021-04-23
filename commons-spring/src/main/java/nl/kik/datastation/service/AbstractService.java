package nl.kik.datastation.service;

import java.net.URL;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.datatypes.xsd.impl.XSDDateTimeType;
import org.apache.jena.datatypes.xsd.impl.XSDDurationType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import lombok.extern.slf4j.Slf4j;
import nl.kik.datastation.dto.Graph;
import nl.kik.datastation.dto.RDFObject;
import nl.kik.datastation.dto.RDFObject.RDFObjectBuilder;

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
			return Optional.ofNullable(graph.getModel().getResource(id)).stream()//
					.map(r -> Pair.of(r, getProperties(graph, r))) //
					.map(p -> Pair.of(p.getRight(), (U) getObject(graph, p.getRight(), p.getLeft()))) //
					.filter(p -> p.getRight() != null) //
					.map(Pair::getRight) //
					.findFirst();
		} finally {
			graph.end();
		}
	}

	@SuppressWarnings("unchecked")
	protected <U extends RDFObject> U getObject(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource) {
		return getMostSpecificType(getObjectTypes(), properties, resource).map(t -> {
			log.trace("Loading {} of type {}", resource, t);
			return getObject(graph, properties, resource, (Class<U>) t);
		}).orElse(null);
	}

	private Optional<Class<? extends RDFObject>> getMostSpecificType(Map<Resource, Class<? extends RDFObject>> types,
			MultiValuedMap<Property, RDFNode> properties, Resource resource) {
		List<Class<? extends RDFObject>> allTypes = getAllTypes(types, properties, resource);
		List<Class<? extends RDFObject>> subTypes = allTypes.stream() //
				.filter(t -> allTypes.stream().filter(tt -> t.isAssignableFrom(tt)).count() == 1) //
				.collect(Collectors.toList());
		log.trace("For {} found sub types {}", resource, subTypes);
		if (subTypes.size() == 1) {
			return Optional.ofNullable(subTypes.get(0));
		}
		log.warn("For {} found sub types {}", resource, subTypes);
		return Optional.empty();
	}

	/**
	 * @param types
	 * @param properties
	 * @param resource
	 * @return
	 */
	protected List<Class<? extends RDFObject>> getAllTypes(Map<Resource, Class<? extends RDFObject>> types,
			MultiValuedMap<Property, RDFNode> properties, Resource resource) {
		List<Class<? extends RDFObject>> allTypes = properties.get(RDF.type).stream() //
				.map(types::get) //
				.filter(Objects::nonNull) //
				.collect(Collectors.toList());
		log.trace("For {} found types {}", resource, allTypes);
		return allTypes;
	}

	protected abstract Map<Resource, Class<? extends RDFObject>> getObjectTypes();

	protected abstract <U extends RDFObject> U getObject(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, Class<U> t);

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
			if (value instanceof URL) {
				log.warn("URLs should be added using addURL for {}", property);
			}
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


	@SuppressWarnings("unchecked")
	protected <B extends RDFObjectBuilder<?, ?>> B getRDFObject(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, B builder) {
		return (B) builder //
				.id(resource.getURI()) //
		;
	}
	/**
	 * @param graph
	 * @param properties
	 * @param member
	 * @return
	 */
	protected <T extends RDFObject> Set<T> getSet(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Property property, Class<T> clazz) {
		return getSet(properties, property, n -> getObject(graph, n, clazz));
	}

	protected <T extends RDFObject> T getObject(Graph<? extends Model> graph,
			MultiValuedMap<Property, RDFNode> properties, Property property, Class<T> clazz) {
		List<T> result = properties.get(property).stream() //
				.map(n -> getObject(graph, n, clazz)) //
				.filter(Objects::nonNull) //
				.collect(Collectors.toList());
		if (result.size() != 1) {
			if (result.size() > 1) {
				log.warn("Expected unique object for {} but found {}", property, result);
			}
			return null;
		}
		return result.get(0);
	}

	protected <T extends RDFObject> T getObject(Graph<? extends Model> graph, RDFNode n, Class<T> clazz) {
		return Optional.ofNullable(n) //
				.filter(RDFNode::isResource) //
				.map(r -> lookupById(graph, r.asResource().getURI())) //
				.filter(Optional::isPresent) //
				.map(Optional::get) //
				.filter(clazz::isInstance) //
				.map(clazz::cast) //
				.orElse(null);
	}

}
