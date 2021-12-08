package nl.kik.commons.datastation.service;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.parser.Shape;

import com.cedarsoftware.util.DeepEquals;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShapesComparator implements BiPredicate<Shapes, Shapes> {

	private final BiPredicate<Shape, Shape> shapeComparator;

	public ShapesComparator() {
		this(null);
	}

	public ShapesComparator(final BiPredicate<Shape, Shape> shapeComparator) {
		this.shapeComparator = shapeComparator == null ? new ShapeComparator() : shapeComparator;
	}

	private <T> boolean compare(final Collection<T> a, final Collection<T> b, final BiFunction<T, T, Boolean> compare) {
		if (a.size() != b.size()) {
			return false;
		}
		loop: for (final T n : a) {
			for (final T m : b) {
				if (compare.apply(m, n)) {
					continue loop;
				}
			}
			ShapesComparator.log.trace("{} could not be matched", n);
			return false;
		}
		return true;
	}

	@Override
	public boolean test(final Shapes a, final Shapes b) {
		ShapesComparator.log.trace("============== Shapes ===============================");
		if (!compare(a.getImports(), b.getImports(), DeepEquals::deepEquals)) {
			ShapesComparator.log.trace("Imports failing {} != {}", a.getImports(), b.getImports());
			return false;
		}
		if (!compare(a.getTargetShapes(), b.getTargetShapes(), shapeComparator::test)) {
			ShapesComparator.log.trace("Shapes failing {} != {}", a.getTargetShapes(), b.getTargetShapes());
			return false;
		}
		ShapesComparator.log.trace("============== Shapes ok =============================");
		return true;
	}

}
