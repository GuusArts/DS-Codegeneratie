package nl.kik.datastation.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import lombok.extern.slf4j.Slf4j;
import nl.kik.datastation.dto.Graph;

@Slf4j
public class RDFService {

	public static final String GRAPH = "graph";
	public static final String COLON = ":";

	/**
	 * @return
	 */
	public static String getId(@NotNull String prefix, @NotNull String id) {
		return prefix + COLON + id;
	}

	/**
	 * @return
	 */
	public static String getGraphId(@NotNull String id) {
		return getId(GRAPH, id);
	}

	/**
	 * @return
	 */
	public static String removeId(@NotNull String prefix, @NotNull String id) {
		log.trace("Resolving {} id {}", prefix, id);
		if (!id.startsWith(prefix + COLON)) {
			throw new IllegalArgumentException(prefix + " id must start with `" + prefix + COLON + "'");
		}
		return StringUtils.trimToEmpty(id.substring(prefix.length() + COLON.length()));
	}

	/**
	 * Removes the "graph" prefix from the provided ID
	 * 
	 * @return the
	 */
	public static String removeGraphId(@NotNull String id) {
		return removeId(GRAPH, id);
	}

	/**
	 * @param model
	 */
	public static <M extends Model, G extends Graph<M>> G emitRDF(G model, String name) {
		model.beginRead();
		try {
			try (OutputStream f = new FileOutputStream(new File(name))) {
				RDFDataMgr.write(f, model.getModel(), RDFFormat.RDFXML_PRETTY);
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			return model;
		} finally {
			model.end();
		}
	}

	/**
	 * @param model
	 */
	public static <M extends Model, G extends Graph<M>> G emitTTL(G model, Path name) {
		model.beginRead();
		try {
			try (OutputStream f = Files.newOutputStream(name)) {
				RDFDataMgr.write(f, model.getModel(), RDFFormat.TTL);
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			return model;
		} finally {
			model.end();
		}
	}

	/**
	 * @param r
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected MultiValuedMap<Property, RDFNode> getProperties(Graph<? extends Model> g, Resource r) {
		return (MultiValuedMap) search(g, r, null, null, s -> s) //
				.collect(HashSetValuedHashMap::new, (b, s) -> b.put(s.getPredicate(), s.getObject()),
						(b1, b2) -> b1.putAll(b2)); //
	}

	/**
	 * @param <U>
	 * @param l
	 * @param values
	 * @return
	 */
	protected <U extends Enum<U>> U getEnum(Collection<Resource> l, Map<Resource, U> values) {
		if (l == null) {
			return null;
		}
		List<U> list = l.stream().map(rr -> {
			U v = values.get(rr);
			log.trace(" - trying {} for {}", rr, v);
			return v;
		}) //
				.filter(Objects::nonNull).collect(Collectors.toList());
		if (list == null || list.isEmpty()) {
			log.trace("Enum value not found");
			return null;
		}
		if (list.size() > 1) {
			Optional<U> first = list.stream().sorted().findFirst();
			log.trace("Found too many values {} for enum; using {}", list, first);
			return first.get();
		}
		return list.get(0);
	}

	public <U extends Enum<U>> U getEnum(Graph<? extends Model> g, Resource r, Property p, Map<Resource, U> values) {
		final U result = getEnum(search(g, r, p, null, Statement::getResource).collect(Collectors.toList()), values);
		log.trace("Get enum {} for {} -> {}", p, r, result);
		return result;
	}

	public <U extends Enum<U>> U getEnum(MultiValuedMap<Property, RDFNode> map, Property p, Map<Resource, U> values) {
		final U result = getEnum(map.get(p).stream() //
				.filter(Resource.class::isInstance) //
				.map(Resource.class::cast) //
				.collect(Collectors.toList()), values);
		log.trace("Get enum {} -> {}", p, result);
		return result;
	}

	/**
	 * @param r
	 * @param type
	 * @return
	 */
	public Resource getResource(Graph<? extends Model> g, Resource r, Property p) {
		g.beginRead();
		try {
			return g.getModel().getProperty(r, p).getResource();
		} catch (Exception e) {
			return null;
		} finally {
			g.end();
		}
	}

	/**
	 * @param r
	 * @param type
	 * @return
	 */
	public Resource getResource(MultiValuedMap<Property, RDFNode> properties, Property p) {
		try {
			return properties.get(p).iterator().next().asResource();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param r
	 * @param description
	 * @return
	 */
	public String getString(Graph<? extends Model> g, Resource r, Property p) {
		g.beginRead();
		try {
			return getString(g.getModel().getProperty(r, p).getObject());
		} catch (Exception e) {
			return null;
		} finally {
			g.end();
		}
	}

	/**
	 * @param r
	 * @param description
	 * @return
	 */
	public String getString(MultiValuedMap<Property, RDFNode> properties, Property p) {
		try {
			return getString(properties.get(p).iterator().next());
		} catch (Exception e) {
			return null;
		}
	}

	protected String getString(RDFNode node) {
		try {
			return node.asLiteral().getString();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param r
	 * @param description
	 * @return
	 */
	public URL getURL(MultiValuedMap<Property, RDFNode> properties, Property p) {
		try {
			return getURL(properties.get(p).iterator().next());
		} catch (Exception e) {
			return null;
		}
	}

	protected URL getURL(RDFNode node) {
		try {
			return new URL(node.asResource().getURI());
		} catch (Exception e) {
			try {
				return new URL(node.asLiteral().getString());
			} catch (Exception e2) {
				return null;
			}
		}
	}

	public Set<URL> getURLSet(MultiValuedMap<Property, RDFNode> properties, Property p) {
		return getSet(properties, p, this::getURL);
	}

	/**
	 * @param r
	 * @param description
	 * @return
	 */
	public Float getFloat(MultiValuedMap<Property, RDFNode> properties, Property p) {
		try {
			return properties.get(p).iterator().next().asLiteral().getFloat();
		} catch (Exception e) {
			return null;
		}
	}
	public Double getDouble(MultiValuedMap<Property, RDFNode> properties, Property p) {
		try {
			return properties.get(p).iterator().next().asLiteral().getDouble();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param r
	 * @param description
	 * @return
	 */
	public ZonedDateTime getDateTime(MultiValuedMap<Property, RDFNode> properties, Property p) {
		try {
			return ZonedDateTime.parse(properties.get(p).iterator().next().asLiteral().getString());
		} catch (Exception e) {
			return null;
		}
	}
	public Duration getDuration(MultiValuedMap<Property, RDFNode> properties, Property p) {
		try {
			return Duration.parse(properties.get(p).iterator().next().asLiteral().getString());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param r
	 * @param description
	 * @return
	 */
	public LocalDate getLocalDate(MultiValuedMap<Property, RDFNode> properties, Property p) {
		try {
			return LocalDate.parse(properties.get(p).iterator().next().asLiteral().getString());
		} catch (Exception e) {
			return null;
		}
	}

	public long getLong(MultiValuedMap<Property, RDFNode> properties, Property p) {
		try {
			return Long.valueOf(properties.get(p).iterator().next().asLiteral().getString());
		} catch (Exception e) {
			return 0;
		}
	}

	public Boolean getBoolan(MultiValuedMap<Property, RDFNode> properties, Property p) {
		try {
			return properties.get(p).iterator().next().asLiteral().getBoolean();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param r
	 * @param description
	 * @return
	 */
	public Long getTimestamp(Graph<? extends Model> g, Resource r, Property p) {
		g.beginRead();
		try {
			return g.getModel().getProperty(r, p).getLong();
		} catch (Exception e) {
			return null;
		} finally {
			g.end();
		}
	}

	/**
	 * @param r
	 * @return
	 */
	public <U> Stream<U> search(Graph<? extends Model> g, Resource r, Property p, Resource o,
			Function<Statement, U> extract) {
		g.beginRead();
		try {
			log.trace("Search for ({}, {}, {})", r, p, o);
			return new ArrayList<>(g.getModel().listStatements(r, p, o).toList()).stream() //
					.filter(Objects::nonNull) //
					.distinct() //
					.map(s -> {
						try {
							U v = extract.apply(s);
							log.trace(" - {} -> {}", s, v);
							return v;
						} catch (Exception e) {
							log.trace(" - Failed extracting from {}", s);
							return null;
						}
					}) //
					.filter(Objects::nonNull); //
		} finally {
			g.end();
		}
	}

	/**
	 * 
	 * @param <U>
	 * @param graph    the graph to search
	 * @param resource the resource
	 * @param prop     the property to search
	 * @param l        the literal value to search
	 * @param extract
	 * @return
	 */
	public <U> Stream<U> searchLiteral(Graph<? extends Model> graph, Resource resource, Property prop, String value,
			Function<Statement, U> extract) {
		graph.beginRead();
		try {
			log.trace("Search for ({}, {}, {})", resource, prop, value);
			return new ArrayList<>(graph.getModel().listStatements(resource, prop, value).toList()).stream() //
					.filter(Objects::nonNull) //
					.map(s -> {
						try {
							U v = extract.apply(s);
							log.trace(" - {} -> {}", s, v);
							return v;
						} catch (Exception e) {
							log.trace(" - Failed extracting from {}", s);
							return null;
						}
					}) //
					.filter(Objects::nonNull); //
		} finally {
			graph.end();
		}
	}

	public static void snapshot(Graph<? extends Model> m, boolean raw, Predicate<Statement> filter) {
		m.beginRead();
		log.trace("Stored statements");
		Model model = m.getModel();
		if (raw && model instanceof InfModel) {
			model = ((InfModel) model).getRawModel();
		}
		model.listStatements().forEachRemaining(s -> {
			if (filter == null || filter.test(s)) {
				log.info(" - ({}, {}, {})", s.getSubject(), s.getPredicate(), s.getObject());
			}
		});
		m.end();
	}

	protected static <K, V> Map<V, K> reverse(Map<K, V> input) {
		return input.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
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
	 * @param object
	 */
	protected void addURL(Graph<? extends Model> g, Resource resource, Property property, URL url) {
		if (url != null) {
			g.getModel().add(resource, property, g.getModel().createResource(url.toString()));
		}
	}

	/**
	 * @param <T>
	 * @param graph
	 * @param properties
	 * @param property
	 * @param clazz
	 * @return
	 */
	protected <T> Set<T> getSet(MultiValuedMap<Property, RDFNode> properties, Property property,
			Function<RDFNode, T> getter) {
		Set<T> result = properties.get(property).stream() //
				.map(getter) //
				.filter(Objects::nonNull) //
				.collect(Collectors.toSet());
		if (result.isEmpty()) {
			return null;
		}
		return result;
	}

	protected Set<String> getStringSet(MultiValuedMap<Property, RDFNode> properties, Property property) {
		return getSet(properties, property, this::getString);
	}

}
