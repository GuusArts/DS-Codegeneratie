package nl.kik.commons.datastation.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
	private Map<Node, Resource> resources = new HashMap<>();
	private Map<Node, Property> properties = new HashMap<>();
	private Resource parent;
	private Resource path;

	public ShaclExporter(Model model) {
		this.model = model;
	}

	public Model export(Shapes shapes) {
		System.out.println("base: " + shapes.getBase());
		if (!CollectionUtils.isEmpty(shapes.getImports())) {
			Resource ontology = shapes.getBase() == null ? model.createResource() : toResource(shapes.getBase());
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

	@Override
	public void visit(NodeShape nodeShape) {
		Resource savedParent = parent;
		Resource nodeResource = toResource(nodeShape.getShapeNode());
		parent = nodeResource;

		model.add(nodeResource, RDF.type, toResource(SHACL.NodeShape));
		commonNode(nodeShape, nodeResource);

		parent = savedParent;
	}

	/**
	 * @param nodeShape
	 * @param nodeResource
	 */
	protected void commonNode(Shape nodeShape, Resource nodeResource) {
		if (nodeShape.deactivated()) {
			model.add(nodeResource, toProperty(SHACL.deactivated), model.createTypedLiteral(nodeShape.deactivated()));
		}
		if (nodeShape.getSeverity() != null && nodeShape.getSeverity() != Severity.Violation) {
			model.add(nodeResource, toProperty(SHACL.severity), toProperty(nodeShape.getSeverity().level()));
		}
		nodeShape.getMessages().forEach(m -> model.add(nodeResource, toProperty(SHACL.message), toLiteral(m)));
		nodeShape.getTargets().forEach(
				t -> model.add(nodeResource, toProperty(t.getTargetType().predicate), toResource(t.getObject())));

		nodeShape.getConstraints().forEach(c -> c.visit(this));
		nodeShape.getPropertyShapes().forEach(p -> p.visit(this));
	}

	@Override
	public void visit(PropertyShape propertyShape) {
		Resource savedParent = parent;
		Resource nodeResource = toResource(propertyShape.getShapeNode());
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

	private Resource toResource(Node n) {
		if (resources.containsKey(n)) {
			return resources.get(n);
		}
		if (n instanceof Node_Concrete) {
			if (n instanceof Node_Blank) {
				Resource resource = model.createResource(
						n.getBlankNodeLabel() == null ? AnonId.create() : AnonId.create(n.getBlankNodeLabel()));
				resources.put(n, resource);
				return resource;
			} else if (n instanceof Node_Literal) {
				throw new IllegalArgumentException();
			} else if (n instanceof Node_URI) {
				Node_URI ni = (Node_URI) n;
				Resource resource = model.createResource(ni.getURI());
				resources.put(ni, resource);
				return resource;
			} else {
				throw new IllegalArgumentException();
			}
		} else {
			throw new IllegalArgumentException();
		}
	}

	private Property toProperty(Node n) {
		if (properties.containsKey(n)) {
			return properties.get(n);
		}
		if (n instanceof Node_Concrete) {
			if (n instanceof Node_Blank) {
				throw new IllegalArgumentException();
			} else if (n instanceof Node_Literal) {
				throw new IllegalArgumentException();
			} else if (n instanceof Node_URI) {
				Node_URI ni = (Node_URI) n;
				Property property = model.createProperty(ni.getURI());
				properties.put(ni, property);
				return property;
			} else {
				throw new IllegalArgumentException();
			}
		} else {
			throw new IllegalArgumentException();
		}
	}

	private Literal toLiteral(Node n) {
		if (n instanceof Node_Concrete) {
			if (n instanceof Node_Blank) {
				throw new IllegalArgumentException();
			} else if (n instanceof Node_Literal) {
				Node_Literal ni = (Node_Literal) n;
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
			} else if (n instanceof Node_URI) {
				throw new IllegalArgumentException();
			} else {
				throw new IllegalArgumentException();
			}
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void visit(ClassConstraint constraint) {
		model.add(parent, toProperty(SHACL.class_), toResource(constraint.getExpectedClass()));
	}

	@Override
	public void visit(DatatypeConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(NodeKindConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(MinCount constraint) {
		model.add(parent, toProperty(SHACL.minCount), model.createTypedLiteral(constraint.getMinCount()));
	}

	@Override
	public void visit(MaxCount constraint) {
		model.add(parent, toProperty(SHACL.maxCount), model.createTypedLiteral(constraint.getMaxCount()));
	}

	@Override
	public void visit(ValueMinExclusiveConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(ValueMinInclusiveConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(ValueMaxInclusiveConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(ValueMaxExclusiveConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(StrMinLengthConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(StrMaxLengthConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(PatternConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(StrLanguageIn constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(UniqueLangConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(EqualsConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(DisjointConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(LessThanConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(LessThanOrEqualsConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(ShNot constraint) {
		Resource savedParent = parent;
		Resource nodeResource = toResource(constraint.getOther().getShapeNode());
		parent = nodeResource;
		model.add(savedParent, toProperty(SHACL.not), nodeResource);
		constraint.getOther().visit(this);
		parent = savedParent;
	}

	@Override
	public void visit(ShAnd constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(ShOr constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(ShXone constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(ShNode constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(QualifiedValueShape constraint) {
		Resource savedParent = parent;
		Resource nodeResource = toResource(constraint.getSub().getShapeNode());
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
	public void visit(ClosedConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(HasValueConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(InConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(ConstraintComponentSPARQL constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(SparqlConstraint constraint) {
		if (constraint.getMessage() != null) {
			model.add(parent, toProperty(SHACL.message), model.createTypedLiteral(constraint.getMessage()));
		}
		Resource sparql = model.createResource();
		model.add(parent, toProperty(SHACL.sparql), sparql);
		model.add(parent, RDF.type, toProperty(SHACL.SPARQLConstraint));
		Query q = constraint.getQuery().cloneQuery();
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
	public void visit(JViolationConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(JLogConstraint constraint) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(P_Link pathNode) {
		path = toResource(pathNode.getNode());
	}

	@Override
	public void visit(P_ReverseLink pathNode) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(P_NegPropSet pathNotOneOf) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(P_Inverse inversePath) {
		Resource newPath = model.createResource();
		inversePath.getSubPath().visit(this);
		model.add(newPath, toProperty(SHACL.inversePath), path);
		path = newPath;
	}

	@Override
	public void visit(P_Mod pathMod) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(P_FixedLength pFixedLength) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(P_Distinct pathDistinct) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(P_Multi pathMulti) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(P_Shortest pathShortest) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(P_ZeroOrOne path) {
		Resource newPath = model.createResource();
		path.getSubPath().visit(this);
		model.add(newPath, toProperty(SHACL.zeroOrOnePath), this.path);
		this.path = newPath;
	}

	@Override
	public void visit(P_ZeroOrMore1 path) {
		Resource newPath = model.createResource();
		path.getSubPath().visit(this);
		model.add(newPath, toProperty(SHACL.zeroOrMorePath), this.path);
		this.path = newPath;
	}

	@Override
	public void visit(P_ZeroOrMoreN path) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(P_OneOrMore1 path) {
		Resource newPath = model.createResource();
		path.getSubPath().visit(this);
		model.add(newPath, toProperty(SHACL.oneOrMorePath), this.path);
		this.path = newPath;
	}

	@Override
	public void visit(P_OneOrMoreN path) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(P_Alt pathAlt) {
		throw new IllegalArgumentException();
	}

	@Override
	public void visit(P_Seq pathSeq) {
		Resource tail = RDF.nil;
		for (Path p : toList(pathSeq)) {
			Resource newTail = model.createResource();
			model.add(newTail, RDF.rest, tail);
			p.visit(this);
			model.add(newTail, RDF.first, path);
			tail = newTail;
		}
		path = tail;
	}

	private List<Path> toList(P_Seq pathSeq) {
		List<Path> result = new ArrayList<>();
		add(result, pathSeq);
		Collections.reverse(result);
		return result;
	}

	private void add(List<Path> result, Path path) {
		if (path instanceof P_Seq) {
			P_Seq pathSeq = (P_Seq) path;
			add(result, pathSeq.getLeft());
			add(result, pathSeq.getRight());
		} else {
			result.add(path);
		}
	}

}
