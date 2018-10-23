package com.axelor.apps.event.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.event.db.Discount;
import com.axelor.apps.event.db.Event;
import com.axelor.apps.event.db.EventRegistration;
import com.axelor.apps.event.db.repo.EventRegistrationRepository;
import com.axelor.apps.event.db.repo.EventRepository;
import com.axelor.apps.event.exceptions.IExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class EventServiceImpl implements EventService {

	@Override
	public void chackBeforeDays(Event event) throws Exception {
		List<Discount> discount = event.getDiscounts();
		LocalDate d1 = event.getRegistrationOpen();
		LocalDate d2 = event.getRegistrationClose();
		if (d1 != null && d2 != null) {
			long noOfDaysBetween = ChronoUnit.DAYS.between(d1, d2);
			if (discount != null) {
				for (Discount discount2 : discount) {
					if (noOfDaysBetween < discount2.getBeforeDays()) {
						throw new Exception(I18n.get(IExceptionMessage.BEFORE_DAYS));
					}
				}
			}
		}
	}

	@Override
	public List<Discount> generateDiscount(Event event) {
		LocalDate d2 = event.getRegistrationClose();
		List<Discount> myList = new ArrayList<Discount>();
		List<EventRegistration> eventRegistrations = event.getEventRegistrations();
		if (eventRegistrations != null) {
			for (EventRegistration eventRegistration : eventRegistrations) {
				LocalDateTime dateTime = eventRegistration.getRegistrationDate();
				if (dateTime != null) {
					LocalDate d1 = dateTime.toLocalDate();
					if(d1 != null && d2 != null) {
					long noOfDaysBetween = ChronoUnit.DAYS.between(d1, d2);
					int beforeDay = (int) noOfDaysBetween;
					Discount discount = calculateDiscount(event, beforeDay);
					myList.add(discount);
					}
				}
			}
			return myList;
		}
		return null;
	}

	@Override
	public Event importRegistrationDataCsv(Event event) {
		Long id = event.getId();
		Event event2 = Beans.get(EventRepository.class).all().filter("self.id = (?)", id).fetchOne();
		return event2;
	}

	@Override
	@Transactional
	public List<EventRegistration> importRegistrationDataSave(List<EventRegistration> eventRegistration) {
		for (EventRegistration eventRegistration2 : eventRegistration) {
			Beans.get(EventRegistrationRepository.class).save(eventRegistration2);
		}
		return eventRegistration;
	}

	@Override
	public int calculateTotalEntry(Event event) throws Exception {
		int count = 0;
		if (event != null) {
			List<EventRegistration> eventRegistration = Beans.get(EventRegistrationRepository.class).all()
					.filter("self.event = (?)", event).fetch();
			if (eventRegistration.size() != 0) {
				for (int i = 0; i < eventRegistration.size(); i++) {
					count++;
				}
				return count;
			}
		}
		return count;
	}

	@Override
	public List<EventRegistration> calculateRegistrationAmount(Event event) throws Exception {
		List<EventRegistration> eventRegistration = event.getEventRegistrations();
		if (eventRegistration != null) {
			List<EventRegistration> eventRegistrationsList = new ArrayList<>();
			if (event.getDiscounts().size() != 0) {
				Discount discount = event.getDiscounts().get(0);
				for (EventRegistration eventRegistration2 : eventRegistration) {
					LocalDateTime dateTime = eventRegistration2.getRegistrationDate();
					if (dateTime != null) {

						LocalDate dateFromDateTime = dateTime.toLocalDate();
						if (((dateFromDateTime.compareTo(event.getRegistrationClose())) <= 0)
								&& ((dateFromDateTime.compareTo(event.getRegistrationOpen())) >= 0)) {

							BigDecimal bigDecimal = event.getEventFees().subtract(discount.getDiscountAmount());
							eventRegistration2.setAmount(bigDecimal);
							eventRegistrationsList.add(eventRegistration2);
						} else {
							throw new Exception(I18n.get(IExceptionMessage.REGISTRATION_DATE) + " " + dateFromDateTime);
						}

					} else {
						eventRegistrationsList.add(eventRegistration2);
					}
				}
				return eventRegistrationsList;
			}
		}
		return eventRegistration;
	}

	public Discount calculateDiscount(Event event, int beforeDay) {
		BigDecimal dis = new BigDecimal(30);
		BigDecimal dis3 = new BigDecimal(100);
		Discount discount = new Discount();
		discount.setBeforeDays(beforeDay);
		discount.setDiscountPercent(dis);
		discount.setDiscountAmount((discount.getDiscountPercent().multiply(event.getEventFees())).divide(dis3));
		return discount;
	}
}
