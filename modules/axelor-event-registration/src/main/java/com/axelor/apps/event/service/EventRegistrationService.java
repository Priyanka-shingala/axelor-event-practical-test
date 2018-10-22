package com.axelor.apps.event.service;

import java.math.BigDecimal;
import java.util.List;

import com.axelor.apps.event.db.Event;
import com.axelor.apps.event.db.EventRegistration;

public interface EventRegistrationService {
	public void checkRegistrationDate(EventRegistration eventRegistration) throws Exception;

	public BigDecimal calculateAmount(Event event, EventRegistration eventRegistration);

	public void setSendMassage(List<EventRegistration> eventRegistrations);
}
