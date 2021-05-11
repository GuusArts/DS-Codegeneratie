package nl.kik.datastation.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;

import net.minidev.json.JSONObject;
import nl.kik.datastation.dto.ds.Result;
import nl.kik.datastation.dto.ds.async.Response;
import nl.kik.datastation.service.ResultService;
import nl.kik.datastation.util.FunctionWrapper.FunctionWithException;

public class ResponseMessageConverter extends MessageMessageConverter<Result, Response<Result>> {
	@Autowired
	private ResultService resultService;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Class<Response<Result>> getMessageClass() {
		return (Class) Response.class;
	}

	@Override
	protected Class<Result> getBodyClass() {
		return Result.class;
	}

	@Override
	protected FunctionWithException<JSONObject, Result, Exception> getDecoder(HttpInputMessage inputMessage) {
		return resultService::unwrap;
	}

	@Override
	protected FunctionWithException<Result, JSONObject, Exception> getEncoder(HttpOutputMessage outputMessage) {
		return resultService::wrap;
	}

}
