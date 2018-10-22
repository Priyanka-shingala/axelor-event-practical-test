package com.axelor.apps.event.web;

import java.math.BigDecimal;
import com.axelor.apps.event.db.Event;
import com.axelor.apps.event.db.EventRegistration;
import com.axelor.apps.event.exceptions.IExceptionMessage;
import com.axelor.apps.event.service.EventRegistrationService;
import com.axelor.apps.event.service.EventService;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class EventRegistrationController {

	@Inject
	EventService eventService;

	@Inject
	EventRegistrationService eventRegistrationService;

	public void checkRegistrationDate(ActionRequest request, ActionResponse response) throws Exception {
		EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
		eventRegistrationService.checkRegistrationDate(eventRegistration);
	}

	public void checkEventCapacity(ActionRequest request, ActionResponse response) throws Exception {

		EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
		Event event = eventRegistration.getEvent();
		if (event != null) {
			int count = eventService.calculateTotalEntry(event);
			if (event.getCapacity() < count) {
				response.setError(I18n.get(IExceptionMessage.CHECK_CAPACITY));
			}
		}
	}

	public void calculateAmount(ActionRequest request, ActionResponse response) {
		EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
		Event event = eventRegistration.getEvent();
		if (event != null) {
			BigDecimal bigDecimal = eventRegistrationService.calculateAmount(event, eventRegistration);
			response.setValue("amount", bigDecimal);
		}
	}
}
