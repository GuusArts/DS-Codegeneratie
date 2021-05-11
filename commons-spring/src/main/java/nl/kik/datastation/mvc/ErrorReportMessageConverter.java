package nl.kik.datastation.mvc;

import java.util.Map;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;

import net.minidev.json.JSONObject;
import nl.kik.datastation.dto.ds.async.ErrorReport;
import nl.kik.datastation.service.MessageService;
import nl.kik.datastation.util.FunctionWrapper.FunctionWithException;

public class ErrorReportMessageConverter extends MessageMessageConverter<String, ErrorReport<String>> {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Class<ErrorReport<String>> getMessageClass() {
		return (Class) ErrorReport.class;
	}

	@Override
	protected Class<String> getBodyClass() {
		return String.class;
	}

	@Override
	protected FunctionWithException<JSONObject, String, Exception> getDecoder(HttpInputMessage inputMessage) {
		return o -> o.getAsString(MessageService.MESSAGE);
	}

	@Override
	protected FunctionWithException<String, JSONObject, Exception> getEncoder(HttpOutputMessage outputMessage) {
		return s -> new JSONObject(Map.of(MessageService.MESSAGE, s));
	}

}
