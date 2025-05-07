package nl.kik.commons.datastation.dto.dcat;

import java.net.URI;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.dto.RDFObject;

/**
 * A curated collection of metadata about resources (e.g., datasets and data services).
 * Based on DCAT2 specification: https://www.w3.org/TR/vocab-dcat-2/#Class:Catalog
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Catalog extends Dataset {
    URI homepage;
    Set<RDFObject> themeTaxonomy;
    Set<Resource> hasPart;
    Set<Dataset> dataset;
    Set<DataService> service;
    Set<Catalog> catalog;
    Set<CatalogRecord> record;
    
    Set<DatasetSeries> datasetSeries;
    Set<URI> language;
}
