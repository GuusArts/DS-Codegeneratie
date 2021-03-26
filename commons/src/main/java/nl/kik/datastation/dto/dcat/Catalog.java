package nl.kik.datastation.dto.dcat;

import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class Catalog extends Dataset {
//	foaf:Document homepage;
	Object themeTaxonomy;
	List<Resource> hasPart;
	List<Dataset> dataset;
	DataService service;
	Catalog catalog;
	CatalogRecord record;
	
}
