package nl.kik.commons.datastation.service;

import nl.kik.commons.datastation.dto.didcomm.Message;

public interface DatastationService {
	void messageService(Message<?> message);
}
