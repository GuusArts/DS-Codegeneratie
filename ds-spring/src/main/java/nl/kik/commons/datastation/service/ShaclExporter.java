package nl.kik.commons.datastation.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Node_Concrete;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.engine.constraint.ClassConstraint;
import org.apache.jena.shacl.engine.constraint.ClosedConstraint;
import org.apache.jena.shacl.engine.constraint.ConstraintComponentSPARQL;
import org.apache.jena.shacl.engine.constraint.DatatypeConstraint;
import org.apache.jena.shacl.engine.constraint.DisjointConstraint;
import org.apache.jena.shacl.engine.constraint.EqualsConstraint;
import org.apache.jena.shacl.engine.constraint.HasValueConstraint;
import org.apache.jena.shacl.engine.constraint.InConstraint;
import org.apache.jena.shacl.engine.constraint.JLogConstraint;
import org.apache.jena.shacl.engine.constraint.JViolationConstraint;
import org.apache.jena.shacl.engine.constraint.LessThanConstraint;
import org.apache.jena.shacl.engine.constraint.LessThanOrEqualsConstraint;
import org.apache.jena.shacl.engine.constraint.MaxCount;
import org.apache.jena.shacl.engine.constraint.MinCount;
import org.apache.jena.shacl.engine.constraint.NodeKindConstraint;
import org.apache.jena.shacl.engine.constraint.PatternConstraint;
import org.apache.jena.shacl.engine.constraint.QualifiedValueShape;
import org.apache.jena.shacl.engine.constraint.ShAnd;
import org.apache.jena.shacl.engine.constraint.ShNode;
import org.apache.jena.shacl.engine.constraint.ShNot;
import org.apache.jena.shacl.engine.constraint.ShOr;
import org.apache.jena.shacl.engine.constraint.ShXone;
import org.apache.jena.shacl.engine.constraint.SparqlConstraint;
import org.apache.jena.shacl.engine.constraint.StrLanguageIn;
import org.apache.jena.shacl.engine.constraint.StrMaxLengthConstraint;
import org.apache.jena.shacl.engine.constraint.StrMinLengthConstraint;
import org.apache.jena.shacl.engine.constraint.UniqueLangConstraint;
import org.apache.jena.shacl.engine.constraint.ValueMaxExclusiveConstraint;
import org.apache.jena.shacl.engine.constraint.ValueMaxInclusiveConstraint;
import org.apache.jena.shacl.engine.constraint.ValueMinExclusiveConstraint;
import org.apache.jena.shacl.engine.constraint.ValueMinInclusiveConstraint;
import org.apache.jena.shacl.parser.ConstraintVisitor;
import org.apache.jena.shacl.parser.NodeShape;
import org.apache.jena.shacl.parser.PropertyShape;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.parser.ShapeVisitor;
import org.apache.jena.shacl.validation.Severity;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.path.P_Alt;
import org.apache.jena.sparql.path.P_Distinct;
import org.apache.jena.sparql.path.P_FixedLength;
import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Mod;
import org.apache.jena.sparql.path.P_Multi;
import org.apache.jena.sparql.path.P_NegPropSet;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.P_OneOrMoreN;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.P_Shortest;
import org.apache.jena.sparql.path.P_ZeroOrMore1;
import org.apache.jena.sparql.path.P_ZeroOrMoreN;
import org.apache.jena.sparql.path.P_ZeroOrOne;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathVisitor;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

public class ShaclExporter implements ShapeVisitor, ConstraintVisitor, PathVisitor {
	private final Model model;
	private final Map<Node, Resource> resources = new HashMap<>();
	private final Map<Node, Property> properties = new HashMap<>();
	private final Map<String, Pair<String, Resource>> prefixes = new HashMap<>();
	private Resource parent;
	private Resource path;
	private Resource ontology;

	public ShaclExporter(final Model model) {
		this.model = model;
	}

	private void add(final List<Path> result, final Path path) {
		if (path instanceof P_Seq) {
			final P_Seq pathSeq = (P_Seq) path;
			add(result, pathSeq.getLeft());
			add(result, pathSeq.getRight());
		} else {
			result.add(path);
		}
	}

	/**
	 * @param nodeShape
	 * @param nodeResource
	 */
	protected void commonNode(final Shape nodeShape, final Resource nodeResource) {
		if (nodeShape.deactivated()) {
			model.add(nodeResource, toProperty(SHACL.deactivated), model.createTypedLiteral(nodeShape.deactivated()));
		}
		if (nodeShape.getSeverity() != null && nodeShape.getSeverity() != Severity.Violation) {
			model.add(nodeResource, toProperty(SHACL.severity), toProperty(nodeShape.getSeverity().level()));
		}
		nodeShape.getMessages().forEach(m -> model.add(nodeResource, toProperty(SHACL.message), toLiteral(m)));
		nodeShape.getTargets()
				.forEach(t -> model.add(nodeResource, toProperty(t.getTargetType().predicate), toResource(t.getObject())));

		nodeShape.getConstraints().forEach(c -> c.visit(this));
		nodeShape.getPropertyShapes().forEach(p -> p.visit(this));
	}

	public Model export(final Shapes shapes) {
		ontology = shapes.getBase() == null ? model.createResource() : toResource(shapes.getBase());
		if (!CollectionUtils.isEmpty(shapes.getImports())) {
			model.add(ontology, RDF.type, OWL.Ontology);
			shapes.getImports().forEach(i -> model.add(ontology, OWL.imports, toResource(i)));
		}
		if (shapes.getGraph() != null && shapes.getGraph().getPrefixMapping() != null) {
			model.setNsPrefixes(shapes.getGraph().getPrefixMapping());
		}
		shapes.iterator().forEachRemaining(t -> t.visit(this));
		return getModel();
	}

	public Model getModel() {
		return model;
	}

	private List<Path> toList(final P_Seq pathSeq) {
		final List<Path> result = new ArrayList<>();
		add(result, pathSeq);
		Collections.reverse(result);
		return result;
	}

	private Literal toLiteral(final Node n) {
		if (!(n instanceof Node_Concrete) || n instanceof Node_Blank)
			throw new IllegalArgumentException();
		if (n instanceof Node_Literal) {
			final Node_Literal ni = (Node_Literal) n;
			Literal value;
			if (ni.getLiteralDatatype() != null) {
				value = model.createTypedLiteral(ni.getLiteralValue(), ni.getLiteralDatatype());
			} else if (StringUtils.isNotBlank(ni.getLiteralDatatypeURI())) {
				value = model.createTypedLiteral(ni.getLiteralValue(), ni.getLiteralDatatypeURI());
			} else if (StringUtils.isNotBlank(ni.getLiteralLanguage()) && ni.getLiteralValue() instanceof String) {
				value = model.createLiteral((String) ni.getLiteralValue(), ni.getLiteralLanguage());
			} else {
				value = model.createTypedLiteral(ni.getLiteralValue());
			}
			return value;
		}
		if (n instanceof Node_URI)
			throw new IllegalArgumentException();
		else
			throw new IllegalArgumentException();
	}

	private Property toProperty(final Node n) {
		if (properties.containsKey(n))
			return properties.get(n);
		if (!(n instanceof Node_Concrete) || n instanceof Node_Blank || (n instanceof Node_Literal))
			throw new IllegalArgumentException();
		if (n instanceof Node_URI) {
			final Node_URI ni = (Node_URI) n;
			final Property property = model.createProperty(ni.getURI());
			properties.put(ni, property);
			return property;
		} else
			throw new IllegalArgumentException();
	}

	private Resource toResource(final Node n) {
		if (resources.containsKey(n))
			return resources.get(n);
		if (!(n instanceof Node_Concrete))
			throw new IllegalArgumentException();
		if (n instanceof Node_Blank) {
			final Resource resource = model
					.createResource(n.getBlankNodeLabel() == null ? AnonId.create() : AnonId.create(n.getBlankNodeLabel()));
			resources.put(n, resource);
			return resource;
		}
		if (n instanceof Node_Literal)
			throw new IllegalArgumentException();
		if (n instanceof Node_URI) {
			final Node_URI ni = (Node_URI) n;
			final Resource resource = model.createResource(ni.getURI());
			resources.put(ni, resource);
			return resource;
		} else
			throw new IllegalArgumentException();
	}

	private Resource toURI(final String prefix, final String uri) throws URISyntaxException {
		if (prefixes.containsKey(prefix)) {
			final Pair<String, Resource> namespace = prefixes.get(prefix);
			if (!namespace.getLeft().equals(uri))
				return null;
			return namespace.getRight();
		}
		final Resource r = model.createResource();
		model.add(ontology, toProperty(SHACL.declare), r);
		model.add(r, toProperty(SHACL.namespace), model.createTypedLiteral(new URI(uri)));
		model.add(r, toProperty(SHACL.prefix), model.createTypedLiteral(prefix));
		prefixes.put(prefix, Pair.of(uri, r));
		return r;
	}

	@Override
	public void visit(final ClassConstraint constraint) {
		model.add(parent, toProperty(SHACL.class_), toResource(constraint.getExpectedClass()));
	}

	@Override
	public void visit(final ClosedConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final ConstraintComponentSPARQL constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final DatatypeConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final DisjointConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final EqualsConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final HasValueConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final InConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final JLogConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final JViolationConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final LessThanConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final LessThanOrEqualsConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final MaxCount constraint) {
		model.add(parent, toProperty(SHACL.maxCount), model.createTypedLiteral(constraint.getMaxCount()));
	}

	@Override
	public void visit(final MinCount constraint) {
		model.add(parent, toProperty(SHACL.minCount), model.createTypedLiteral(constraint.getMinCount()));
	}

	@Override
	public void visit(final NodeKindConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final NodeShape nodeShape) {
		final Resource savedParent = parent;
		final Resource nodeResource = toResource(nodeShape.getShapeNode());
		parent = nodeResource;

		model.add(nodeResource, RDF.type, toResource(SHACL.NodeShape));
		commonNode(nodeShape, nodeResource);

		parent = savedParent;
	}

	@Override
	public void visit(final P_Alt pathAlt) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final P_Distinct pathDistinct) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final P_FixedLength pFixedLength) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final P_Inverse inversePath) {
		final Resource newPath = model.createResource();
		inversePath.getSubPath().visit(this);
		model.add(newPath, toProperty(SHACL.inversePath), path);
		path = newPath;
	}

	@Override
	public void visit(final P_Link pathNode) {
		path = toResource(pathNode.getNode());
	}

	@Override
	public void visit(final P_Mod pathMod) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final P_Multi pathMulti) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final P_NegPropSet pathNotOneOf) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final P_OneOrMore1 path) {
		final Resource newPath = model.createResource();
		path.getSubPath().visit(this);
		model.add(newPath, toProperty(SHACL.oneOrMorePath), this.path);
		this.path = newPath;
	}

	@Override
	public void visit(final P_OneOrMoreN path) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final P_ReverseLink pathNode) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final P_Seq pathSeq) {
		Resource tail = RDF.nil;
		for (final Path p : toList(pathSeq)) {
			final Resource newTail = model.createResource();
			model.add(newTail, RDF.rest, tail);
			p.visit(this);
			model.add(newTail, RDF.first, path);
			tail = newTail;
		}
		path = tail;
	}

	@Override
	public void visit(final P_Shortest pathShortest) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final P_ZeroOrMore1 path) {
		final Resource newPath = model.createResource();
		path.getSubPath().visit(this);
		model.add(newPath, toProperty(SHACL.zeroOrMorePath), this.path);
		this.path = newPath;
	}

	@Override
	public void visit(final P_ZeroOrMoreN path) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final P_ZeroOrOne path) {
		final Resource newPath = model.createResource();
		path.getSubPath().visit(this);
		model.add(newPath, toProperty(SHACL.zeroOrOnePath), this.path);
		this.path = newPath;
	}

	@Override
	public void visit(final PatternConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final PropertyShape propertyShape) {
		final Resource savedParent = parent;
		final Resource nodeResource = toResource(propertyShape.getShapeNode());
		parent = nodeResource;

		model.add(savedParent, toProperty(SHACL.property), nodeResource);
		model.add(nodeResource, RDF.type, toResource(SHACL.PropertyShape));
		if (propertyShape.getPath() != null) {
			propertyShape.getPath().visit(this);
			model.add(parent, toProperty(SHACL.path), path);
			path = null;
		}
		commonNode(propertyShape, nodeResource);

		parent = savedParent;
	}

	@Override
	public void visit(final QualifiedValueShape constraint) {
		final Resource savedParent = parent;
		final Resource nodeResource = toResource(constraint.getSub().getShapeNode());
		parent = nodeResource;
		if (constraint.qMin() >= 0) {
			model.add(savedParent, toProperty(SHACL.qualifiedMinCount), model.createTypedLiteral(constraint.qMin()));
		}
		if (constraint.qMax() >= 0) {
			model.add(savedParent, toProperty(SHACL.qualifiedMaxCount), model.createTypedLiteral(constraint.qMax()));
		}
		if (constraint.qDisjoint()) {
			model.add(savedParent, toProperty(SHACL.disjoint), model.createTypedLiteral(constraint.qDisjoint()));
		}
		model.add(savedParent, toProperty(SHACL.qualifiedValueShape), nodeResource);
		constraint.getSub().visit(this);
		parent = savedParent;
	}

	@Override
	public void visit(final ShAnd constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final ShNode constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final ShNot constraint) {
		final Resource savedParent = parent;
		final Resource nodeResource = toResource(constraint.getOther().getShapeNode());
		parent = nodeResource;
		model.add(savedParent, toProperty(SHACL.not), nodeResource);
		constraint.getOther().visit(this);
		parent = savedParent;
	}

	@Override
	public void visit(final ShOr constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final ShXone constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final SparqlConstraint constraint) {
		if (constraint.getMessage() != null) {
			model.add(parent, toProperty(SHACL.message), model.createTypedLiteral(constraint.getMessage()));
		}
		final Resource sparql = model.createResource();
		model.add(parent, toProperty(SHACL.sparql), sparql);
		model.add(sparql, RDF.type, toProperty(SHACL.SPARQLConstraint));
		final Query q = constraint.getQuery().cloneQuery();
		final PrefixMapping pm = q.getPrefixMapping();
		for (final Entry<String, String> e : new HashSet<>(pm.getNsPrefixMap().entrySet())) {
			Resource r = null;
			try {
				r = toURI(e.getKey(), e.getValue());
			} catch (final URISyntaxException e1) {
			}
			if (r != null) {
				pm.removeNsPrefix(e.getKey());
				model.add(sparql, toProperty(SHACL.prefixes), model.createResource(e.getValue()));
			}
		}
		q.getPrefixMapping().clearNsPrefixMap();
		switch (q.queryType()) {
		case ASK:
			model.add(sparql, toProperty(SHACL.ask), model.createTypedLiteral(q.toString()));
			break;
		case SELECT:
			model.add(sparql, toProperty(SHACL.select), model.createTypedLiteral(q.toString()));
			break;
		case CONSTRUCT:
		case CONSTRUCT_JSON:
		case CONSTRUCT_QUADS:
		case DESCRIBE:
		case UNKNOWN:
		default:
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void visit(final StrLanguageIn constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final StrMaxLengthConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final StrMinLengthConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final UniqueLangConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final ValueMaxExclusiveConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final ValueMaxInclusiveConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final ValueMinExclusiveConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(final ValueMinInclusiveConstraint constraint) {
		throw new IllegalArgumentException();
	}

}
