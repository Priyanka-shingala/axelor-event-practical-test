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
		LocalDate d2 = event.getRegistrationClose();
		BigDecimal dis = new BigDecimal(30);
		BigDecimal dis2 = new BigDecimal(50);
		BigDecimal dis3 = new BigDecimal(100);
		LocalDateTime dateTime = eventRegistration.getRegistrationDate();
		LocalDate d1 = dateTime.toLocalDate();
		long noOfDaysBetween = ChronoUnit.DAYS.between(d1, d2);
		int beforeDay = (int) noOfDaysBetween;
		Discount discount = new Discount();
		discount.setBeforeDays(beforeDay);
		if (beforeDay > 5) {
			discount.setDiscountPercent(dis2);
		} else {
			discount.setDiscountPercent(dis);
		}
		discount.setDiscountAmount((discount.getDiscountPercent().multiply(event.getEventFees())).divide(dis3));
		BigDecimal bigDecimal = event.getEventFees().subtract(discount.getDiscountAmount());
		return bigDecimal;
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
