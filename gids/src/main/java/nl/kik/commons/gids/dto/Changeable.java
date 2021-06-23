package nl.kik.commons.gids.dto;

import java.time.ZonedDateTime;

public interface Changeable {
	GidsAttribute<ZonedDateTime> getLastModified();
}
