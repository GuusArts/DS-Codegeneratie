# Introduction

This module aims to give access to shared KIK-V resources in a uniform way.  The module comprises functionality to access to zorgaanbieder Gids (used by KIK-Starter and KIK-Registratie) and a datastation + NUTS nodes.

Modules are set up to provide maximal support while pulling in minimal dependencies; as such each sub-module exists as `<name>` as well as `<name>-spring`, where the latter pulls in Spring and Jena dependencies.

# Datastation protocol and NUTS

The `ds` and `ds-spring` modules provide easy programatic access to NUTS nodes and the KIK-V datastation protocol.

`ds` provides DTOs for interacting with the protocol, including DTOs for the NUTS APIs necessary for the KIK-V program.  It pulls in JWT and JSON-LD/DID APIs.  Credentials necessary for KIK-V are implementeded to give direct access to them in parsed form.

`ds-spring` provides services for communicating directly with NUTS as well as the datastation protocol from Spring applciations, as well as serializing/deserializing to/from Jena datatypes.

### Notes

The NUTS functionality should optimally be extracted to a generic library supporting all of NUTS without the KIK-V extensions.

The VerifiablePresentation in the used library does not allow multiple VerifiableCredentials; there is an alternative used here that does.

This code includes partial DCAT implementation that is not used presently; it is not used presently, but the spec contains a mention so it may come back.

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

## DCAT2 Support

This project provides modular support for [DCAT2](https://www.w3.org/TR/vocab-dcat-2/) (W3C Data Catalog Vocabulary) for semantic interoperability of data catalogs.

### Modules
- **dcat**: Java DTOs for DCAT2 (no dependencies)
- **dcat-spring**: Spring and Jena integration for serialization, deserialization, and validation

### Features
- Java models for Catalog, Dataset, Distribution, Agent
- Jena-based RDF and JSON-LD serialization/deserialization
- Spring Boot auto-configuration for easy integration
- SHACL-based validation for DCAT2 compliance

### Usage Example
```java
// Create a Catalog
Catalog catalog = new Catalog();
catalog.setId("urn:mycatalog");
catalog.setTitle("My Catalog");
// ... add datasets, distributions, etc.

// Serialize to RDF/JSON-LD
Dcat2RdfService rdfService = new Dcat2RdfServiceImpl();
String jsonld = rdfService.toJsonLd(catalog);

// Validate
Dcat2Validator validator = new Dcat2Validator();
boolean isValid = validator.validate(catalog);
```

### Integration
- Add `dcat` and `dcat-spring` as dependencies in your Maven project.
- Use Spring Boot for auto-configuration, or instantiate services directly.

### Extending
- Add more DCAT2 fields or SHACL shapes as needed for your use case.

---
