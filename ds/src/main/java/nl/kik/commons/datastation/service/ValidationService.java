package nl.kik.commons.datastation.service;

import com.nimbusds.jose.JWSObject;

import nl.kik.commons.datastation.dto.kikv.ResultSet;

public interface ValidationService {

	JWSObject sign(ResultSet value) throws Exception;

}
