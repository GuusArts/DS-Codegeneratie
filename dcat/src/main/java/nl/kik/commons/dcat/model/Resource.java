package nl.kik.commons.dcat.model;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.dto.RDFObject;

/**
 * Base class for all DCAT2 resources.
 * 
 * In DCAT2, a Resource is an entity described by metadata in the catalog.
 * This is a super-class of dcat:Dataset, dcat:DataService, etc.
 * 
 * @see <a href="https://www.w3.org/TR/vocab-dcat-2/#Class:Resource">DCAT2 Resource</a>
 */
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Resource extends RDFObject {
    
    /**
     * A free-text account of the resource.
     */
    private String description;
    
    /**
     * A name given to the resource.
     */
    private String title;
    
    /**
     * A web page that can be navigated to in a web browser to gain access to the resource.
     */
    private URI landingPage;
    
    /**
     * Keywords or tags describing the resource.
     */
    private Set<String> keywords = new HashSet<>();
    
    /**
     * The main categories of the resource. A resource can have multiple themes.
     */
    private Set<URI> themes = new HashSet<>();
    
    /**
     * Relevant contact information for the resource.
     */
    private URI contactPoint;
    
    /**
     * Adds a keyword to the resource.
     * 
     * @param keyword The keyword to add
     * @return This resource for method chaining
     */
    public Resource addKeyword(String keyword) {
        if (keywords == null) {
            keywords = new HashSet<>();
        }
        keywords.add(keyword);
        return this;
    }
    
    /**
     * Adds a theme to the resource.
     * 
     * @param theme The theme to add
     * @return This resource for method chaining
     */
    public Resource addTheme(URI theme) {
        if (themes == null) {
            themes = new HashSet<>();
        }
        themes.add(theme);
        return this;
    }
}