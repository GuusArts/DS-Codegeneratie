package nl.kik.commons.service;

import java.io.File;
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
import nl.kik.commons.dto.Graph;

@Slf4j
public class RDFService {

	public static final String GRAPH = "graph";
	public static final String COLON = ":";

	/**
	 * @param model
	 */
	public static <M extends Model, G extends Graph<M>> G emitRDF(final G model, final String name) {
		model.beginRead();
		try {
			try (OutputStream f = new FileOutputStream(new File(name))) {
				RDFDataMgr.write(f, model.getModel(), RDFFormat.RDFXML_PRETTY);
			} catch (final IOException e) {
			}
			return model;
		} finally {
			model.end();
		}
	}

	/**
	 * @param model
	 */
	public static <M extends Model, G extends Graph<M>> G emitTTL(final G model, final Path name) {
		model.beginRead();
		try {
			try (OutputStream f = Files.newOutputStream(name)) {
				RDFDataMgr.write(f, model.getModel(), RDFFormat.TTL);
			} catch (final IOException e) {
			}
			return model;
		} finally {
			model.end();
		}
	}

	/**
	 * @return
	 */
	public static String getGraphId(@NotNull final String id) {
		return RDFService.getId(RDFService.GRAPH, id);
	}

	/**
	 * @return
	 */
	public static String getId(@NotNull final String prefix, @NotNull final String id) {
		return prefix + RDFService.COLON + id;
	}

	/**
	 * Removes the "graph" prefix from the provided ID
	 *
	 * @return the
	 */
	public static String removeGraphId(@NotNull final String id) {
		return RDFService.removeId(RDFService.GRAPH, id);
	}

	/**
	 * @return
	 */
	public static String removeId(@NotNull final String prefix, @NotNull final String id) {
		RDFService.log.trace("Resolving {} id {}", prefix, id);
		if (!id.startsWith(prefix + RDFService.COLON))
			throw new IllegalArgumentException(prefix + " id must start with `" + prefix + RDFService.COLON + "'");
		return StringUtils.trimToEmpty(id.substring(prefix.length() + RDFService.COLON.length()));
	}

	protected static <K, V> Map<V, K> reverse(final Map<K, V> input) {
		return input.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
	}

	public static void snapshot(final Graph<? extends Model> m, final boolean raw, final Predicate<Statement> filter) {
		m.beginRead();
		RDFService.log.trace("Stored statements");
		Model model = m.getModel();
		if (raw && model instanceof InfModel) {
			model = ((InfModel) model).getRawModel();
		}
		model.listStatements().forEachRemaining(s -> {
			if (filter == null || filter.test(s)) {
				RDFService.log.info(" - ({}, {}, {})", s.getSubject(), s.getPredicate(), s.getObject());
			}
		});
		m.end();
	}

	public static void addAllURLs(final Graph<? extends Model> g, final Resource resource, final Property property,
			final Collection<? extends URL> list) {
		for (final URL value : CollectionUtils.emptyIfNull(list)) {
			addURL(g, resource, property, value);
		}
	}

	/**
	 * @param g
	 * @param resource
	 * @param object
	 */
	public static void addURL(final Graph<? extends Model> g, final Resource resource, final Property property,
			final URL url) {
		if (url != null) {
			g.getModel().add(resource, property, g.getModel().createResource(url.toString()));
		}
	}

	public static Boolean getBoolean(final MultiValuedMap<Property, RDFNode> properties, final Property p) {
		try {
			return properties.get(p).iterator().next().asLiteral().getBoolean();
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * @param r
	 * @param description
	 * @return
	 */
	public static ZonedDateTime getDateTime(final MultiValuedMap<Property, RDFNode> properties, final Property p) {
		try {
			return getDateTime(properties.get(p).iterator().next());
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * @param r
	 * @param description
	 * @return
	 */
	public static ZonedDateTime getDateTime(RDFNode n) {
		try {
			return ZonedDateTime.parse(n.asLiteral().getString());
		} catch (final Exception e) {
			return null;
		}
	}

	public static Double getDouble(final MultiValuedMap<Property, RDFNode> properties, final Property p) {
		try {
			return properties.get(p).iterator().next().asLiteral().getDouble();
		} catch (final Exception e) {
			return null;
		}
	}

	public static Duration getDuration(final MultiValuedMap<Property, RDFNode> properties, final Property p) {
		try {
			return Duration.parse(properties.get(p).iterator().next().asLiteral().getString());
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * @param <U>
	 * @param l
	 * @param values
	 * @return
	 */
	public static <U extends Enum<U>> U getEnum(final Collection<Resource> l, final Map<Resource, U> values) {
		if (l == null)
			return null;
		final List<U> list = l.stream().map(rr -> {
			final U v = values.get(rr);
			RDFService.log.trace(" - trying {} for {}", rr, v);
			return v;
		}) //
				.filter(Objects::nonNull).collect(Collectors.toList());
		if (list == null || list.isEmpty()) {
			RDFService.log.trace("Enum value not found");
			return null;
		}
		if (list.size() > 1) {
			final Optional<U> first = list.stream().sorted().findFirst();
			RDFService.log.trace("Found too many values {} for enum; using {}", list, first);
			return first.get();
		}
		return list.get(0);
	}

	public static <U extends Enum<U>> U getEnum(final Graph<? extends Model> g, final Resource r, final Property p,
			final Map<Resource, U> values) {
		final U result = getEnum(search(g, r, p, null, Statement::getResource).collect(Collectors.toList()), values);
		RDFService.log.trace("Get enum {} for {} -> {}", p, r, result);
		return result;
	}

	public static <U extends Enum<U>> U getEnum(final MultiValuedMap<Property, RDFNode> map, final Property p,
			final Map<Resource, U> values) {
		final U result = getEnum(map.get(p).stream() //
				.filter(Resource.class::isInstance) //
				.map(Resource.class::cast) //
				.collect(Collectors.toList()), values);
		RDFService.log.trace("Get enum {} -> {}", p, result);
		return result;
	}

	/**
	 * @param r
	 * @param description
	 * @return
	 */
	public static Float getFloat(final MultiValuedMap<Property, RDFNode> properties, final Property p) {
		try {
			return properties.get(p).iterator().next().asLiteral().getFloat();
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * @param r
	 * @param description
	 * @return
	 */
	public static LocalDate getLocalDate(final MultiValuedMap<Property, RDFNode> properties, final Property p) {
		try {
			return LocalDate.parse(properties.get(p).iterator().next().asLiteral().getString());
		} catch (final Exception e) {
			return null;
		}
	}

	public static long getLong(final MultiValuedMap<Property, RDFNode> properties, final Property p) {
		try {
			return Long.parseLong(properties.get(p).iterator().next().asLiteral().getString());
		} catch (final Exception e) {
			return 0;
		}
	}

	/**
	 * @param r
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static MultiValuedMap<Property, RDFNode> getProperties(final Graph<? extends Model> g, final Resource r) {
		return (MultiValuedMap) search(g, r, null, null, s -> s) //
				.collect(HashSetValuedHashMap::new, (b, s) -> b.put(s.getPredicate(), s.getObject()),
						HashSetValuedHashMap::putAll); //
	}

	/**
	 * @param r
	 * @param type
	 * @return
	 */
	public static Resource getResource(final Graph<? extends Model> g, final Resource r, final Property p) {
		g.beginRead();
		try {
			return g.getModel().getProperty(r, p).getResource();
		} catch (final Exception e) {
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
	public static Resource getResource(final MultiValuedMap<Property, RDFNode> properties, final Property p) {
		try {
			return properties.get(p).iterator().next().asResource();
		} catch (final Exception e) {
			return null;
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
	public static <T> Set<T> getSet(final MultiValuedMap<Property, RDFNode> properties, final Property property,
			final Function<RDFNode, T> getter) {
		final Set<T> result = properties.get(property).stream() //
				.map(getter) //
				.filter(Objects::nonNull) //
				.collect(Collectors.toSet());
		if (result.isEmpty())
			return null;
		return result;
	}

	/**
	 * @param r
	 * @param description
	 * @return
	 */
	public static String getString(final Graph<? extends Model> g, final Resource r, final Property p) {
		g.beginRead();
		try {
			return getString(g.getModel().getProperty(r, p).getObject());
		} catch (final Exception e) {
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
	public static String getString(final MultiValuedMap<Property, RDFNode> properties, final Property p) {
		try {
			return getString(properties.get(p).iterator().next());
		} catch (final Exception e) {
			return null;
		}
	}

	public static String getString(final RDFNode node) {
		try {
			return node.asLiteral().getString();
		} catch (final Exception e) {
			return null;
		}
	}

	public static Set<String> getStringSet(final MultiValuedMap<Property, RDFNode> properties,
			final Property property) {
		return getSet(properties, property, RDFService::getString);
	}

	/**
	 * @param r
	 * @param description
	 * @return
	 */
	public static Long getTimestamp(final Graph<? extends Model> g, final Resource r, final Property p) {
		g.beginRead();
		try {
			return g.getModel().getProperty(r, p).getLong();
		} catch (final Exception e) {
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
	public static URL getURL(final MultiValuedMap<Property, RDFNode> properties, final Property p) {
		try {
			return getURL(properties.get(p).iterator().next());
		} catch (final Exception e) {
			return null;
		}
	}

	public static URL getURL(final RDFNode node) {
		try {
			return new URL(node.asResource().getURI());
		} catch (final Exception e) {
			try {
				return new URL(node.asLiteral().getString());
			} catch (final Exception e2) {
				return null;
			}
		}
	}

	public static Set<URL> getURLSet(final MultiValuedMap<Property, RDFNode> properties, final Property p) {
		return getSet(properties, p, RDFService::getURL);
	}

	/**
	 * @param r
	 * @return
	 */
	public static <U> Stream<U> search(final Graph<? extends Model> g, final Resource r, final Property p,
			final Resource o, final Function<Statement, U> extract) {
		g.beginRead();
		try {
			RDFService.log.trace("Search for ({}, {}, {})", r, p, o);
			return new ArrayList<>(g.getModel().listStatements(r, p, o).toList()).stream() //
					.filter(Objects::nonNull) //
					.distinct() //
					.map(s -> {
						try {
							final U v = extract.apply(s);
							RDFService.log.trace(" - {} -> {}", s, v);
							return v;
						} catch (final Exception e) {
							RDFService.log.trace(" - Failed extracting from {}", s);
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
	public static <U> Stream<U> searchLiteral(final Graph<? extends Model> graph, final Resource resource,
			final Property prop, final String value, final Function<Statement, U> extract) {
		graph.beginRead();
		try {
			RDFService.log.trace("Search for ({}, {}, {})", resource, prop, value);
			return new ArrayList<>(graph.getModel().listStatements(resource, prop, value).toList()).stream() //
					.filter(Objects::nonNull) //
					.map(s -> {
						try {
							final U v = extract.apply(s);
							RDFService.log.trace(" - {} -> {}", s, v);
							return v;
						} catch (final Exception e) {
							RDFService.log.trace(" - Failed extracting from {}", s);
							return null;
						}
					}) //
					.filter(Objects::nonNull); //
		} finally {
			graph.end();
		}
	}

}
