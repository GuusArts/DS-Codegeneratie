package nl.kik.commons.datastation.service.nuts;

import org.springframework.beans.factory.annotation.Autowired;

import com.nimbusds.jose.JWSObject;

import nl.kik.commons.datastation.dto.kikv.ResultSet;
import nl.kik.commons.datastation.dto.vc.VerifiablePresentation;
import nl.kik.commons.datastation.service.CryptoService;

public class NutsCryptoService implements CryptoService {
	@Autowired
	private NutsNode nuts;

	@Override
	public JWSObject sign(ResultSet value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultSet validate(JWSObject value) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void check(VerifiablePresentation vp) throws Exception {
		// TODO Auto-generated method stub

	}
}
