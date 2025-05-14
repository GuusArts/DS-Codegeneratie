package nl.kik.commons.datastation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import nl.kik.commons.dto.Graph;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final DCATService dcatService;

    @Autowired
    public CatalogController(DCATService dcatService) {
        this.dcatService = dcatService;
    }

    @GetMapping(value = "", produces = "application/ld+json")
    public ResponseEntity<String> getCatalog() {
        try {
            // TODO: Replace with real model/data integration
            Model model = ModelFactory.createDefaultModel();
            Graph<Model> graph = new Graph<>(model);
            String jsonld = dcatService.getCatalogAsJsonLD(graph);
            if (jsonld == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No catalog found");
            }
            return ResponseEntity.ok(jsonld);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Serialization error: " + e.getMessage());
        }
    }
} 