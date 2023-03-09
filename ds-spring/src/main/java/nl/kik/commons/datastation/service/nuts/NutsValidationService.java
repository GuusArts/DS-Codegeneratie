package nl.kik.commons.datastation.service.nuts;

import org.springframework.beans.factory.annotation.Autowired;

import com.nimbusds.jose.JWSObject;

import nl.kik.commons.datastation.dto.kikv.ResultSet;
import nl.kik.commons.datastation.service.ValidationService;

public class NutsValidationService implements ValidationService {
	@Autowired
	private NutsNode nuts;

	@Override
	public JWSObject sign(ResultSet value) {
		// TODO Auto-generated method stub
		return null;
	}
}
