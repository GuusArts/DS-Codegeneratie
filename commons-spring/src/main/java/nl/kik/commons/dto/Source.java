package nl.kik.commons.dto;

import org.apache.jena.rdf.model.Resource;

public interface Source {
	void beginRead();

	void beginWrite();

	void commit();

	void end();

	Resource getResource(String uri);
}
