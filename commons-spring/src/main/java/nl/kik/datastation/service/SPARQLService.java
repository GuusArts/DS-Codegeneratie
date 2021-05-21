package nl.kik.datastation.service;

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

import nl.kik.datastation.dto.ds.AskResult;
import nl.kik.datastation.dto.ds.Binding;
import nl.kik.datastation.dto.ds.ConstructResult;
import nl.kik.datastation.dto.ds.Header;
import nl.kik.datastation.dto.ds.RDFType;
import nl.kik.datastation.dto.ds.SelectBody;
import nl.kik.datastation.dto.ds.SelectResult;
import nl.kik.datastation.util.FunctionWrapper;

public class SPARQLService {
	public AskResult wrap(boolean result) {
		return AskResult.builder() //
				.head(Header.builder().build()) //
				.value(result) //
				.build();
	}

	public SelectResult wrap(ResultSet result) {
		return SelectResult.builder() //
				.head(Header.builder() //
						.vars(result.getResultVars()) //
						.build()) //
				.results(SelectBody.builder() //
						.bindings(wrapBindings(result)) //
						.build()) //
				.build();
	}

	protected List<Map<String, Binding>> wrapBindings(ResultSet result) {
		List<Map<String, Binding>> r = new ArrayList<>();
		result.forEachRemaining(FunctionWrapper.wrapper((QuerySolution s) -> {
			r.add(wrapSolution(s));
		}));
		return r;
	}

	protected Map<String, Binding> wrapSolution(QuerySolution s) throws ParseException {
		Map<String, Binding> r = new HashMap<>();
		for (Iterator<String> it = s.varNames(); it.hasNext();) {
			String name = it.next();
			r.put(name, wrapBinding(s.get(name)));
		}
		return r;
	}

	protected Binding wrapBinding(RDFNode rdfNode) throws ParseException {
		if (rdfNode.isLiteral()) {
			Literal literal = rdfNode.asLiteral();
			return Binding.builder() //
					.type(RDFType.literal) //
					.value(literal.getLexicalForm()) //
					.datatype(literal.getDatatypeURI()) //
					.language(literal.getLanguage()) //
					.build();
		} else if (rdfNode.isAnon()) {
			Resource resource = rdfNode.asResource();
			return Binding.builder() //
					.type(RDFType.bnode) //
					.value(resource.getId().getLabelString()) //
					.build();
		} else if (rdfNode.isURIResource()) {
			Resource resource = rdfNode.asResource();
			return Binding.builder() //
					.type(RDFType.bnode) //
					.value(resource.getURI()) //
					.build();
		} else {
			throw new ParseException("Received unexpected node " + rdfNode, 0);
		}
	}

	/**
	 * This serialization takes place in memory and is very unlikely to scale to
	 * large graphs. Then again, serializing a large model to JSON will never fly.
	 * 
	 * @param model
	 * @return
	 * @throws ParseException
	 */
	public ConstructResult wrap(Model model) throws ParseException {
		return ConstructResult.builder() //
				.data(parse(model)) //
				.build();
	}

	protected Map<String, Object> parse(Model model) throws ParseException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		RDFDataMgr.write(outputStream, model, RDFFormat.JSONLD_COMPACT_FLAT);
		return JSONObjectUtils.parse(outputStream.toString());
	}

	public Model unwrap(ConstructResult result, Supplier<Model> modelSupplier) {
		String serialized = result.getData().toString();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(serialized.getBytes());
		Model model = modelSupplier.get();
		RDFDataMgr.read(model, inputStream, Lang.JSONLD);
		return model;
	}

	public Model unwrap(ConstructResult result) {
		return unwrap(result, ModelFactory::createDefaultModel);
	}

}
