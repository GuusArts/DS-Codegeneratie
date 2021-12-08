package nl.kik.commons.gids.dto;

import java.util.List;

public interface HasNames {
	List<GidsAttribute<String>> getName();

	GidsAttribute<String> getPrimaryName();
}
