package nl.kik.commons.datastation.validation;

import java.util.List;

import nl.kik.commons.datastation.dto.dcat.Catalog;
import nl.kik.commons.datastation.dto.dcat.Dataset;
import nl.kik.commons.datastation.dto.dcat.Distribution;
import nl.kik.commons.dto.RDFObject;

/**
 * Interface for validating DCAT2 objects against the DCAT2 specification.
 */
public interface DCAT2Validator {
    
    /**
     * Validate a DCAT2 catalog
     * 
     * @param catalog The catalog to validate
     * @return A list of validation errors, empty if no errors
     */
    List<ValidationError> validateCatalog(Catalog catalog);
    
    /**
     * Validate a DCAT2 dataset
     * 
     * @param dataset The dataset to validate
     * @return A list of validation errors, empty if no errors
     */
    List<ValidationError> validateDataset(Dataset dataset);
    
    /**
     * Validate a DCAT2 distribution
     * 
     * @param distribution The distribution to validate
     * @return A list of validation errors, empty if no errors
     */
    List<ValidationError> validateDistribution(Distribution distribution);
    
    /**
     * Validate any DCAT2 object
     * 
     * @param object The object to validate
     * @return A list of validation errors, empty if no errors
     */
    List<ValidationError> validate(RDFObject object);
    
    /**
     * Class to represent a validation error
     */
    class ValidationError {
        private String property;
        private String message;
        private String code;
        
        public ValidationError(String property, String message, String code) {
            this.property = property;
            this.message = message;
            this.code = code;
        }
        
        public String getProperty() {
            return property;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getCode() {
            return code;
        }
        
        @Override
        public String toString() {
            return String.format("ValidationError{property='%s', message='%s', code='%s'}", property, message, code);
        }
    }
}