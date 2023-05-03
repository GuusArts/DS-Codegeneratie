package nl.kik.commons.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GraphService {
	private static Map<nl.kik.commons.dto.Graph<?>, StorageInfo> active = new HashMap<>();

	public static synchronized void enter(nl.kik.commons.dto.Graph<?> g, boolean read) {
		StorageInfo info = active.computeIfAbsent(g, gg -> new StorageInfo().withCreated(ZonedDateTime.now()));
		info.setCount(info.getCount() + 1);
		info.getReads().add(read);
		try {
			throw new UnsupportedOperationException();
		} catch (UnsupportedOperationException e) {
			info.getStacks().add(e);
		}
	}

	public static synchronized void commit(nl.kik.commons.dto.Graph<?> g) {
		StorageInfo info = active.get(g);
		if (g == null) {
			log.error("Unbalanced enter leave for commit in {}", g);
			return;
		}
		info.getCommits().add(info.getCount());
	}

	public static synchronized void leave(nl.kik.commons.dto.Graph<?> g) {
		StorageInfo info = active.get(g);
		if (g == null) {
			log.error("Unbalanced enter leave for {}", g);
			return;
		}
		info.setCount(info.getCount() - 1);
		if (info.getCount() == 0) {
			active.remove(g);
		}
	}

	public synchronized void checkActive() {
		log.info("Checking stale storages ({} total)", active.size());
		ZonedDateTime cutoff = ZonedDateTime.now().minusMinutes(1);
		ZonedDateTime errorCutoff = ZonedDateTime.now().minusMinutes(5);
		active.entrySet().stream() //
				.filter(e -> e.getValue().getCreated().isBefore(cutoff)) //
				.filter(e -> !e.getValue().getCreated().isBefore(errorCutoff)) //
				.forEach(i -> log.warn("Storage live for over 1 minute {}, depth {}", i.getKey(),
						i.getValue().getCount()));

		active.entrySet().stream() //
				.filter(e -> e.getValue().getCreated().isBefore(errorCutoff)) //
				.forEach(e -> {
					log.error("Storage live for over 10 minutes {}", e.getKey(), e.getValue().getCount());
					e.getValue().getStacks().forEach(ex -> log.info(" - entry point: ", ex));
				});
	}

}

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@With
class StorageInfo {
	private int count = 0;
	private List<Throwable> stacks = new ArrayList<>();
	private ZonedDateTime created;
	private List<Boolean> reads = new ArrayList<>();
	private List<Integer> commits = new ArrayList<>();
}
