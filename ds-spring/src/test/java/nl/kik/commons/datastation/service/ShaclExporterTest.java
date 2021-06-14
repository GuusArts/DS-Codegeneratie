package nl.kik.commons.datastation.service;

import java.util.Collection;
import java.util.function.BiFunction;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.engine.constraint.ClassConstraint;
import org.apache.jena.shacl.engine.constraint.ClosedConstraint;
import org.apache.jena.shacl.engine.constraint.ConstraintComponentSPARQL;
import org.apache.jena.shacl.engine.constraint.ConstraintOp1;
import org.apache.jena.shacl.engine.constraint.ConstraintOpN;
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
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.ConstraintVisitor;
import org.apache.jena.shacl.parser.PropertyShape;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.sparql.path.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cedarsoftware.util.DeepEquals;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class ShaclExporterTest {
	public final class EqualsVisitor implements ConstraintVisitor {
		private boolean result;
		private final Constraint other;

		public EqualsVisitor(final Constraint a, final Constraint b) {
			other = b;
			a.visit(this);
		}

		private boolean same(final ConstraintOp1 a, final Constraint b) {
			if (a.getClass() != b.getClass())
				return false;
			return compare(a.getOther(), ((ConstraintOp1) b).getOther());
		}

		private boolean same(final ConstraintOpN a, final Constraint b) {
			if (a.getClass() != b.getClass())
				return false;
			return compare(a.getOthers(), ((ConstraintOpN) b).getOthers(), ShaclExporterTest.this::compare);
		}

		public boolean value() {
			return result;
		}

		@Override
		public void visit(final ClassConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final ClosedConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final ConstraintComponentSPARQL constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final DatatypeConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final DisjointConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final EqualsConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final HasValueConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final InConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final JLogConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final JViolationConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final LessThanConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final LessThanOrEqualsConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final MaxCount constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final MinCount constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final NodeKindConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final PatternConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final QualifiedValueShape constraint) {
			if (constraint.getClass() != other.getClass()) {
				result = false;
				return;
			}
			final QualifiedValueShape o = (QualifiedValueShape) other;
			if (constraint.qMin() != o.qMin()) {
				result = false;
				return;
			}
			if (constraint.qMax() != o.qMax()) {
				result = false;
				return;
			}
			if (constraint.qDisjoint() != o.qDisjoint()) {
				result = false;
				return;
			}
			result = compare(constraint.getSub(), o.getSub());
		}

		@Override
		public void visit(final ShAnd constraint) {
			result = same(constraint, other);
		}

		@Override
		public void visit(final ShNode constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final ShNot constraint) {
			result = same(constraint, other);
		}

		@Override
		public void visit(final ShOr constraint) {
			result = same(constraint, other);
		}

		@Override
		public void visit(final ShXone constraint) {
			result = same(constraint, other);
		}

		@Override
		public void visit(final SparqlConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final StrLanguageIn constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final StrMaxLengthConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final StrMinLengthConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final UniqueLangConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final ValueMaxExclusiveConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final ValueMaxInclusiveConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final ValueMinExclusiveConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

		@Override
		public void visit(final ValueMinInclusiveConstraint constraint) {
			result = DeepEquals.deepEquals(constraint, other);
		}

	}

	ShaclExporter exporter;

	int index = 0;

	private <T> boolean compare(final Collection<T> a, final Collection<T> b, final BiFunction<T, T, Boolean> compare) {
		if (a.size() != b.size())
			return false;
		loop: for (final T n : a) {
			for (final T m : b) {
				if (compare.apply(m, n)) {
					continue loop;
				}
			}
			ShaclExporterTest.log.info("{} could not be matched", n);
			return false;
		}
		return true;
	}

	private boolean compare(final Constraint a, final Constraint b) {
		return new EqualsVisitor(a, b).value();
	}

	private boolean compare(final Path a, final Path b) {
		return DeepEquals.deepEquals(a, b);
	}

	private boolean compare(final Shape a, final Shape b) {
		final int i = index++;
		ShaclExporterTest.log.info(i + " ============== Shape ===============================");
		if (a.deactivated() != b.deactivated()) {
			ShaclExporterTest.log.info("deactivated failing {} != {}", a.deactivated(), b.deactivated());
			return false;
		}
		if (a.hasTarget() != b.hasTarget()) {
			ShaclExporterTest.log.info("hasTarget failing {} != {}", a.hasTarget(), b.hasTarget());
			return false;
		}
		if (a.isNodeShape() != b.isNodeShape()) {
			ShaclExporterTest.log.info("isNodeShape failing {} != {}", a.isNodeShape(), b.isNodeShape());
			return false;
		}
		if (a.isPropertyShape() != b.isPropertyShape()) {
			ShaclExporterTest.log.info("isPropertyShape failing {} != {}", a.isPropertyShape(), b.isPropertyShape());
			return false;
		}
		if (a.getSeverity() != b.getSeverity()) {
			ShaclExporterTest.log.info("getSeverity failing {} != {}", a.getSeverity(), b.getSeverity());
			return false;
		}
		if (!compare(a.getConstraints(), b.getConstraints(), this::compare)) {
			ShaclExporterTest.log.info("getConstraints failing {} != {}", a.getConstraints(), b.getConstraints());
			return false;
		}
		if (!compare(a.getMessages(), b.getMessages(), DeepEquals::deepEquals)) {
			ShaclExporterTest.log.info("getMessages failing {} != {}", a.getMessages(), b.getMessages());
			return false;
		}
		if (!compare(a.getPropertyShapes(), b.getPropertyShapes(), this::compare)) {
			ShaclExporterTest.log.info("getPropertyShapes failing {} != {}", a.getPropertyShapes(),
					b.getPropertyShapes());
			return false;
		}
		if (!compare(a.getTargets(), b.getTargets(), DeepEquals::deepEquals)) {
			ShaclExporterTest.log.info("getTargets failing {} != {}", a.getTargets(), b.getTargets());
			return false;
		}
		if (a instanceof PropertyShape) {
			if (!(b instanceof PropertyShape))
				return false;
			final PropertyShape m = (PropertyShape) a;
			final PropertyShape n = (PropertyShape) b;
			if (!compare(m.getPath(), n.getPath())) {
				ShaclExporterTest.log.info("getPath failing {} != {}", m.getPath(), n.getPath());
				return false;
			}
		} else if (b instanceof PropertyShape)
			return false;
		ShaclExporterTest.log.info(i + " ============== Shape ok =============================");
		return true;
	}

	private boolean compare(final Shapes a, final Shapes b) {
		ShaclExporterTest.log.info("============== Shapes ===============================");
		if (!compare(a.getImports(), b.getImports(), DeepEquals::deepEquals)) {
			ShaclExporterTest.log.info("Imports failing {} != {}", a.getImports(), b.getImports());
			return false;
		}
		if (!compare(a.getTargetShapes(), b.getTargetShapes(), this::compare)) {
			ShaclExporterTest.log.info("Shapes failing {} != {}", a.getTargetShapes(), b.getTargetShapes());
			return false;
		}
		ShaclExporterTest.log.info("============== Shapes ok =============================");
		return true;
	}

	private Shapes read(final String string) {
		final Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, ShaclExporterTest.class.getResourceAsStream(string), Lang.TURTLE);
//		RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_FLAT);
		return Shapes.parse(model);
	}

	@BeforeEach
	void setUp() throws Exception {
		exporter = new ShaclExporter(ModelFactory.createDefaultModel());
	}

	@Test
	void test1() {
		final Shapes s = read("../../../../../validation.ttl");
		final Model export = exporter.export(s);
//		RDFDataMgr.write(System.out, export, RDFFormat.TURTLE_FLAT);
		final Shapes fixpoint = Shapes.parse(export);
		Assertions.assertTrue(compare(s, fixpoint));
	}

	@Test
	void test2() {
		final Shapes s = read("../../../../../vph-validation.ttl");
		final Model export = exporter.export(s);
//		RDFDataMgr.write(System.out, export, RDFFormat.TURTLE_FLAT);
		final Shapes fixpoint = Shapes.parse(export);
		Assertions.assertTrue(compare(s, fixpoint));
	}

}
