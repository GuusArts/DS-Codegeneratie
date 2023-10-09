package nl.kik.commons.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotNull;

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
	/**
	 * @param g
	 * @param resource
	 * @param object
	 */
	protected void addAll(final Graph<? extends Model> g, final Resource resource, final Property property,
			final Collection<? extends RDFObject> list) {
		for (final RDFObject object : CollectionUtils.emptyIfNull(list)) {
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
	protected Statement addObject(final Graph<? extends Model> g, final Resource resource, final Property property,
			final RDFObject object) {
		if (object != null) {
			final Resource r = saveDetails(g, object);
			final Statement s = g.getModel().createStatement(resource, property, r);
			g.getModel().add(s);
			return s;
		}
		return null;
	}

	public void delete(final Graph<? extends Model> g, final RDFObject object) {
		delete(g, object, false);
	}

	/**
	 * @param <U>
	 * @param g
	 * @param object
	 */
	protected void delete(final Graph<? extends Model> g, final RDFObject object, final boolean purge) {
		g.beginWrite();
		try {
			final Resource resource = g.getModel().createResource(object.getId());
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

	protected abstract void deleteDetails(Graph<? extends Model> g, Resource resource, RDFObject object, boolean purge);

	/**
	 * @param types
	 * @param properties
	 * @param resource
	 * @return
	 */
	protected List<Class<? extends RDFObject>> getAllTypes(final Map<Resource, Class<? extends RDFObject>> types,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource) {
		final List<Class<? extends RDFObject>> allTypes = properties.get(RDF.type).stream() //
				.map(types::get) //
				.filter(Objects::nonNull) //
				.collect(Collectors.toList());
		AbstractRDFService.log.trace("For {} found types {}", resource, allTypes);
		return allTypes;
	}

	private Optional<Class<? extends RDFObject>> getMostSpecificType(
			final Map<Resource, Class<? extends RDFObject>> types, final MultiValuedMap<Property, RDFNode> properties,
			final Resource resource) {
		final List<Class<? extends RDFObject>> allTypes = getAllTypes(types, properties, resource);
		final List<Class<? extends RDFObject>> subTypes = allTypes.stream() //
				.filter(t -> allTypes.stream().filter(tt -> t.isAssignableFrom(tt)).count() == 1) //
				.collect(Collectors.toList());
		AbstractRDFService.log.trace("For {} found sub types {}", resource, subTypes);
		if (subTypes.size() == 1) {
			return Optional.ofNullable(subTypes.get(0));
		}
		AbstractRDFService.log.warn("For {} found sub types {}", resource, subTypes);
		return Optional.empty();
	}

	protected <T extends RDFObject> T getObject(final L graph, Map<Resource, RDFObject> existing,
			final MultiValuedMap<Property, RDFNode> properties, final Property property, final Class<T> clazz) {
		final List<T> result = properties.get(property).stream() //
				.map(n -> getObject(graph, existing, n, clazz)) //
				.filter(Objects::nonNull) //
				.collect(Collectors.toList());
		if (result.size() != 1) {
			if (result.size() > 1) {
				AbstractRDFService.log.warn("Expected unique object for {} but found {}", property, result);
			}
			return null;
		}
		return result.get(0);
	}

	@SuppressWarnings("unchecked")
	protected <U extends RDFObject> U getObject(final L graph, Map<Resource, RDFObject> existing,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource) {
		RDFObject e = existing.get(resource);
		if (e != null) {
			return (U) e;
		}
		e = getMostSpecificType(getObjectTypes(), properties, resource).map(t -> {
			AbstractRDFService.log.trace("Loading {} of type {}", resource, t);
			return getObject(graph, existing, properties, resource, (Class<U>) t);
		}).orElse(null);
		if (e != null) {
			existing.put(resource, e);
		}
		return (U) e;
	}

//	protected <U extends RDFObject> U getObject(final L graph, final MultiValuedMap<Property, RDFNode> properties,
//			final Resource resource) {
//		return getObject(graph, new HashMap<>(), properties, resource);
//	}

	protected abstract <U extends RDFObject> U getObject(L graph, Map<Resource, RDFObject> existing,
			MultiValuedMap<Property, RDFNode> properties, Resource resource, Class<U> t);

	protected <T extends RDFObject> T getObject(final L graph, Map<Resource, RDFObject> existing, final RDFNode n,
			final Class<T> clazz) {
		return Optional.ofNullable(n) //
				.filter(RDFNode::isResource) //
				.map(RDFNode::asResource) //
				.filter(Resource::isURIResource) //
				.map(r -> lookupById(graph, existing, r.getURI())) //
				.filter(Optional::isPresent) //
				.map(Optional::get) //
				.filter(clazz::isInstance) //
				.map(clazz::cast) //
				.orElse(null);
	}

	protected abstract Map<Resource, Class<? extends RDFObject>> getObjectTypes();

	protected abstract @NotNull String getPrefix();

	protected MultiValuedMap<Property, RDFNode> getProperties(final L g, final Resource r) {
		if (g instanceof Graph) {
			return RDFService.getProperties((Graph<?>) g, r);
		}
		throw new IllegalArgumentException();
	}

	@SuppressWarnings("unchecked")
	protected <B extends RDFObjectBuilder<?, ?>> B getRDFObject(final L graph,
			final MultiValuedMap<Property, RDFNode> properties, final Resource resource, final B builder) {
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
	protected <T extends RDFObject> Set<T> getSet(final L graph, Map<Resource, RDFObject> existing,
			final MultiValuedMap<Property, RDFNode> properties, final Property property, final Class<T> clazz) {
		return RDFService.getSet(properties, property, n -> getObject(graph, existing, n, clazz));
	}

	@SuppressWarnings("unchecked")
	public <U extends RDFObject> Optional<U> lookupById(final L graph, Map<Resource, RDFObject> existing,
			@NotNull final String id) {
		graph.beginRead();
		try {
			return Optional.ofNullable(graph.getResource(id)).stream()//
					.map(r -> Pair.of(r, getProperties(graph, r))) //
					.map(p -> Pair.of(p.getRight(), (U) getObject(graph, existing, p.getRight(), p.getLeft()))) //
					.filter(p -> p.getRight() != null) //
					.map(Pair::getRight) //
					.findFirst();
		} finally {
			graph.end();
		}
	}

	public <U extends RDFObject> U save(final Graph<? extends Model> g, final U object) {
		saveDetails(g, object);
		return object;
	}

	protected Resource saveDetails(final Graph<? extends Model> g, final RDFObject object) {
		g.beginWrite();
		try {
			String id = object.getId();
			if (id == null) {
				id = RDFService.getId(getPrefix(), UUID.randomUUID().toString());
				AbstractRDFService.log.trace("Saving {} as {}", object, id);
				object.setId(id);
			} else {
				AbstractRDFService.log.trace("Recreating {}", id);
				delete(g, object);
			}
			final Resource resource = g.getModel().createResource(object.getId());
			saveDetails(g, resource, object);
			g.commit();
			return resource;
		} finally {
			g.end();
		}
	}

	protected abstract void saveDetails(Graph<? extends Model> g, Resource resource, RDFObject object);

	protected void saveRDFObject(final Graph<? extends Model> g, final Resource resource, final RDFObject object) {
	}

}
