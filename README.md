# Introduction

This module aims to give access to shared KIK-V resources in a uniform way.  The module comprises functionality to access to zorgaanbieder Gids (used by KIK-Starter and KIK-Registratie), a datastation + NUTS nodes, and DCAT2 metadata.

Modules are set up to provide maximal support while pulling in minimal dependencies; as such each sub-module exists as `<name>` as well as `<name>-spring`, where the latter pulls in Spring and Jena dependencies.

# Datastation protocol and NUTS

The `ds` and `ds-spring` modules provide easy programatic access to NUTS nodes and the KIK-V datastation protocol.

`ds` provides DTOs for interacting with the protocol, including DTOs for the NUTS APIs necessary for the KIK-V program.  It pulls in JWT and JSON-LD/DID APIs.  Credentials necessary for KIK-V are implementeded to give direct access to them in parsed form.

`ds-spring` provides services for communicating directly with NUTS as well as the datastation protocol from Spring applciations, as well as serializing/deserializing to/from Jena datatypes.

### Notes

The NUTS functionality should optimally be extracted to a generic library supporting all of NUTS without the KIK-V extensions.

The VerifiablePresentation in the used library does not allow multiple VerifiableCredentials; there is an alternative used here that does.

This code now includes a full implementation of DCAT2 (Data Catalog Vocabulary) to support semantic interoperability for data catalogs.

# DCAT2

The `dcat` and `dcat-spring` modules provide support for the [DCAT2 (Data Catalog Vocabulary)](https://www.w3.org/TR/vocab-dcat-2/) standard, which is a W3C recommendation for describing data catalogs.

`dcat` provides Java models for DCAT2 entities (Catalog, Dataset, Distribution, DataService, CatalogRecord) and vocabulary constants. These can be used to create and manipulate DCAT2 metadata in Java applications.

`dcat-spring` provides Spring integration for DCAT2, including services for converting DCAT2 objects to and from RDF using Apache Jena. This enables applications to serialize and deserialize DCAT2 metadata in various formats (RDF/XML, Turtle, JSON-LD, etc.).

## Usage Example

```java
// Create a catalog
Catalog catalog = Catalog.builder()
        .id("http://example.org/catalog/1")
        .title("Example Catalog")
        .description("An example catalog")
        .build();

// Create a dataset
Dataset dataset = Dataset.builder()
        .id("http://example.org/dataset/1")
        .title("Example Dataset")
        .description("An example dataset")
        .build();

// Create a distribution
Distribution distribution = Distribution.builder()
        .id("http://example.org/distribution/1")
        .title("Example Distribution")
        .description("An example distribution")
        .accessURL(URI.create("http://example.org/dataset/1/access"))
        .downloadURL(URI.create("http://example.org/dataset/1/download"))
        .mediaType("application/json")
        .format("JSON")
        .build();

// Add the distribution to the dataset
dataset.addDistribution(distribution);

// Add the dataset to the catalog
catalog.addDataset(dataset);

// In a Spring application, use DCAT2Service to convert to RDF
@Autowired
private DCAT2Service dcat2Service;

// Save the catalog to RDF
Graph<Model> graph = Graph.create(ModelFactory.createDefaultModel());
dcat2Service.save(graph, catalog);

// Serialize to Turtle
StringWriter writer = new StringWriter();
graph.getModel().write(writer, "TURTLE");
String turtleOutput = writer.toString();
```

# ZA Gids

Provides DTO + service for communicating with the ZA Gids in KIK-Starter.

# Using

Use the [maven repository](https://gitlab.opencirclesolutions.nl/os/kik-commons/-/packages).  Make sure you have set up a key to access.

# Building

Should build using Maven 3.8 using 

```
mvn clean install
```

# Error reports

Create [issues](https://gitlab.opencirclesolutions.nl/os/kik-commons/-/issues) in Gitlab.

# Contributing

Make a [merge request](https://gitlab.opencirclesolutions.nl/os/kik-commons/-/merge_requests)
