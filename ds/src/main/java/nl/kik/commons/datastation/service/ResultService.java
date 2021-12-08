package nl.kik.commons.datastation.service;

import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.nimbusds.jose.util.JSONObjectUtils;

import nl.kik.commons.datastation.dto.ds.AskResult;
import nl.kik.commons.datastation.dto.ds.AskResult.AskResultBuilder;
import nl.kik.commons.datastation.dto.ds.Binding;
import nl.kik.commons.datastation.dto.ds.ConstructResult;
import nl.kik.commons.datastation.dto.ds.Header;
import nl.kik.commons.datastation.dto.ds.Header.HeaderBuilder;
import nl.kik.commons.datastation.dto.ds.RDFType;
import nl.kik.commons.datastation.dto.ds.Result;
import nl.kik.commons.datastation.dto.ds.SPARQLResult;
import nl.kik.commons.datastation.dto.ds.SPARQLResult.SPARQLResultBuilder;
import nl.kik.commons.datastation.dto.ds.SelectBody;
import nl.kik.commons.datastation.dto.ds.SelectResult;
import nl.kik.commons.datastation.dto.ds.SelectResult.SelectResultBuilder;
import nl.kik.commons.datastation.util.FunctionWrapper;
import nl.kik.commons.datastation.util.FunctionWrapper.FunctionWithException;

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

	public Result unwrap(final Map<String, Object> object) throws ParseException {
		final Map<String, Object> head = JSONObjectUtils.getJSONObject(object, ResultService.HEAD);
		if (head != null) {
			return unwrapSPARQL(object, head) //
					.build();
		}
		final Map<String, Object> results = JSONObjectUtils.getJSONObject(object, ResultService.RESULTS);
		if (results != null) {
			return unwrapJSONLD(object, results) //
					.build();
		}
		return unwrapExtension(object);
	}

	protected AskResult.AskResultBuilder<?, ?> unwrapAsk(final Map<String, Object> object, final Boolean value)
			throws ParseException {
		final AskResultBuilder<?, ?> result = AskResult.builder() //

				.value(value) //
		;
		return unwrapExtension(result);
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Binding> unwrapBinding(final Map<String, Object> binding) throws ParseException {
		try {
			return binding.entrySet().stream() //
					.collect(Collectors.toMap(Entry::getKey, FunctionWrapper
							.wrapper((final Map.Entry<String, Object> e) -> unwrapBindingValue((Map<String, Object>) e.getValue()))));
		} catch (final RuntimeException e) {
			throw new ParseException("Failed parsing sub-elment: " + e.getCause().getMessage(), 0);
		}
	}

	protected Binding unwrapBindingValue(final Map<String, Object> o) throws ParseException {
		return Binding.builder() //
				.type(RDFType.valueOf(JSONObjectUtils.getString(o, ResultService.TYPE))) //
				.value(getRequiredString(o, ResultService.VALUE)) //
				.language(JSONObjectUtils.getString(o, ResultService.XML_LANG)) //
				.datatype(JSONObjectUtils.getString(o, ResultService.DATATYPE)) //
				.build();
	}

	protected SPARQLResult.SPARQLResultBuilder<?, ?> unwrapBody(final Map<String, Object> object) throws ParseException {
		final Map<String, Object> results = JSONObjectUtils.getJSONObject(object, ResultService.RESULTS);
		final Boolean value = getGeneric(object, ResultService.BOOLEAN, Boolean.class);
		if (results != null) {
			return unwrapSelect(object, results);
		}
		if (value != null) {
			return unwrapAsk(object, value);
		}
		return unwrapBodyExtension(object);
	}

	protected SPARQLResultBuilder<?, ?> unwrapBodyExtension(final Map<String, Object> object) throws ParseException {
		throw new ParseException("Received unknown SPARQLResult object", 0);
	}

	protected AskResultBuilder<?, ?> unwrapExtension(final AskResultBuilder<?, ?> result) throws ParseException {
		return result;
	}

	protected HeaderBuilder<?, ?> unwrapExtension(final HeaderBuilder<?, ?> result) throws ParseException {
		return result;
	}

	protected Result unwrapExtension(final Map<String, Object> object) throws ParseException {
		throw new ParseException("Received unknown Result object", 0);
	}

	protected SelectResultBuilder<?, ?> unwrapExtension(final SelectResultBuilder<?, ?> result) throws ParseException {
		return result;
	}

	protected SPARQLResultBuilder<?, ?> unwrapExtension(final SPARQLResultBuilder<?, ?> result) {
		return result;
	}

	protected Header.HeaderBuilder<?, ?> unwrapHead(final Map<String, Object> head) throws ParseException {
		final List<String> link = JSONObjectUtils.getStringList(head, ResultService.LINK);
		final List<String> vars = JSONObjectUtils.getStringList(head, ResultService.VARS);
		try {
			final HeaderBuilder<?, ?> result = Header.builder() //
					.link(link == null ? null
							: link.stream().map(FunctionWrapper.wrapper((final String s) -> new URL(s))).collect(Collectors.toList())) //
					.vars(vars) //
			;
			return unwrapExtension(result);
		} catch (final RuntimeException e) {
			throw new ParseException("Failed parsing sub-elment: " + e.getCause().getMessage(), 0);
		}
	}

	protected ConstructResult.ConstructResultBuilder<?, ?> unwrapJSONLD(final Map<String, Object> object,
			final Map<String, Object> results) {
		return ConstructResult.builder() //
				.data(results) //
		;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T, E extends Exception> Map<String, T> unwrapResultSet(final Map<String, Object> message,
			final FunctionWithException<Map<String, Object>, T, E> mapper) throws E, ParseException {
		try {
			return getList(message, ResultService.RESULTSET, (Class<Map<String, Object>>) (Class) Map.class).stream() //
					.collect(Collectors.toMap(
							FunctionWrapper.wrapper((final Map<String, Object> o) -> getRequiredString(o, ResultService.ID)),
							FunctionWrapper.wrapper(
									(final Map<String, Object> o) -> mapper.apply(getRequiredJSONObject(o, ResultService.RESULT)))));
		} catch (final RuntimeException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof ParseException) {
				throw (ParseException) cause;
			}
			throw (E) cause;
		}
	}

	protected SelectResult.SelectResultBuilder<?, ?> unwrapSelect(final Map<String, Object> object,
			final Map<String, Object> results) throws ParseException {
		final SelectResultBuilder<?, ?> result = SelectResult.builder() //
				.results(unwrapSelectBody(results)) //
		;
		return unwrapExtension(result);
	}

	protected SelectBody unwrapSelectBody(final Map<String, Object> results) throws ParseException {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<Map<String, Object>> bindings = getList(results, ResultService.BINDINGS,
				(Class<Map<String, Object>>) (Class) Map.class);
		try {
			return SelectBody.builder() //
					.bindings(bindings.stream().map(FunctionWrapper.wrapper((final Map<String, Object> o) -> unwrapBinding(o)))
							.collect(Collectors.toList())) //
					.build();
		} catch (final RuntimeException e) {
			throw new ParseException("Failed parsing sub-elment: " + e.getCause().getMessage(), 0);
		}
	}

	protected SPARQLResult.SPARQLResultBuilder<?, ?> unwrapSPARQL(final Map<String, Object> object,
			final Map<String, Object> head) throws ParseException {
		final Header header = unwrapHead(head) //
				.build();
		final SPARQLResultBuilder<?, ?> result = unwrapBody(object) //
				.head(header) //
		;
		return unwrapExtension(result);
	}

	protected Map<String, Object> wrap(final AskResult result, final Map<String, Object> json) throws ParseException {
		json.put(ResultService.BOOLEAN, result.isValue());
		return wrapExtension(result, json);
	}

	protected Map<String, Object> wrap(final Binding v) throws ParseException {
		final Map<String, Object> result = new HashMap<>();
		if (v.getType() != null) {
			result.put(ResultService.TYPE, v.getType().name());
		}
		if (v.getValue() != null) {
			result.put(ResultService.VALUE, v.getValue());
		}
		if (v.getDatatype() != null) {
			result.put(ResultService.DATATYPE, v.getDatatype());
		}
		if (v.getLanguage() != null) {
			result.put(ResultService.XML_LANG, v.getLanguage());
		}
		return wrapExtension(v, result);
	}

	protected Map<String, Object> wrap(final ConstructResult result, final Map<String, Object> json)
			throws ParseException {
		json.put(ResultService.RESULTS, result.getData());
		return wrapExtension(result, json);
	}

	protected Map<String, Object> wrap(final Header head) throws ParseException {
		final Map<String, Object> result = new HashMap<>();
		if (head.getLink() != null) {
			result.put(ResultService.LINK, head.getLink().stream().map(URL::toString).collect(Collectors.toList()));
		}
		if (head.getVars() != null) {
			result.put(ResultService.VARS, head.getVars());
		}
		return wrapExtension(head, result);
	}

	protected Map<String, Object> wrap(final Map<String, Binding> binding) throws ParseException {
		final Map<String, Object> result = new HashMap<>();
		binding.forEach(FunctionWrapper.wrapper((final String k, final Binding v) -> {
			result.put(k, wrap(v));
		}));
		return wrapExtension(binding, result);
	}

	public Map<String, Object> wrap(final Result result) throws ParseException {
		final Map<String, Object> json = new HashMap<>();
		if (result instanceof SPARQLResult) {
			return wrap((SPARQLResult) result, json);
		}
		return wrapExtension(result, json);
	}

	protected Map<String, Object> wrap(final SelectBody results) throws ParseException {
		final Map<String, Object> result = new HashMap<>();
		if (results.getBindings() != null) {
			try {
				result.put(ResultService.BINDINGS, results.getBindings().stream()
						.map(FunctionWrapper.wrapper((final Map<String, Binding> b) -> wrap(b))).collect(Collectors.toList()));
			} catch (final RuntimeException e) {
				throw new ParseException("Failed parsing sub-elment: " + e.getCause().getMessage(), 0);
			}
		}
		return wrapExtension(results, result);
	}

	protected Map<String, Object> wrap(final SelectResult result, final Map<String, Object> json) throws ParseException {
		json.put(ResultService.RESULTS, wrap(result.getResults()));
		return wrapExtension(result, json);
	}

	protected Map<String, Object> wrap(final SPARQLResult result, final Map<String, Object> json) throws ParseException {
		if (result instanceof AskResult) {
			json.put(ResultService.HEAD, wrap(result.getHead()));
			return wrap((AskResult) result, json);
		}
		if (result instanceof SelectResult) {
			json.put(ResultService.HEAD, wrap(result.getHead()));
			return wrap((SelectResult) result, json);
		}
		if (result instanceof ConstructResult) {
			return wrap((ConstructResult) result, json);
		}
		return wrapExtension(result, json);
	}

	protected Map<String, Object> wrapExtension(final AskResult result, final Map<String, Object> json)
			throws ParseException {
		return json;
	}

	protected Map<String, Object> wrapExtension(final Binding v, final Map<String, Object> result) throws ParseException {
		return result;
	}

	protected Map<String, Object> wrapExtension(final ConstructResult result, final Map<String, Object> json)
			throws ParseException {
		return json;
	}

	protected Map<String, Object> wrapExtension(final Header head, final Map<String, Object> result) {
		return result;
	}

	protected Map<String, Object> wrapExtension(final Map<String, Binding> binding, final Map<String, Object> result)
			throws ParseException {
		return result;
	}

	protected Map<String, Object> wrapExtension(final Result result, final Map<String, Object> json)
			throws ParseException {
		throw new ParseException("Received unexpected subclass of Result " + result.getClass().getCanonicalName(), 0);
	}

	protected Map<String, Object> wrapExtension(final SelectBody results, final Map<String, Object> result)
			throws ParseException {
		return result;
	}

	protected Map<String, Object> wrapExtension(final SelectResult result, final Map<String, Object> json)
			throws ParseException {
		return json;
	}

	protected Map<String, Object> wrapExtension(final SPARQLResult result, final Map<String, Object> json)
			throws ParseException {
		throw new ParseException("Received unexpected subclass of SPARQLResult " + result.getClass().getCanonicalName(), 0);
	}

	@SuppressWarnings("unchecked")
	public <E extends Exception> Map<String, Object> wrapResultSet(final Map<String, Result> s,
			final FunctionWithException<Result, Map<String, Object>, E> mapper) throws E, ParseException {
		try {
			return Map.of(ResultService.RESULTSET, s.entrySet().stream() //
					.map(FunctionWrapper.wrapper((final Map.Entry<String, Result> e) -> Map.of(//
							ResultService.ID, e.getKey(), //
							ResultService.RESULT, mapper.apply(e.getValue()) //
					))) //
					.collect(Collectors.toList()) //
			);
		} catch (final RuntimeException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof ParseException) {
				throw (ParseException) cause;
			}
			throw (E) cause;
		}
	}
}
