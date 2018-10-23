package com.axelor.apps.event.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.axelor.apps.event.db.Discount;
import com.axelor.apps.event.db.Event;
import com.axelor.apps.event.db.EventRegistration;
import com.axelor.apps.event.db.repo.EventRegistrationRepository;
import com.axelor.apps.event.exceptions.IExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class EventRegistrationServiceImpl implements EventRegistrationService {

	@Override
	public void checkRegistrationDate(EventRegistration eventRegistration) throws Exception {
		LocalDateTime dateTime = eventRegistration.getRegistrationDate();
		if (dateTime != null) {
			LocalDate dateFromDateTime = dateTime.toLocalDate();
			Event event = eventRegistration.getEvent();
			if (event != null) {
				if (((dateFromDateTime.compareTo(event.getRegistrationClose())) <= 0)
						&& ((dateFromDateTime.compareTo(event.getRegistrationOpen())) >= 0)) {
					return;
				}
				throw new Exception(I18n.get(IExceptionMessage.REGISTRATION_DATE));
			}
		}
	}

	@Override
	public BigDecimal calculateAmount(Event event, EventRegistration eventRegistration) {
		Discount discount = event.getDiscounts().get(0);
		if (discount != null) {
			LocalDateTime dateTime = eventRegistration.getRegistrationDate();
			if (dateTime != null) {
				LocalDate dateFromDateTime = dateTime.toLocalDate();
				LocalDate d2 = event.getRegistrationClose();
				if (dateFromDateTime != null && d2 != null) {
					long noOfDaysBetween = ChronoUnit.DAYS.between(dateFromDateTime, d2);
					int beforeDay = (int) noOfDaysBetween;
					if (beforeDay >= discount.getBeforeDays()) {
						BigDecimal bigDecimal = event.getEventFees().subtract(discount.getDiscountAmount());
						return bigDecimal;
					} else {
						BigDecimal bigDecimal = event.getEventFees();
						return bigDecimal;
					}
				}
			}
		}
		return new BigDecimal(0);
	}

	@Override
	@Transactional
	public void setSendMassage(List<EventRegistration> eventRegistrations) {
		for (EventRegistration eventRegistration2 : eventRegistrations) {
			eventRegistration2.setSendEmail(Boolean.TRUE);
			Beans.get(EventRegistrationRepository.class).save(eventRegistration2);
		}
	}
}
