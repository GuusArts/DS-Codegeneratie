package nl.kik.datastation.dto.dcat;

import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class DataService extends Resource {
	URL endpointURL;
	Object endpointDescription;
	List<Dataset> servesDataset;
	
}
