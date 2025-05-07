package nl.kik.commons.datastation.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import nl.kik.commons.datastation.dto.dcat.Catalog;
import nl.kik.commons.datastation.dto.dcat.CatalogRecord;
import nl.kik.commons.datastation.dto.dcat.DCATObject;
import nl.kik.commons.datastation.dto.dcat.DataService;
import nl.kik.commons.datastation.dto.dcat.Dataset;
import nl.kik.commons.datastation.dto.dcat.DatasetSeries;
import nl.kik.commons.datastation.dto.dcat.Distribution;
import nl.kik.commons.datastation.dto.dcat.Resource;
import nl.kik.commons.dto.RDFObject;

/**
 * Implementation of DCAT2Validator that validates DCAT2 objects according to the DCAT2 specification.
 * Based on https://www.w3.org/TR/vocab-dcat-2/
 */
@Component
public class DCAT2ValidatorImpl implements DCAT2Validator {

    @Override
    public List<ValidationError> validateCatalog(Catalog catalog) {
        if (catalog == null) {
            return Collections.singletonList(
                    new ValidationError("catalog", "Catalog cannot be null", "DCAT2-NULL-CATALOG"));
        }
        
        List<ValidationError> errors = new ArrayList<>();
        
        // Validate base Resource properties
        errors.addAll(validateResource(catalog));
        
        // Catalog should have at least one dataset, service, or catalog
        if (CollectionUtils.isEmpty(catalog.getDataset()) && 
            CollectionUtils.isEmpty(catalog.getService()) && 
            CollectionUtils.isEmpty(catalog.getCatalog()) &&
            CollectionUtils.isEmpty(catalog.getDatasetSeries())) {
            errors.add(new ValidationError("catalog.content", 
                    "Catalog should contain at least one dataset, service, or catalog", 
                    "DCAT2-EMPTY-CATALOG"));
        }
        
        // Validate datasets
        if (!CollectionUtils.isEmpty(catalog.getDataset())) {
            for (Dataset dataset : catalog.getDataset()) {
                errors.addAll(validateDataset(dataset));
            }
        }
        
        // Validate dataset series
        if (!CollectionUtils.isEmpty(catalog.getDatasetSeries())) {
            for (DatasetSeries series : catalog.getDatasetSeries()) {
                errors.addAll(validateDatasetSeries(series));
            }
        }
        
        // Validate services
        if (!CollectionUtils.isEmpty(catalog.getService())) {
            for (DataService service : catalog.getService()) {
                errors.addAll(validateDataService(service));
            }
        }
        
        // Validate nested catalogs
        if (!CollectionUtils.isEmpty(catalog.getCatalog())) {
            for (Catalog nestedCatalog : catalog.getCatalog()) {
                errors.addAll(validateCatalog(nestedCatalog));
            }
        }
        
        // Validate catalog records
        if (!CollectionUtils.isEmpty(catalog.getRecord())) {
            for (CatalogRecord record : catalog.getRecord()) {
                errors.addAll(validateCatalogRecord(record));
            }
        }
        
        return errors;
    }

    @Override
    public List<ValidationError> validateDataset(Dataset dataset) {
        if (dataset == null) {
            return Collections.singletonList(
                    new ValidationError("dataset", "Dataset cannot be null", "DCAT2-NULL-DATASET"));
        }
        
        List<ValidationError> errors = new ArrayList<>();
        
        // Validate base Resource properties
        errors.addAll(validateResource(dataset));
        
        // Validate distributions
        if (!CollectionUtils.isEmpty(dataset.getDistribution())) {
            for (Distribution distribution : dataset.getDistribution()) {
                errors.addAll(validateDistribution(distribution));
            }
        }
        
        // Specific validations for datasets
        if (!(dataset instanceof DatasetSeries) && StringUtils.isBlank(dataset.getTitle())) {
            errors.add(new ValidationError("dataset.title", 
                    "Dataset must have a title", 
                    "DCAT2-MISSING-TITLE"));
        }
        
        if (!(dataset instanceof DatasetSeries) && StringUtils.isBlank(dataset.getDescription())) {
            errors.add(new ValidationError("dataset.description", 
                    "Dataset should have a description", 
                    "DCAT2-MISSING-DESCRIPTION"));
        }
        
        return errors;
    }
    
    protected List<ValidationError> validateDatasetSeries(DatasetSeries series) {
        if (series == null) {
            return Collections.singletonList(
                    new ValidationError("datasetSeries", "Dataset series cannot be null", "DCAT2-NULL-DATASET-SERIES"));
        }
        
        List<ValidationError> errors = new ArrayList<>(validateDataset(series));
        
        // A dataset series should ideally have at least one member
        if (CollectionUtils.isEmpty(series.getSeriesMembers())) {
            errors.add(new ValidationError("datasetSeries.seriesMembers", 
                    "Dataset series should have at least one member dataset", 
                    "DCAT2-EMPTY-SERIES"));
        } else {
            // Validate member datasets
            for (Dataset member : series.getSeriesMembers()) {
                errors.addAll(validateDataset(member));
            }
        }
        
        return errors;
    }

    @Override
    public List<ValidationError> validateDistribution(Distribution distribution) {
        if (distribution == null) {
            return Collections.singletonList(
                    new ValidationError("distribution", "Distribution cannot be null", "DCAT2-NULL-DISTRIBUTION"));
        }
        
        List<ValidationError> errors = new ArrayList<>();
        
        // Validate base DCATObject properties
        errors.addAll(validateDCATObject(distribution));
        
        // Distribution should have either accessURL or downloadURL
        if (CollectionUtils.isEmpty(distribution.getAccessURL()) && 
            CollectionUtils.isEmpty(distribution.getDownloadURL()) &&
            CollectionUtils.isEmpty(distribution.getAccessService())) {
            errors.add(new ValidationError("distribution.access", 
                    "Distribution should have either accessURL, downloadURL, or accessService", 
                    "DCAT2-NO-ACCESS-POINT"));
        }
        
        // If byteSize is provided, it should be positive
        if (distribution.getByteSize() != null && distribution.getByteSize() <= 0) {
            errors.add(new ValidationError("distribution.byteSize", 
                    "ByteSize must be positive", 
                    "DCAT2-INVALID-BYTE-SIZE"));
        }
        
        return errors;
    }
    
    protected List<ValidationError> validateDataService(DataService service) {
        if (service == null) {
            return Collections.singletonList(
                    new ValidationError("dataService", "Data service cannot be null", "DCAT2-NULL-DATA-SERVICE"));
        }
        
        List<ValidationError> errors = new ArrayList<>();
        
        // Validate base Resource properties
        errors.addAll(validateResource(service));
        
        // Data service should have endpointURL
        if (service.getEndpointURL() == null) {
            errors.add(new ValidationError("dataService.endpointURL", 
                    "Data service should have an endpoint URL", 
                    "DCAT2-MISSING-ENDPOINT-URL"));
        }
        
        return errors;
    }
    
    protected List<ValidationError> validateCatalogRecord(CatalogRecord record) {
        if (record == null) {
            return Collections.singletonList(
                    new ValidationError("catalogRecord", "Catalog record cannot be null", "DCAT2-NULL-CATALOG-RECORD"));
        }
        
        List<ValidationError> errors = new ArrayList<>();
        
        // Validate base DCATObject properties
        errors.addAll(validateDCATObject(record));
        
        // Catalog record must have a primary topic
        if (record.getPrimaryTopic() == null) {
            errors.add(new ValidationError("catalogRecord.primaryTopic", 
                    "Catalog record must have a primary topic", 
                    "DCAT2-MISSING-PRIMARY-TOPIC"));
        }
        
        return errors;
    }
    
    protected List<ValidationError> validateResource(Resource resource) {
        if (resource == null) {
            return Collections.singletonList(
                    new ValidationError("resource", "Resource cannot be null", "DCAT2-NULL-RESOURCE"));
        }
        
        List<ValidationError> errors = new ArrayList<>();
        
        // Validate base DCATObject properties
        errors.addAll(validateDCATObject(resource));
        
        // Resources should have titles
        if (StringUtils.isBlank(resource.getTitle())) {
            errors.add(new ValidationError("resource.title", 
                    "Resource should have a title", 
                    "DCAT2-MISSING-TITLE"));
        }
        
        return errors;
    }
    
    protected List<ValidationError> validateDCATObject(DCATObject object) {
        if (object == null) {
            return Collections.singletonList(
                    new ValidationError("object", "DCAT object cannot be null", "DCAT2-NULL-OBJECT"));
        }
        
        List<ValidationError> errors = new ArrayList<>();
        
        // DCATObjects should have IDs
        if (StringUtils.isBlank(object.getId())) {
            errors.add(new ValidationError("object.id", 
                    "DCAT object should have an ID", 
                    "DCAT2-MISSING-ID"));
        }
        
        return errors;
    }

    @Override
    public List<ValidationError> validate(RDFObject object) {
        if (object == null) {
            return Collections.singletonList(
                    new ValidationError("object", "Object cannot be null", "DCAT2-NULL-OBJECT"));
        }
        
        if (object instanceof Catalog) {
            return validateCatalog((Catalog) object);
        } else if (object instanceof Dataset) {
            if (object instanceof DatasetSeries) {
                return validateDatasetSeries((DatasetSeries) object);
            }
            return validateDataset((Dataset) object);
        } else if (object instanceof Distribution) {
            return validateDistribution((Distribution) object);
        } else if (object instanceof DataService) {
            return validateDataService((DataService) object);
        } else if (object instanceof CatalogRecord) {
            return validateCatalogRecord((CatalogRecord) object);
        } else if (object instanceof Resource) {
            return validateResource((Resource) object);
        } else if (object instanceof DCATObject) {
            return validateDCATObject((DCATObject) object);
        }
        
        // Not a DCAT2 object - no specific validations
        return Collections.emptyList();
    }
}