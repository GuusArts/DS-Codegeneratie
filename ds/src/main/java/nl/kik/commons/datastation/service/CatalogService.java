package nl.kik.commons.datastation.service;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import nl.kik.commons.datastation.dto.dcat.Catalog;
import nl.kik.commons.datastation.dto.dcat.DataService;
import nl.kik.commons.datastation.dto.dcat.Dataset;
import nl.kik.commons.datastation.dto.dcat.Distribution;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class CatalogService {
	/**
	 * Return all datasets that provide a dataservice conforming to the provided
	 * semantics
	 *
	 * @param catalog
	 * @param provides
	 * @return
	 */
	public Collection<Dataset> getDatasets(final Catalog catalog, final URI provides) {
		Objects.requireNonNull(catalog, "catalog must be given");
		Objects.requireNonNull(provides, "provides must be given");
		return Objects.requireNonNullElse(catalog.getDataset(), Collections.<Dataset>emptySet()).stream() //
				.filter(dataset -> Objects.requireNonNullElse(dataset.getDistribution(), Collections.<Distribution>emptySet())
						.stream() //
						.anyMatch(distribution -> Objects
								.requireNonNullElse(distribution.getAccessService(), Collections.<DataService>emptySet()).stream() //
								.anyMatch(dataservice -> dataservice.getConformsTo() != null
										&& dataservice.getConformsTo().contains(provides)) //
						) //
				) //
				.collect(Collectors.toList());
	}

	/**
	 * Return the endpoint providing the given semantics. If there are multiple
	 * (e.g., due to load balancing/failover), return any
	 *
	 * @param dataset
	 * @param provices
	 * @return
	 */
	public Optional<DataService> getEndpoint(final Dataset dataset, final URI provides) {
		return getEndpoints(dataset, provides).stream().findAny();
	}

	/**
	 * Return the endpoint(s) providing the given semantics.
	 *
	 * @param dataset
	 * @param provices
	 * @return
	 */
	public Collection<DataService> getEndpoints(final Dataset dataset, final URI provides) {
		Objects.requireNonNull(dataset, "dataset must be given");
		Objects.requireNonNull(provides, "provides must be given");
		return Objects.requireNonNullElse(dataset.getDistribution(), Collections.<Distribution>emptySet()).stream() //
				.flatMap(distribution -> Objects
						.requireNonNullElse(distribution.getAccessService(), Collections.<DataService>emptySet()).stream() //
						.filter(
								dataservice -> dataservice.getConformsTo() != null && dataservice.getConformsTo().contains(provides)) //
				) //
				.collect(Collectors.toList());
	}

	/**
	 * Serialize the given Catalog to DCAT2-compliant JSON-LD.
	 *
	 * @param catalog the Catalog to serialize
	 * @return JSON-LD string
	 * @throws JsonProcessingException if serialization fails
	 */
	public String publishCatalogAsJsonLD(Catalog catalog) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		// If you have the JSON-LD module, register it here
		// mapper.registerModule(new JsonldModule());
		return mapper.writeValueAsString(catalog);
	}

	/**
	 * Example: Construct a sample Catalog with one Dataset and one Distribution.
	 */
	public static Catalog exampleCatalog() {
		Distribution distribution = Distribution.builder()
			.title("Example Distribution")
			.description("A sample distribution for demonstration.")
			.build();
		Dataset dataset = Dataset.builder()
			.title("Example Dataset")
			.description("A sample dataset for demonstration.")
			.distribution(Collections.singleton(distribution))
			.build();
		Catalog catalog = Catalog.builder()
			.title("Example Catalog")
			.description("A sample DCAT2 catalog.")
			.dataset(Collections.singleton(dataset))
			.build();
		return catalog;
	}

}
