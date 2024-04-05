package nl.kik.commons.datastation.dto.dcat;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.dto.RDFObject;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Catalog extends Dataset {
//	foaf:Document homepage;
	Set<RDFObject> themeTaxonomy;
	Set<Resource> hasPart;
	Set<Dataset> dataset;
	Set<DataService> service;
	Set<Catalog> catalog;
	Set<CatalogRecord> record;
}
