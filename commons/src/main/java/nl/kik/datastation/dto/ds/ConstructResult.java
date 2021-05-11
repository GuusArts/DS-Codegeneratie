package nl.kik.datastation.dto.ds;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import net.minidev.json.JSONObject;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ConstructResult extends SPARQLResult {
	private JSONObject data;
}
