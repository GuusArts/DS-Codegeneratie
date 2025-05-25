package nl.kik.commons.dcat2.service;

import nl.kik.commons.dcat2.dto.Catalog;
import org.apache.jena.rdf.model.Model;

public interface Dcat2RdfService {
    Model toRdf(Catalog catalog);
    Catalog fromRdf(Model model);
    String toJsonLd(Catalog catalog);
    Catalog fromJsonLd(String jsonLd);
} 