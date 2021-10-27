package nl.kik.commons.gids.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class GidsAttributeTest {

	private void assertOne(final GidsAttribute<String> v, final LocalDate start, final LocalDate end,
			final String string) {
		Assertions.assertEquals(1, v.getAll(Source.KIK_STARTER).size());
		final Triple<LocalDate, LocalDate, String> value = v.getValues().get(Source.KIK_STARTER).iterator().next();
		GidsAttributeTest.log.info("Resulting registration {}", value);
		Assertions.assertEquals(start, value.getLeft());
		Assertions.assertEquals(end, value.getMiddle());
		Assertions.assertEquals(string, value.getRight());
	}

	private LocalDate date(final int month, final int day) {
		return LocalDate.of(2021, month, day);
	}

	@Test
	void testProject() {
		final GidsAttribute<String> v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 4), "A")//
				.alternative(Source.KIK_STARTER, date(12, 6), date(12, 8), "B")//
				.alternative(Source.LRZA, date(12, 7), date(12, 9), "C")//
				.build();

		assertEquals(3, v.getAll().size());
		assertEquals(1, v.project(Source.LRZA).getAll().size());
		assertEquals(2, v.project(Source.KIK_STARTER).getAll().size());
		assertEquals(2, v.project(date(12, 7)).getAll().size());
		assertEquals(1, v.project(date(12, 6)).getAll().size());
		assertEquals(1, v.project(date(12, 3)).getAll().size());
		assertNull(v.project(date(12, 1)));

		GidsAttribute<Organisation> o = GidsAttribute.<Organisation>builder() //
				.alternative(Source.LRZA, Organisation.builder() //
						.id("hello") //
						.primaryName(GidsAttribute.<String>builder() //
								.alternative(Source.LRZA, "hello") //
								.alternative(Source.TABELBEHEER, "world") //
								.build()) //
						.build()) //
				.build();

		GidsAttribute<Organisation> p = o.project(Source.LRZA);
		assertNotNull(p);
		assertEquals(1, p.getAll().size());
		assertEquals(1, p.getAny().getPrimaryName().getAll().size());
		assertNull(o.project(Source.TABELBEHEER));
	}

	@Test
	void testGet() {
		final GidsAttribute<String> v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 4), "A")//
				.alternative(Source.KIK_STARTER, date(12, 6), date(12, 8), "B")//
				.alternative(Source.LRZA, date(12, 7), date(12, 9), "C")//
				.build();

		Assertions.assertEquals("A", v.get(Source.KIK_STARTER, date(12, 2)));
		Assertions.assertEquals("A", v.get(Source.KIK_STARTER, date(12, 3)));
		Assertions.assertNull(v.get(Source.KIK_STARTER, date(12, 4)));
		Assertions.assertNull(v.get(Source.KIK_STARTER, date(12, 5)));
		Assertions.assertEquals("B", v.get(Source.KIK_STARTER, date(12, 6)));
		Assertions.assertEquals("B", v.get(Source.KIK_STARTER, date(12, 7)));
		Assertions.assertNull(v.get(Source.KIK_STARTER, date(12, 8)));

		Assertions.assertNull(v.get(Source.LRZA, date(12, 6)));
		Assertions.assertEquals("C", v.get(Source.LRZA, date(12, 7)));
		Assertions.assertEquals("C", v.get(Source.LRZA, date(12, 8)));
		Assertions.assertNull(v.get(Source.LRZA, date(12, 9)));

		Assertions.assertEquals(1, v.getAll(date(12, 6)).size());
		Assertions.assertEquals(2, v.getAll(date(12, 7)).size());
		Assertions.assertEquals(1, v.getAll(date(12, 8)).size());
	}

	@Test
	void testOverlappingDifferent() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
					.alternative(Source.KIK_STARTER, date(12, 4), date(12, 2), "A")//
					.build();
		});

		GidsAttributeTest.log.info("Clearly disjoint");
		GidsAttribute<String> v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 3), "A")//
				.alternative(Source.KIK_STARTER, date(12, 5), date(12, 6), "B")//
				.build();
		Assertions.assertEquals(2, v.getAll(Source.KIK_STARTER).size());
		GidsAttributeTest.log.info("Less clearly disjoint");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 3), "A")//
				.alternative(Source.KIK_STARTER, date(12, 4), date(12, 5), "B")//
				.build();
		Assertions.assertEquals(2, v.getAll(Source.KIK_STARTER).size());

		GidsAttributeTest.log.info("Adjacent");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 3), "A")//
				.alternative(Source.KIK_STARTER, date(12, 3), date(12, 4), "B")//
				.build();
		Assertions.assertEquals(2, v.getAll(Source.KIK_STARTER).size());

		GidsAttributeTest.log.info("Overlapping");
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
					.alternative(Source.KIK_STARTER, date(12, 2), date(12, 4), "A")//
					.alternative(Source.KIK_STARTER, date(12, 3), date(12, 5), "B")//
					.build();
		});

		GidsAttributeTest.log.info("Contained");
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
					.alternative(Source.KIK_STARTER, date(12, 2), date(12, 8), "A")//
					.alternative(Source.KIK_STARTER, date(12, 4), date(12, 6), "B")//
					.build();
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
					.alternative(Source.KIK_STARTER, date(12, 4), date(12, 6), "A")//
					.alternative(Source.KIK_STARTER, date(12, 2), date(12, 8), "B")//
					.build();
		});

		GidsAttributeTest.log.info("Triple overlap");
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
					.alternative(Source.KIK_STARTER, date(12, 2), date(12, 4), "A")//
					.alternative(Source.KIK_STARTER, date(12, 6), date(12, 8), "B")//
					.alternative(Source.KIK_STARTER, date(12, 3), date(12, 7), "A")//
					.build();
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
					.alternative(Source.KIK_STARTER, date(12, 2), date(12, 4), "A")//
					.alternative(Source.KIK_STARTER, date(12, 3), date(12, 7), "A")//
					.alternative(Source.KIK_STARTER, date(12, 6), date(12, 8), "B")//
					.build();
		});
	}

	@Test
	void testOverlappingInternalConsistency() {
		GidsAttributeTest.log.info("Just one");
		final GidsAttribute<String> v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 3), "A")//
				.build();
		assertOne(v, date(12, 2), date(12, 3), "A");
		GidsAttributeTest.log.info("Empty period");
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
					.alternative(Source.KIK_STARTER, date(12, 2), date(12, 2), "A")//
					.build();
		});
		GidsAttributeTest.log.info("Start efter end");
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
					.alternative(Source.KIK_STARTER, date(12, 4), date(12, 2), "A")//
					.build();
		});
	}

	@Test
	void testOverlappingSame() {
		GidsAttributeTest.log.info("Clearly disjoint");
		GidsAttribute<String> v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 3), "A")//
				.alternative(Source.KIK_STARTER, date(12, 5), date(12, 6), "A")//
				.build();
		Assertions.assertEquals(2, v.getAll(Source.KIK_STARTER).size());
		GidsAttributeTest.log.info("Less clearly disjoint");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 3), "A")//
				.alternative(Source.KIK_STARTER, date(12, 4), date(12, 5), "A")//
				.build();
		Assertions.assertEquals(2, v.getAll(Source.KIK_STARTER).size());

		GidsAttributeTest.log.info("Adjacent");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 3), "A")//
				.alternative(Source.KIK_STARTER, date(12, 3), date(12, 4), "A")//
				.build();
		assertOne(v, date(12, 2), date(12, 4), "A");

		GidsAttributeTest.log.info("Overlapping");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 4), "A")//
				.alternative(Source.KIK_STARTER, date(12, 3), date(12, 5), "A")//
				.build();
		assertOne(v, date(12, 2), date(12, 5), "A");

		GidsAttributeTest.log.info("Contained");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 8), "A")//
				.alternative(Source.KIK_STARTER, date(12, 4), date(12, 6), "A")//
				.build();
		assertOne(v, date(12, 2), date(12, 6), "A");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 4), date(12, 6), "A")//
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 8), "A")//
				.build();
		assertOne(v, date(12, 2), date(12, 8), "A");

		GidsAttributeTest.log.info("Triple overlap");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 4), "A")//
				.alternative(Source.KIK_STARTER, date(12, 6), date(12, 8), "A")//
				.alternative(Source.KIK_STARTER, date(12, 3), date(12, 7), "A")//
				.build();
		assertOne(v, date(12, 2), date(12, 7), "A");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 4), "A")//
				.alternative(Source.KIK_STARTER, date(12, 3), date(12, 7), "A")//
				.alternative(Source.KIK_STARTER, date(12, 6), date(12, 8), "A")//
				.build();
		assertOne(v, date(12, 2), date(12, 8), "A");

		GidsAttributeTest.log.info("Open/semi-open intervals");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), null, "A")//
				.alternative(Source.KIK_STARTER, date(12, 3), date(12, 4), "A")//
				.build();
		assertOne(v, date(12, 2), date(12, 4), "A");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 4), "A")//
				.alternative(Source.KIK_STARTER, date(12, 3), null, "A")//
				.build();
		assertOne(v, date(12, 2), null, "A");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 3), date(12, 5), "A")//
				.alternative(Source.KIK_STARTER, date(12, 4), null, "A")//
				.build();
		assertOne(v, date(12, 3), null, "A");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 5), "A")//
				.alternative(Source.KIK_STARTER, null, date(12, 4), "A")//
				.build();
		assertOne(v, date(12, 2), date(12, 4), "A");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 5), "A")//
				.alternative(Source.KIK_STARTER, null, null, "A")//
				.build();
		assertOne(v, date(12, 2), null, "A");
	}

}
