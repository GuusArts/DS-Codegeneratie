package nl.kik.commons.dcat.service;

import org.apache.jena.rdf.model.Resource;

import nl.kik.commons.dcat.model.Catalog;
import nl.kik.commons.dcat.model.CatalogRecord;
import nl.kik.commons.dcat.model.DataService;
import nl.kik.commons.dcat.model.Dataset;
import nl.kik.commons.dcat.model.Distribution;
import nl.kik.commons.dto.Graph;

/**
 * Service for working with DCAT2 objects and RDF.
 */
public interface DCAT2Service {

    /**
     * Saves a DCAT2 Catalog to an RDF graph.
     * 
     * @param graph The graph to save to
     * @param catalog The catalog to save
     * @return The RDF resource representing the catalog
     */
    Resource save(Graph<?> graph, Catalog catalog);

    /**
     * Saves a DCAT2 Dataset to an RDF graph.
     * 
     * @param graph The graph to save to
     * @param dataset The dataset to save
     * @return The RDF resource representing the dataset
     */
    Resource save(Graph<?> graph, Dataset dataset);

    /**
     * Saves a DCAT2 Distribution to an RDF graph.
     * 
     * @param graph The graph to save to
     * @param distribution The distribution to save
     * @return The RDF resource representing the distribution
     */
    Resource save(Graph<?> graph, Distribution distribution);

    /**
     * Saves a DCAT2 DataService to an RDF graph.
     * 
     * @param graph The graph to save to
     * @param dataService The data service to save
     * @return The RDF resource representing the data service
     */
    Resource save(Graph<?> graph, DataService dataService);

    /**
     * Saves a DCAT2 CatalogRecord to an RDF graph.
     * 
     * @param graph The graph to save to
     * @param catalogRecord The catalog record to save
     * @return The RDF resource representing the catalog record
     */
    Resource save(Graph<?> graph, CatalogRecord catalogRecord);

    /**
     * Loads a DCAT2 Catalog from an RDF graph.
     * 
     * @param graph The graph to load from
     * @param uri The URI of the catalog to load
     * @return The loaded catalog
     */
    Catalog loadCatalog(Graph<?> graph, String uri);

    /**
     * Loads a DCAT2 Dataset from an RDF graph.
     * 
     * @param graph The graph to load from
     * @param uri The URI of the dataset to load
     * @return The loaded dataset
     */
    Dataset loadDataset(Graph<?> graph, String uri);

    /**
     * Loads a DCAT2 Distribution from an RDF graph.
     * 
     * @param graph The graph to load from
     * @param uri The URI of the distribution to load
     * @return The loaded distribution
     */
    Distribution loadDistribution(Graph<?> graph, String uri);

    /**
     * Loads a DCAT2 DataService from an RDF graph.
     * 
     * @param graph The graph to load from
     * @param uri The URI of the data service to load
     * @return The loaded data service
     */
    DataService loadDataService(Graph<?> graph, String uri);

    /**
     * Loads a DCAT2 CatalogRecord from an RDF graph.
     * 
     * @param graph The graph to load from
     * @param uri The URI of the catalog record to load
     * @return The loaded catalog record
     */
    CatalogRecord loadCatalogRecord(Graph<?> graph, String uri);
}