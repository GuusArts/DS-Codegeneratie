package nl.kik.commons.dcat2.dto;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * An Agent class for DCAT2.
 * 
 * An agent is a resource that acts or has the power to act.
 * Based on the FOAF Agent concept: http://xmlns.com/foaf/spec/#term_Agent
 * Used in DCAT2 for publishers, creators, etc.
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Agent extends DCATObject {
    
    /**
     * Name of the agent.
     */
    private String name;
    
    /**
     * Email address of the agent.
     */
    private String email;
    
    /**
     * URL of the agent's home page.
     */
    private URI homepage;
    
    /**
     * Type of agent, e.g., Person, Organization, etc.
     */
    private String type;
}
