package nl.kik.commons.dto;

import org.apache.jena.rdf.model.Resource;

public interface Source {
	void beginWrite();
	void beginRead();
	void commit();
	void end();
	
	Resource getResource(String uri);
}
