package nl.kik.commons.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GraphService {
    private static Map<nl.kik.commons.dto.Graph<?>, StorageInfo> active = new HashMap<>();
    private static Set<nl.kik.commons.dto.Graph<?>> logged = new HashSet<>();

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
            if (logged.remove(g)) {
                log.warn("Finally removing {}", g);
            }
        }
    }

    public synchronized static void checkActive(int warning, int error) {
        log.trace("Checking stale storages ({} total)", active.size());
        ZonedDateTime cutoff = ZonedDateTime.now().minusMinutes(warning);
        ZonedDateTime errorCutoff = ZonedDateTime.now().minusMinutes(error);
        active.entrySet().stream() //
                .filter(e -> e.getValue().getCreated().isBefore(cutoff)) //
                .filter(e -> !e.getValue().getCreated().isBefore(errorCutoff)) //
                .forEach(i -> log.warn("Storage live for over {} minute {}, depth {}", warning, i.getKey(),
                        i.getValue().getCount()));

        active.entrySet().stream() //
                .filter(e -> e.getValue().getCreated().isBefore(errorCutoff)) //
                .filter(e -> !logged.contains(e.getKey())) //
                .forEach(e -> {
                    log.error("Storage live for over {} minutes {}, depth {}, since {}", error, e.getKey(),
                            e.getValue().getCount(), e.getValue().getCreated());
                    log.info(" - reads: {}", e.getValue().getReads());
                    log.info(" - commits: {}", e.getValue().getCommits());
                    logged.add(e.getKey());
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
