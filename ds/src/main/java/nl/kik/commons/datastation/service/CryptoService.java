package nl.kik.commons.datastation.service;

import com.nimbusds.jose.JWSObject;

import nl.kik.commons.datastation.dto.kikv.ResultSet;
import nl.kik.commons.datastation.dto.vc.VerifiablePresentation;

public interface CryptoService {

	JWSObject sign(ResultSet value) throws Exception;

	ResultSet validate(JWSObject value) throws Exception;

	void check(VerifiablePresentation vp) throws Exception;

}
