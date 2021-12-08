package nl.kik.commons.datastation.service;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

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
import org.apache.jena.shacl.validation.Severity;
import org.apache.jena.sparql.path.Path;

import com.cedarsoftware.util.DeepEquals;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShapeComparator implements BiPredicate<Shape, Shape> {
	public final class EqualsVisitor implements ConstraintVisitor {
		private boolean result;
		private final Constraint other;

		public EqualsVisitor(final Constraint a, final Constraint b) {
			other = b;
			a.visit(this);
		}

		private boolean same(final ConstraintOp1 a, final Constraint b) {
			if (a.getClass() != b.getClass()) {
				return false;
			}
			return test(a.getOther(), ((ConstraintOp1) b).getOther());
		}

		private boolean same(final ConstraintOpN a, final Constraint b) {
			if (a.getClass() != b.getClass()) {
				return false;
			}
			return compare(a.getOthers(), ((ConstraintOpN) b).getOthers(), ShapeComparator.this::test);
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
			if (constraint.qMin() != o.qMin() || constraint.qMax() != o.qMax() || constraint.qDisjoint() != o.qDisjoint()) {
				result = false;
				return;
			}
			result = test(constraint.getSub(), o.getSub());
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

	int index = 0;

	private <T> boolean compare(final Collection<T> a, final Collection<T> b, final BiFunction<T, T, Boolean> compare) {
		if (a == null) {
			return b == null || b.isEmpty() == true;
		}
		if (b == null) {
			return a.isEmpty();
		}
		if (a.size() != b.size()) {
			return false;
		}
		loop: for (final T n : a) {
			for (final T m : b) {
				if (compare.apply(m, n)) {
					continue loop;
				}
			}
			ShapeComparator.log.trace("{} could not be matched", n);
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

	@Override
	public boolean test(final Shape a, final Shape b) {
		if (a == null) {
			return b == null == true;
		}
		if (b == null) {
			return false;
		}
		final int i = index++;
		ShapeComparator.log.trace(i + " ============== Shape ===============================");
		if (a.deactivated() != b.deactivated()) {
			ShapeComparator.log.trace("deactivated failing {} != {}", a.deactivated(), b.deactivated());
			return false;
		}
		if (a.hasTarget() != b.hasTarget()) {
			ShapeComparator.log.trace("hasTarget failing {} != {}", a.hasTarget(), b.hasTarget());
			return false;
		}
		if (a.isNodeShape() != b.isNodeShape()) {
			ShapeComparator.log.trace("isNodeShape failing {} != {}", a.isNodeShape(), b.isNodeShape());
			return false;
		}
		if (a.isPropertyShape() != b.isPropertyShape()) {
			ShapeComparator.log.trace("isPropertyShape failing {} != {}", a.isPropertyShape(), b.isPropertyShape());
			return false;
		}
		if (!Objects.equals(Objects.requireNonNullElse(a.getSeverity(), Severity.Violation),
				Objects.requireNonNullElse(b.getSeverity(), Severity.Violation))) {
			ShapeComparator.log.trace("getSeverity failing {} != {}", a.getSeverity(), b.getSeverity());
			return false;
		}
		if (!compare(a.getConstraints(), b.getConstraints(), this::compare)) {
			ShapeComparator.log.trace("getConstraints failing {} != {}", a.getConstraints(), b.getConstraints());
			return false;
		}
		if (!compare(a.getMessages(), b.getMessages(), DeepEquals::deepEquals)) {
			ShapeComparator.log.trace("getMessages failing {} != {}", a.getMessages(), b.getMessages());
			return false;
		}
		if (!compare(a.getPropertyShapes(), b.getPropertyShapes(), this::test)) {
			ShapeComparator.log.trace("getPropertyShapes failing {} != {}", a.getPropertyShapes(), b.getPropertyShapes());
			return false;
		}
		if (!compare(a.getTargets(), b.getTargets(), DeepEquals::deepEquals)) {
			ShapeComparator.log.trace("getTargets failing {} != {}", a.getTargets(), b.getTargets());
			return false;
		}
		if (a instanceof PropertyShape) {
			if (!(b instanceof PropertyShape)) {
				return false;
			}
			final PropertyShape m = (PropertyShape) a;
			final PropertyShape n = (PropertyShape) b;
			if (!compare(m.getPath(), n.getPath())) {
				ShapeComparator.log.trace("getPath failing {} != {}", m.getPath(), n.getPath());
				return false;
			}
		} else if (b instanceof PropertyShape) {
			return false;
		}
		ShapeComparator.log.trace(i + " ============== Shape ok =============================");
		return true;
	}

}
