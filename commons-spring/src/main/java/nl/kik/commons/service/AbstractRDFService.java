package nl.kik.commons.service;

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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.dto.Graph;
import nl.kik.commons.dto.RDFObject;
import nl.kik.commons.dto.RDFObject.RDFObjectBuilder;
import nl.kik.commons.dto.Source;

@Slf4j
public abstract class AbstractRDFService<L extends Source> extends RDFService {
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
	public <U extends RDFObject> Optional<U> lookupById(L graph, @NotNull String id) {
		graph.beginRead();
		try {
			return Optional.ofNullable(graph.getResource(id)).stream()//
					.map(r -> Pair.of(r, getProperties(graph, r))) //
					.map(p -> Pair.of(p.getRight(), (U) getObject(graph, p.getRight(), p.getLeft()))) //
					.filter(p -> p.getRight() != null) //
					.map(Pair::getRight) //
					.findFirst();
		} finally {
			graph.end();
		}
	}

	protected MultiValuedMap<Property, RDFNode> getProperties(L g, final Resource r) {
		if (g instanceof Graph) {
			return RDFService.getProperties((Graph<?>) g, r);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@SuppressWarnings("unchecked")
	protected <U extends RDFObject> U getObject(L graph, MultiValuedMap<Property, RDFNode> properties,
			Resource resource) {
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

	protected abstract <U extends RDFObject> U getObject(L graph, MultiValuedMap<Property, RDFNode> properties,
			Resource resource, Class<U> t);

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

	/**
	 * @param g
	 * @param resource
	 * @param property
	 * @param object
	 * @return
	 */
	protected Statement addObject(Graph<? extends Model> g, Resource resource, Property property, RDFObject object) {
		if (object != null) {
			Resource r = saveDetails(g, object);
			Statement s = g.getModel().createStatement(resource, property, r);
			g.getModel().add(s);
			return s;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected <B extends RDFObjectBuilder<?, ?>> B getRDFObject(L graph, MultiValuedMap<Property, RDFNode> properties,
			Resource resource, B builder) {
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
	protected <T extends RDFObject> Set<T> getSet(L graph, MultiValuedMap<Property, RDFNode> properties,
			Property property, Class<T> clazz) {
		return getSet(properties, property, n -> getObject(graph, n, clazz));
	}

	protected <T extends RDFObject> T getObject(L graph, MultiValuedMap<Property, RDFNode> properties, Property property,
			Class<T> clazz) {
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

	protected <T extends RDFObject> T getObject(L graph, RDFNode n, Class<T> clazz) {
		return Optional.ofNullable(n) //
				.filter(RDFNode::isResource) //
				.map(RDFNode::asResource) //
				.filter(Resource::isURIResource) //
				.map(r -> lookupById(graph, r.getURI())) //
				.filter(Optional::isPresent) //
				.map(Optional::get) //
				.filter(clazz::isInstance) //
				.map(clazz::cast) //
				.orElse(null);
	}

}
