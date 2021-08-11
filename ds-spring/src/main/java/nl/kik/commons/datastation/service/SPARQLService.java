package nl.kik.commons.datastation.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import com.nimbusds.jose.util.JSONObjectUtils;

import nl.kik.commons.datastation.dto.ds.AskResult;
import nl.kik.commons.datastation.dto.ds.Binding;
import nl.kik.commons.datastation.dto.ds.ConstructResult;
import nl.kik.commons.datastation.dto.ds.Header;
import nl.kik.commons.datastation.dto.ds.RDFType;
import nl.kik.commons.datastation.dto.ds.SelectBody;
import nl.kik.commons.datastation.dto.ds.SelectResult;

public class SPARQLService {
	protected Map<String, Object> parse(final Model model) throws ParseException {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		RDFDataMgr.write(outputStream, model, RDFFormat.JSONLD_COMPACT_FLAT);
		return JSONObjectUtils.parse(outputStream.toString());
	}

	public Model unwrap(final ConstructResult result) {
		return unwrap(result, ModelFactory::createDefaultModel);
	}

	public Model unwrap(final ConstructResult result, final Supplier<Model> modelSupplier) {
		final String serialized = result.getData().toString();
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(serialized.getBytes());
		final Model model = modelSupplier.get();
		RDFDataMgr.read(model, inputStream, Lang.JSONLD);
		return model;
	}

	public AskResult wrap(final boolean result) {
		return AskResult.builder() //
				.head(Header.builder().build()) //
				.value(result) //
				.build();
	}

	/**
	 * This serialization takes place in memory and is very unlikely to scale to
	 * large graphs. Then again, serializing a large model to JSON will never fly.
	 *
	 * @param model
	 * @return
	 * @throws ParseException
	 */
	public ConstructResult wrap(final Model model) throws ParseException {
		return ConstructResult.builder() //
				.data(parse(model)) //
				.build();
	}

	public SelectResult wrap(final ResultSet result) {
		return SelectResult.builder() //
				.head(Header.builder() //
						.vars(result.getResultVars()) //
						.build()) //
				.results(SelectBody.builder() //
						.bindings(wrapBindings(result)) //
						.build()) //
				.build();
	}

	protected Binding wrapBinding(final RDFNode rdfNode) throws ParseException {
		if (rdfNode.isLiteral()) {
			final Literal literal = rdfNode.asLiteral();
			return Binding.builder() //
					.type(RDFType.literal) //
					.value(literal.getLexicalForm()) //
					.datatype(literal.getDatatypeURI()) //
					.language(literal.getLanguage()) //
					.build();
		}
		if (rdfNode.isAnon()) {
			final Resource resource = rdfNode.asResource();
			return Binding.builder() //
					.type(RDFType.bnode) //
					.value(resource.getId().getLabelString()) //
					.build();
		}
		if (rdfNode.isURIResource()) {
			final Resource resource = rdfNode.asResource();
			return Binding.builder() //
					.type(RDFType.uri) //
					.value(resource.getURI()) //
					.build();
		} else
			throw new ParseException("Received unexpected node " + rdfNode, 0);
	}

	protected List<Map<String, Binding>> wrapBindings(final ResultSet result) {
		final List<Map<String, Binding>> r = new ArrayList<>();
		while (result.hasNext()) {
			try {
				r.add(wrapSolution(result.next()));
			} catch (final ParseException e) {
				throw new RuntimeException(e);
			}
		}
		return r;
	}

	protected Map<String, Binding> wrapSolution(final QuerySolution s) throws ParseException {
		final Map<String, Binding> r = new HashMap<>();
		for (final Iterator<String> it = s.varNames(); it.hasNext();) {
			final String name = it.next();
			r.put(name, wrapBinding(s.get(name)));
		}
		return r;
	}

}
