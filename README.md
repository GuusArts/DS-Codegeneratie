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

# Building

Should build using Maven 3.8 using 

```
mvn clean install
```

# Branching

`master`: old version; ignore

`nuts`: fase 1 code; not actively developed

`fase2`: fase 2 code; backwards compatible with fase 1; active development goes here, strongly suggest using, also for phase 1

# Error reports

Create [issues](https://gitlab.opencirclesolutions.nl/zin/kik-v/datastation/-/issues) in Gitlab.

# Contributing

Make a [merge request](https://gitlab.opencirclesolutions.nl/zin/kik-v/datastation/-/merge_requests)
