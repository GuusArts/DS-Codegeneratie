package nl.kik.commons.dcat2.service;

import nl.kik.commons.dcat2.dto.Catalog;
import nl.kik.commons.dcat2.dto.Dataset;
import nl.kik.commons.dcat2.dto.Distribution;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class Dcat2RdfServiceImpl implements Dcat2RdfService {
    @Override
    public Model toRdf(Catalog catalog) {
        Model model = ModelFactory.createDefaultModel();
        Resource catalogRes = model.createResource(catalog.getId() != null ? catalog.getId() : "urn:catalog");
        catalogRes.addProperty(RDF.type, DCAT.Catalog);
        if (catalog.getTitle() != null) catalogRes.addProperty(DCTerms.title, catalog.getTitle());
        if (catalog.getDescription() != null) catalogRes.addProperty(DCTerms.description, catalog.getDescription());
        if (catalog.getDatasets() != null) {
            for (Dataset ds : catalog.getDatasets()) {
                Resource dsRes = model.createResource(ds.getId() != null ? ds.getId() : "urn:dataset");
                dsRes.addProperty(RDF.type, DCAT.Dataset);
                if (ds.getTitle() != null) dsRes.addProperty(DCTerms.title, ds.getTitle());
                if (ds.getDescription() != null) dsRes.addProperty(DCTerms.description, ds.getDescription());
                if (ds.getDistributions() != null) {
                    for (Distribution dist : ds.getDistributions()) {
                        Resource distRes = model.createResource(dist.getId() != null ? dist.getId() : "urn:distribution");
                        distRes.addProperty(RDF.type, DCAT.Distribution);
                        if (dist.getTitle() != null) distRes.addProperty(DCTerms.title, dist.getTitle());
                        if (dist.getAccessURL() != null) distRes.addProperty(DCAT.accessURL, model.createResource(dist.getAccessURL()));
                        dsRes.addProperty(DCAT.distribution, distRes);
                    }
                }
                catalogRes.addProperty(DCAT.dataset, dsRes);
            }
        }
        return model;
    }

    @Override
    public Catalog fromRdf(Model model) {
        Catalog catalog = new Catalog();
        ResIterator catalogs = model.listResourcesWithProperty(RDF.type, DCAT.Catalog);
        if (catalogs.hasNext()) {
            Resource catalogRes = catalogs.next();
            catalog.setId(catalogRes.getURI());
            if (catalogRes.hasProperty(DCTerms.title))
                catalog.setTitle(catalogRes.getProperty(DCTerms.title).getString());
            if (catalogRes.hasProperty(DCTerms.description))
                catalog.setDescription(catalogRes.getProperty(DCTerms.description).getString());
            List<Dataset> datasets = new ArrayList<>();
            NodeIterator dsNodes = catalogRes.listProperties(DCAT.dataset).mapWith(Statement::getObject);
            while (dsNodes.hasNext()) {
                RDFNode dsNode = dsNodes.next();
                if (dsNode.isResource()) {
                    Resource dsRes = dsNode.asResource();
                    Dataset ds = new Dataset();
                    ds.setId(dsRes.getURI());
                    if (dsRes.hasProperty(DCTerms.title))
                        ds.setTitle(dsRes.getProperty(DCTerms.title).getString());
                    if (dsRes.hasProperty(DCTerms.description))
                        ds.setDescription(dsRes.getProperty(DCTerms.description).getString());
                    List<Distribution> distributions = new ArrayList<>();
                    NodeIterator distNodes = dsRes.listProperties(DCAT.distribution).mapWith(Statement::getObject);
                    while (distNodes.hasNext()) {
                        RDFNode distNode = distNodes.next();
                        if (distNode.isResource()) {
                            Resource distRes = distNode.asResource();
                            Distribution dist = new Distribution();
                            dist.setId(distRes.getURI());
                            if (distRes.hasProperty(DCTerms.title))
                                dist.setTitle(distRes.getProperty(DCTerms.title).getString());
                            if (distRes.hasProperty(DCAT.accessURL))
                                dist.setAccessURL(distRes.getProperty(DCAT.accessURL).getResource().getURI());
                            distributions.add(dist);
                        }
                    }
                    ds.setDistributions(distributions);
                    datasets.add(ds);
                }
            }
            catalog.setDatasets(datasets);
        }
        return catalog;
    }

    @Override
    public String toJsonLd(Catalog catalog) {
        Model model = toRdf(catalog);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RDFDataMgr.write(out, model, Lang.JSONLD);
        return out.toString();
    }

    @Override
    public Catalog fromJsonLd(String jsonLd) {
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(jsonLd), null, Lang.JSONLD);
        return fromRdf(model);
    }
} 