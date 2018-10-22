package com.axelor.apps.event.service;

import java.util.List;

import com.axelor.apps.event.db.Discount;
import com.axelor.apps.event.db.Event;
import com.axelor.apps.event.db.EventRegistration;

public interface EventService {

	public void chackBeforeDays(Event event) throws Exception;

	public List<Discount> generateDiscount(Event event);

	public Event importRegistrationDataCsv(Event event);

	public int calculateTotalEntry(Event event) throws Exception;

	public List<EventRegistration> importRegistrationDataSave(List<EventRegistration> eventRegistration);

	public List<EventRegistration> calculateRegistrationAmount(Event event) throws Exception;
}
