package nl.kik.datastation.service;

import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nimbusds.jose.util.JSONObjectUtils;

import net.minidev.json.JSONObject;
import nl.kik.datastation.dto.ds.AskResult;
import nl.kik.datastation.dto.ds.AskResult.AskResultBuilder;
import nl.kik.datastation.dto.ds.Binding;
import nl.kik.datastation.dto.ds.ConstructResult;
import nl.kik.datastation.dto.ds.Header;
import nl.kik.datastation.dto.ds.Header.HeaderBuilder;
import nl.kik.datastation.dto.ds.RDFType;
import nl.kik.datastation.dto.ds.Result;
import nl.kik.datastation.dto.ds.SPARQLResult;
import nl.kik.datastation.dto.ds.SPARQLResult.SPARQLResultBuilder;
import nl.kik.datastation.dto.ds.SelectBody;
import nl.kik.datastation.dto.ds.SelectResult;
import nl.kik.datastation.dto.ds.SelectResult.SelectResultBuilder;
import nl.kik.datastation.util.FunctionWrapper;

public class ResultService extends AbstractTokenService {
	private static final String XML_LANG = "xml:lang";
	private static final String DATATYPE = "datatype";
	private static final String VALUE = "value";
	private static final String TYPE = "type";
	private static final String BINDINGS = "bindings";
	private static final String RESULTS = "results";
	private static final String BOOLEAN = "boolean";
	private static final String VARS = "vars";
	private static final String LINK = "link";
	private static final String HEAD = "head";

	public JSONObject wrap(Result result) throws ParseException {
		JSONObject json = new JSONObject();
		if (result instanceof SPARQLResult) {
			return wrap((SPARQLResult) result, json);
		} else {
			return wrapExtension(result, json);
		}
	}

	protected JSONObject wrapExtension(Result result, JSONObject json) throws ParseException {
		throw new ParseException("Received unexpected subclass of Result " + result.getClass().getCanonicalName(), 0);
	}

	protected JSONObject wrap(SPARQLResult result, JSONObject json) throws ParseException {
		if (result instanceof AskResult) {
			json.put(HEAD, wrap(result.getHead()));
			return wrap((AskResult) result, json);
		} else if (result instanceof SelectResult) {
			json.put(HEAD, wrap(result.getHead()));
			return wrap((SelectResult) result, json);
		} else if (result instanceof ConstructResult) {
			return wrap((ConstructResult) result, json);
		} else {
			return wrapExtension(result, json);
		}
	}

	protected JSONObject wrap(Header head) throws ParseException {
		JSONObject result = new JSONObject();
		if (head.getLink() != null) {
			result.put(LINK, head.getLink().stream().map(URL::toString).collect(Collectors.toList()));
		}
		if (head.getVars() != null) {
			result.put(VARS, head.getVars());
		}
		return wrapExtension(head, result);
	}

	protected JSONObject wrapExtension(Header head, JSONObject result) {
		return result;
	}

	protected JSONObject wrapExtension(SPARQLResult result, JSONObject json) throws ParseException {
		throw new ParseException("Received unexpected subclass of SPARQLResult " + result.getClass().getCanonicalName(),
				0);
	}

	protected JSONObject wrap(AskResult result, JSONObject json) throws ParseException {
		json.put(BOOLEAN, result.isValue());
		return wrapExtension(result, json);
	}

	protected JSONObject wrapExtension(AskResult result, JSONObject json) throws ParseException {
		return json;
	}

	protected JSONObject wrap(SelectResult result, JSONObject json) throws ParseException {
		json.put(RESULTS, wrap(result.getResults()));
		return wrapExtension(result, json);
	}

	protected JSONObject wrap(ConstructResult result, JSONObject json) throws ParseException {
		json.put(RESULTS, result.getData());
		return wrapExtension(result, json);
	}

	protected JSONObject wrap(SelectBody results) throws ParseException {
		JSONObject result = new JSONObject();
		if (results.getBindings() != null) {
			try {
				result.put(BINDINGS,
						results.getBindings().stream().map(FunctionWrapper.wrapper((Map<String, Binding> b) -> wrap(b)))
								.collect(Collectors.toList()));
			} catch (RuntimeException e) {
				throw new ParseException("Failed parsing sub-elment: " + e.getCause().getMessage(), 0);
			}
		}
		return wrapExtension(results, result);
	}

	protected JSONObject wrap(Map<String, Binding> binding) throws ParseException {
		JSONObject result = new JSONObject();
		binding.forEach(FunctionWrapper.wrapper((String k, Binding v) -> {
			result.put(k, wrap(v));
		}));
		return wrapExtension(binding, result);
	}

	protected JSONObject wrapExtension(Map<String, Binding> binding, JSONObject result) throws ParseException {
		return result;
	}

	protected JSONObject wrap(Binding v) throws ParseException {
		JSONObject result = new JSONObject();
		if (v.getType() != null) {
			result.put(TYPE, v.getType().name());
		}
		if (v.getValue() != null) {
			result.put(VALUE, v.getValue());
		}
		if (v.getDatatype() != null) {
			result.put(DATATYPE, v.getDatatype());
		}
		if (v.getLanguage() != null) {
			result.put(XML_LANG, v.getLanguage());
		}
		return wrapExtension(v, result);
	}

	protected JSONObject wrapExtension(Binding v, JSONObject result) throws ParseException {
		return result;
	}

	protected JSONObject wrapExtension(SelectBody results, JSONObject result) throws ParseException {
		return result;
	}

	protected JSONObject wrapExtension(SelectResult result, JSONObject json) throws ParseException {
		return json;
	}

	protected JSONObject wrapExtension(ConstructResult result, JSONObject json) throws ParseException {
		return json;
	}

	public Result unwrap(JSONObject object) throws ParseException {
		JSONObject head = JSONObjectUtils.getJSONObject(object, HEAD);
		if (head != null) {
			return unwrapSPARQL(object, head) //
					.build();
		} else {
			JSONObject results = JSONObjectUtils.getJSONObject(object, RESULTS);
			if (results != null) {
				return unwrapJSONLD(object, results) //
						.build();
			}
			return unwrapExtension(object);
		}
	}

	protected ConstructResult.ConstructResultBuilder<?, ?> unwrapJSONLD(JSONObject object, JSONObject results) {
		return ConstructResult.builder() //
				.data(results) //
		;
	}

	protected SPARQLResult.SPARQLResultBuilder<?, ?> unwrapSPARQL(JSONObject object, JSONObject head)
			throws ParseException {
		Header header = unwrapHead(head) //
				.build();
		SPARQLResultBuilder<?, ?> result = unwrapBody(object) //
				.head(header) //
		;
		return unwrapExtension(result);
	}

	protected SPARQLResultBuilder<?, ?> unwrapExtension(SPARQLResultBuilder<?, ?> result) {
		return result;
	}

	protected SPARQLResult.SPARQLResultBuilder<?, ?> unwrapBody(JSONObject object) throws ParseException {
		JSONObject results = JSONObjectUtils.getJSONObject(object, RESULTS);
		Boolean value = getGeneric(object, BOOLEAN, Boolean.class);
		if (results != null) {
			return unwrapSelect(object, results);
		} else if (value != null) {
			return unwrapAsk(object, value);
		} else {
			return unwrapBodyExtension(object);
		}
	}

	protected SPARQLResultBuilder<?, ?> unwrapBodyExtension(JSONObject object) throws ParseException {
		throw new ParseException("Received unknown SPARQLResult object", 0);
	}

	protected AskResult.AskResultBuilder<?, ?> unwrapAsk(JSONObject object, Boolean value) throws ParseException {
		AskResultBuilder<?, ?> result = AskResult.builder() //
				.value(value) //
		;
		return unwrapExtension(result);
	}

	protected AskResultBuilder<?, ?> unwrapExtension(AskResultBuilder<?, ?> result) throws ParseException {
		return result;
	}

	protected SelectResult.SelectResultBuilder<?, ?> unwrapSelect(JSONObject object, JSONObject results)
			throws ParseException {
		SelectResultBuilder<?, ?> result = SelectResult.builder() //
				.results(unwrapSelectBody(results)) //
		;
		return unwrapExtension(result);
	}

	protected SelectBody unwrapSelectBody(JSONObject results) throws ParseException {
		List<JSONObject> bindings = getList(results, BINDINGS, JSONObject.class);
		try {
			return SelectBody.builder() //
					.bindings(bindings.stream().map(FunctionWrapper.wrapper((JSONObject o) -> unwrapBinding(o)))
							.collect(Collectors.toList())) //
					.build();
		} catch (RuntimeException e) {
			throw new ParseException("Failed parsing sub-elment: " + e.getCause().getMessage(), 0);
		}
	}

	protected Map<String, Binding> unwrapBinding(JSONObject binding) throws ParseException {
		try {
			return binding.entrySet().stream() //
					.collect(Collectors.toMap(e -> e.getKey(), FunctionWrapper
							.wrapper((Map.Entry<String, Object> e) -> unwrapBindingValue((JSONObject) e.getValue()))));
		} catch (RuntimeException e) {
			throw new ParseException("Failed parsing sub-elment: " + e.getCause().getMessage(), 0);
		}
	}

	protected Binding unwrapBindingValue(JSONObject o) throws ParseException {
		return Binding.builder() //
				.type(RDFType.valueOf(JSONObjectUtils.getString(o, TYPE))) //
				.value(getRequiredString(o, VALUE)) //
				.language(JSONObjectUtils.getString(o, XML_LANG)) //
				.datatype(JSONObjectUtils.getString(o, DATATYPE)) //
				.build();
	}

	protected SelectResultBuilder<?, ?> unwrapExtension(SelectResultBuilder<?, ?> result) throws ParseException {
		return result;
	}

	protected Header.HeaderBuilder<?, ?> unwrapHead(JSONObject head) throws ParseException {
		List<String> link = JSONObjectUtils.getStringList(head, LINK);
		List<String> vars = JSONObjectUtils.getStringList(head, VARS);
		try {
			HeaderBuilder<?, ?> result = Header.builder() //
					.link(link == null ? null
							: link.stream().map(FunctionWrapper.wrapper((String s) -> new URL(s)))
									.collect(Collectors.toList())) //
					.vars(vars) //
			;
			return unwrapExtension(result);
		} catch (RuntimeException e) {
			throw new ParseException("Failed parsing sub-elment: " + e.getCause().getMessage(), 0);
		}
	}

	protected HeaderBuilder<?, ?> unwrapExtension(HeaderBuilder<?, ?> result) throws ParseException {
		return result;
	}

	protected Result unwrapExtension(JSONObject object) throws ParseException {
		throw new ParseException("Received unknown Result object", 0);
	}
}
