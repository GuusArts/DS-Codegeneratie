package nl.kik.datastation.service;

import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nimbusds.jose.util.JSONObjectUtils;

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
import nl.kik.datastation.util.FunctionWrapper.FunctionWithException;

public class ResultService extends AbstractTokenService {
	private static final String RESULT = "result";
	private static final String ID = "id";
	private static final String RESULTSET = "resultset";
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

	public Map<String, Object> wrap(Result result) throws ParseException {
		Map<String, Object> json = new HashMap<>();
		if (result instanceof SPARQLResult) {
			return wrap((SPARQLResult) result, json);
		} else {
			return wrapExtension(result, json);
		}
	}

	protected Map<String, Object> wrapExtension(Result result, Map<String, Object> json) throws ParseException {
		throw new ParseException("Received unexpected subclass of Result " + result.getClass().getCanonicalName(), 0);
	}

	protected Map<String, Object> wrap(SPARQLResult result, Map<String, Object> json) throws ParseException {
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

	protected Map<String, Object> wrap(Header head) throws ParseException {
		Map<String, Object> result = new HashMap<>();
		if (head.getLink() != null) {
			result.put(LINK, head.getLink().stream().map(URL::toString).collect(Collectors.toList()));
		}
		if (head.getVars() != null) {
			result.put(VARS, head.getVars());
		}
		return wrapExtension(head, result);
	}

	protected Map<String, Object> wrapExtension(Header head, Map<String, Object> result) {
		return result;
	}

	protected Map<String, Object> wrapExtension(SPARQLResult result, Map<String, Object> json) throws ParseException {
		throw new ParseException("Received unexpected subclass of SPARQLResult " + result.getClass().getCanonicalName(),
				0);
	}

	protected Map<String, Object> wrap(AskResult result, Map<String, Object> json) throws ParseException {
		json.put(BOOLEAN, result.isValue());
		return wrapExtension(result, json);
	}

	protected Map<String, Object> wrapExtension(AskResult result, Map<String, Object> json) throws ParseException {
		return json;
	}

	protected Map<String, Object> wrap(SelectResult result, Map<String, Object> json) throws ParseException {
		json.put(RESULTS, wrap(result.getResults()));
		return wrapExtension(result, json);
	}

	protected Map<String, Object> wrap(ConstructResult result, Map<String, Object> json) throws ParseException {
		json.put(RESULTS, result.getData());
		return wrapExtension(result, json);
	}

	protected Map<String, Object> wrap(SelectBody results) throws ParseException {
		Map<String, Object> result = new HashMap<>();
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

	protected Map<String, Object> wrap(Map<String, Binding> binding) throws ParseException {
		Map<String, Object> result = new HashMap<>();
		binding.forEach(FunctionWrapper.wrapper((String k, Binding v) -> {
			result.put(k, wrap(v));
		}));
		return wrapExtension(binding, result);
	}

	protected Map<String, Object> wrapExtension(Map<String, Binding> binding, Map<String, Object> result) throws ParseException {
		return result;
	}

	protected Map<String, Object> wrap(Binding v) throws ParseException {
		Map<String, Object> result = new HashMap<>();
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

	protected Map<String, Object> wrapExtension(Binding v, Map<String, Object> result) throws ParseException {
		return result;
	}

	protected Map<String, Object> wrapExtension(SelectBody results, Map<String, Object> result) throws ParseException {
		return result;
	}

	protected Map<String, Object> wrapExtension(SelectResult result, Map<String, Object> json) throws ParseException {
		return json;
	}

	protected Map<String, Object> wrapExtension(ConstructResult result, Map<String, Object> json) throws ParseException {
		return json;
	}

	public Result unwrap(Map<String, Object> object) throws ParseException {
		Map<String, Object> head = JSONObjectUtils.getJSONObject(object, HEAD);
		if (head != null) {
			return unwrapSPARQL(object, head) //
					.build();
		} else {
			Map<String, Object> results = JSONObjectUtils.getJSONObject(object, RESULTS);
			if (results != null) {
				return unwrapJSONLD(object, results) //
						.build();
			}
			return unwrapExtension(object);
		}
	}

	protected ConstructResult.ConstructResultBuilder<?, ?> unwrapJSONLD(Map<String, Object> object, Map<String, Object> results) {
		return ConstructResult.builder() //
				.data(results) //
		;
	}

	protected SPARQLResult.SPARQLResultBuilder<?, ?> unwrapSPARQL(Map<String, Object> object, Map<String, Object> head)
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

	protected SPARQLResult.SPARQLResultBuilder<?, ?> unwrapBody(Map<String, Object> object) throws ParseException {
		Map<String, Object> results = JSONObjectUtils.getJSONObject(object, RESULTS);
		Boolean value = getGeneric(object, BOOLEAN, Boolean.class);
		if (results != null) {
			return unwrapSelect(object, results);
		} else if (value != null) {
			return unwrapAsk(object, value);
		} else {
			return unwrapBodyExtension(object);
		}
	}

	protected SPARQLResultBuilder<?, ?> unwrapBodyExtension(Map<String, Object> object) throws ParseException {
		throw new ParseException("Received unknown SPARQLResult object", 0);
	}

	protected AskResult.AskResultBuilder<?, ?> unwrapAsk(Map<String, Object> object, Boolean value) throws ParseException {
		AskResultBuilder<?, ?> result = AskResult.builder() //

				.value(value) //
		;
		return unwrapExtension(result);
	}

	protected AskResultBuilder<?, ?> unwrapExtension(AskResultBuilder<?, ?> result) throws ParseException {
		return result;
	}

	protected SelectResult.SelectResultBuilder<?, ?> unwrapSelect(Map<String, Object> object, Map<String, Object> results)
			throws ParseException {
		SelectResultBuilder<?, ?> result = SelectResult.builder() //
				.results(unwrapSelectBody(results)) //
		;
		return unwrapExtension(result);
	}

	protected SelectBody unwrapSelectBody(Map<String, Object> results) throws ParseException {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Map<String, Object>> bindings = getList(results, BINDINGS, (Class<Map<String, Object>>) (Class)Map.class);
		try {
			return SelectBody.builder() //
					.bindings(bindings.stream().map(FunctionWrapper.wrapper((Map<String, Object> o) -> unwrapBinding(o)))
							.collect(Collectors.toList())) //
					.build();
		} catch (RuntimeException e) {
			throw new ParseException("Failed parsing sub-elment: " + e.getCause().getMessage(), 0);
		}
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Binding> unwrapBinding(Map<String, Object> binding) throws ParseException {
		try {
			return binding.entrySet().stream() //
					.collect(Collectors.toMap(e -> e.getKey(), FunctionWrapper
							.wrapper((Map.Entry<String, Object> e) -> unwrapBindingValue((Map<String, Object>) e.getValue()))));
		} catch (RuntimeException e) {
			throw new ParseException("Failed parsing sub-elment: " + e.getCause().getMessage(), 0);
		}
	}

	protected Binding unwrapBindingValue(Map<String, Object> o) throws ParseException {
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

	protected Header.HeaderBuilder<?, ?> unwrapHead(Map<String, Object> head) throws ParseException {
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

	protected Result unwrapExtension(Map<String, Object> object) throws ParseException {
		throw new ParseException("Received unknown Result object", 0);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T, E extends Exception> Map<String, T> unwrapResultSet(Map<String, Object> message,
			FunctionWithException<Map<String, Object>, T, E> mapper) throws E, ParseException {
		try {
			return getList(message, RESULTSET, (Class<Map<String, Object>>) (Class)Map.class).stream() //
					.collect(Collectors.toMap(FunctionWrapper.wrapper((Map<String, Object> o) -> getRequiredString(o, ID)),
							FunctionWrapper.wrapper((Map<String, Object> o) -> mapper.apply(getRequiredJSONObject(o, RESULT)))));
		} catch (RuntimeException e) {
			Throwable cause = e.getCause();
			if (cause instanceof ParseException) {
				throw (ParseException) cause;
			}
			throw (E) cause;
		}
	}

	@SuppressWarnings("unchecked")
	public <E extends Exception> Map<String, Object> wrapResultSet(Map<String, Result> s,
			FunctionWithException<Result, Map<String, Object>, E> mapper) throws E, ParseException {
		try {
			return Map.of(RESULTSET, s.entrySet().stream() //
					.map(FunctionWrapper.wrapper((Map.Entry<String, Result> e) -> Map.of(//
							ID, e.getKey(), //
							RESULT, mapper.apply(e.getValue()) //
					))) //
					.collect(Collectors.toList()) //
			);
		} catch (RuntimeException e) {
			Throwable cause = e.getCause();
			if (cause instanceof ParseException) {
				throw (ParseException) cause;
			}
			throw (E) cause;
		}
	}
}
