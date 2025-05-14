package nl.kik.commons.datastation.dto.dcat;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * A collection of datasets that share certain characteristics.
 * Based on DCAT2 specification: https://www.w3.org/TR/vocab-dcat-2/#Class:Dataset_Series
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class DatasetSeries extends Dataset {
    // The datasets that belong to this series
    Set<Dataset> seriesMembers;
}