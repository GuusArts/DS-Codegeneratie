package nl.kik.commons.datastation.service.nuts;

import org.springframework.beans.factory.annotation.Autowired;

import nl.kik.commons.datastation.service.ValidationService;

public class NutsValidationService implements ValidationService {
	@Autowired
	private NutsNode nuts;
}
