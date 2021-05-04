package nl.kik.datastation.service;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import nl.kik.datastation.dto.dcat.Catalog;
import nl.kik.datastation.dto.dcat.DataService;
import nl.kik.datastation.dto.dcat.Dataset;
import nl.kik.datastation.dto.dcat.Distribution;

@Slf4j
public class CatalogService {
	/**
	 * Return all datasets that provide a dataservice conforming to the provided
	 * semantics
	 * 
	 * @param catalog
	 * @param provides
	 * @return
	 */
	public Collection<Dataset> getDatasets(Catalog catalog, URL provides) {
		Objects.requireNonNull(catalog, "catalog must be given");
		Objects.requireNonNull(provides, "provides must be given");
		return Objects.requireNonNullElse(catalog.getDataset(), Collections.<Dataset>emptySet()).stream() //
				.filter(dataset -> Objects
						.requireNonNullElse(dataset.getDistribution(), Collections.<Distribution>emptySet()).stream() //
						.anyMatch(distribution -> Objects
								.requireNonNullElse(distribution.getAccessService(),
										Collections.<DataService>emptySet())
								.stream() //
								.anyMatch(dataservice -> provides.equals(dataservice.getConformsTo())) //
						) //
				) //
				.collect(Collectors.toList());
	}

	/**
	 * Return the endpoint(s) providing the given semantics.
	 * 
	 * @param dataset
	 * @param provices
	 * @return
	 */
	public Collection<DataService> getEndpoints(Dataset dataset, URL provides) {
		Objects.requireNonNull(dataset, "dataset must be given");
		Objects.requireNonNull(provides, "provides must be given");
		return Objects.requireNonNullElse(dataset.getDistribution(), Collections.<Distribution>emptySet()).stream() //
				.flatMap(distribution -> Objects
						.requireNonNullElse(distribution.getAccessService(), Collections.<DataService>emptySet())
						.stream() //
						.filter(dataservice -> provides.equals(dataservice.getConformsTo())) //
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
	public Optional<DataService> getEndpoint(Dataset dataset, URL provides) {
		return getEndpoints(dataset, provides).stream().findAny();
	}

}
