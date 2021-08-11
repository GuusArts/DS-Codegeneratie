package nl.kik.commons.gids.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GidsAttributeTest {

	@Test
	public void testOverlappingInternalConsistency() {
		log.info("Just one");
		GidsAttribute<String> v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 3), "A")//
				.build();
		assertOne(v, date(12, 2), date(12, 3), "A");
		log.info("Empty period");
		assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
					.alternative(Source.KIK_STARTER, date(12, 2), date(12, 2), "A")//
					.build();
		});
		log.info("Start efter end");
		assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
					.alternative(Source.KIK_STARTER, date(12, 4), date(12, 2), "A")//
					.build();
		});
	}

	@Test
	public void testOverlappingSame() {
		log.info("Clearly disjoint");
		GidsAttribute<String> v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 3), "A")//
				.alternative(Source.KIK_STARTER, date(12, 5), date(12, 6), "A")//
				.build();
		assertEquals(2, v.getAll(Source.KIK_STARTER).size());
		log.info("Less clearly disjoint");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 3), "A")//
				.alternative(Source.KIK_STARTER, date(12, 4), date(12, 5), "A")//
				.build();
		assertEquals(2, v.getAll(Source.KIK_STARTER).size());

		log.info("Adjacent");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 3), "A")//
				.alternative(Source.KIK_STARTER, date(12, 3), date(12, 4), "A")//
				.build();
		assertOne(v, date(12, 2), date(12, 4), "A");

		log.info("Overlapping");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 4), "A")//
				.alternative(Source.KIK_STARTER, date(12, 3), date(12, 5), "A")//
				.build();
		assertOne(v, date(12, 2), date(12, 5), "A");
		
		log.info("Contained");
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

		log.info("Triple overlap");
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
		
		log.info("Open/semi-open intervals");
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

	@Test
	public void testOverlappingDifferent() {
		assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
					.alternative(Source.KIK_STARTER, date(12, 4), date(12, 2), "A")//
					.build();
		});

		log.info("Clearly disjoint");
		GidsAttribute<String> v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 3), "A")//
				.alternative(Source.KIK_STARTER, date(12, 5), date(12, 6), "B")//
				.build();
		assertEquals(2, v.getAll(Source.KIK_STARTER).size());
		log.info("Less clearly disjoint");
		v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 3), "A")//
				.alternative(Source.KIK_STARTER, date(12, 4), date(12, 5), "B")//
				.build();
		assertEquals(2, v.getAll(Source.KIK_STARTER).size());

		log.info("Adjacent");
		assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
					.alternative(Source.KIK_STARTER, date(12, 2), date(12, 3), "A")//
					.alternative(Source.KIK_STARTER, date(12, 3), date(12, 4), "B")//
					.build();
		});

		log.info("Overlapping");
		assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
					.alternative(Source.KIK_STARTER, date(12, 2), date(12, 4), "A")//
					.alternative(Source.KIK_STARTER, date(12, 3), date(12, 5), "B")//
					.build();
		});
		
		log.info("Contained");
		assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 8), "A")//
				.alternative(Source.KIK_STARTER, date(12, 4), date(12, 6), "B")//
				.build();
		});
		assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 4), date(12, 6), "A")//
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 8), "B")//
				.build();
		});

		log.info("Triple overlap");
		assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
					.alternative(Source.KIK_STARTER, date(12, 2), date(12, 4), "A")//
					.alternative(Source.KIK_STARTER, date(12, 6), date(12, 8), "B")//
					.alternative(Source.KIK_STARTER, date(12, 3), date(12, 7), "A")//
					.build();
		});
		assertThrows(IllegalArgumentException.class, () -> {
			GidsAttribute.<String>builder() //
					.alternative(Source.KIK_STARTER, date(12, 2), date(12, 4), "A")//
					.alternative(Source.KIK_STARTER, date(12, 3), date(12, 7), "A")//
					.alternative(Source.KIK_STARTER, date(12, 6), date(12, 8), "B")//
					.build();
		});
	}
	
	@Test
	public void testGet() {
		GidsAttribute<String> v = GidsAttribute.<String>builder() //
				.alternative(Source.KIK_STARTER, date(12, 2), date(12, 4), "A")//
				.alternative(Source.KIK_STARTER, date(12, 6), date(12, 8), "B")//
				.alternative(Source.LRZA, date(12, 7), date(12, 9), "C")//
				.build();
		
		assertEquals("A", v.get(Source.KIK_STARTER, date(12, 2)));
		assertEquals("A", v.get(Source.KIK_STARTER, date(12, 3)));
		assertNull(v.get(Source.KIK_STARTER, date(12, 4)));
		assertNull(v.get(Source.KIK_STARTER, date(12, 5)));
		assertEquals("B", v.get(Source.KIK_STARTER, date(12, 6)));
		assertEquals("B", v.get(Source.KIK_STARTER, date(12, 7)));
		assertNull(v.get(Source.KIK_STARTER, date(12, 8)));
		
		assertNull(v.get(Source.LRZA, date(12, 6)));
		assertEquals("C", v.get(Source.LRZA, date(12, 7)));
		assertEquals("C", v.get(Source.LRZA, date(12, 8)));
		assertNull(v.get(Source.LRZA, date(12, 9)));

		assertEquals(1, v.getAll(date(12, 6)).size());
		assertEquals(2, v.getAll(date(12, 7)).size());
		assertEquals(1, v.getAll(date(12, 8)).size());
	}

	private void assertOne(GidsAttribute<String> v, LocalDate start, LocalDate end, String string) {
		assertEquals(1, v.getAll(Source.KIK_STARTER).size());
		Triple<LocalDate, LocalDate, String> value = v.getValues().get(Source.KIK_STARTER).iterator().next();
		log.info("Resulting registration {}", value);
		assertEquals(start, value.getLeft());
		assertEquals(end, value.getMiddle());
		assertEquals(string, value.getRight());
	}

	protected LocalDate date(int month, int day) {
		return LocalDate.of(2021, month, day);
	}

}
