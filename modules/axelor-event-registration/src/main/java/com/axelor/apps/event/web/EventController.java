package com.axelor.apps.event.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.impl.common.IOUtil;

import com.axelor.apps.event.db.Discount;
import com.axelor.apps.event.db.Event;
import com.axelor.apps.event.db.EventRegistration;
import com.axelor.apps.event.exceptions.IExceptionMessage;
import com.axelor.apps.event.service.EventService;
import com.axelor.data.Listener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.opencsv.CSVReader;

public class EventController {

	@Inject
	MetaFileRepository metaFileRepo;

	@Inject
	MetaFiles metaFiles;

	@Inject
	EventService eventService;

	public void calculateTotalEntry(ActionRequest request, ActionResponse response) throws Exception {
		Event event = request.getContext().asType(Event.class);
		int count = eventService.calculateTotalEntry(event);
		response.setValue("totalEntry", count);

		if (event.getCapacity() < count) {
			response.setError(I18n.get(IExceptionMessage.CHECK_CAPACITY));
		}
	}

	public void chackBeforeDays(ActionRequest request, ActionResponse response) throws Exception {
		Event event = request.getContext().asType(Event.class);
		eventService.chackBeforeDays(event);
	}

	public void calculateAmount(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		List<EventRegistration> eventRegistrations = event.getEventRegistrations();
		BigDecimal dis = new BigDecimal(0);
		if (eventRegistrations != null) {
			for (EventRegistration eventRegistration : eventRegistrations) {
				dis = dis.add(eventRegistration.getAmount());
			}
			response.setValue("amountCollected", dis);
			List<Discount> discounts = event.getDiscounts();
			BigDecimal dis2 = new BigDecimal(0);
			for (Discount discount : discounts) {
				dis2 = dis2.add(discount.getDiscountAmount());
			}
			response.setValue("totalDiscount", dis2);
		}
	}

	public void generateDiscount(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		List<Discount> myList = eventService.generateDiscount(event);

		response.setValue("discounts", myList);
	}

	public void importRegistrationDataCsv(ActionRequest req, ActionResponse res) throws IOException {
		try {
			MetaFile metaFile = metaFileRepo
					.find(Long.valueOf(((Map<?, ?>) req.getContext().get("importFile")).get("id").toString()));
			File csvFile = MetaFiles.getPath(metaFile).toFile();

			if (Files.getFileExtension(csvFile.getName()).equals("csv")) {

				CSVReader reader = new CSVReader(new FileReader(csvFile));
				String[] header = reader.readNext();
				List<String> headers = Arrays.asList(header);
				List<String> expectedHeaders = Arrays.asList("name", "email", "registrationDate", "amount", "address",
						"event");
				if (expectedHeaders.equals(headers)) {
					String config = "/data-csv-import/input-config.xml";
					InputStream inputStream = this.getClass().getResourceAsStream(config);
					File configFile = File.createTempFile("config", ".xml");
					FileOutputStream fout = new FileOutputStream(configFile);
					IOUtil.copyCompletely(inputStream, fout);

					File tempDir = Files.createTempDir();
					File importFile = new File(tempDir, "EventRegis.csv");
					Files.copy(csvFile, importFile);
					importFile = importFile.getParentFile();
					Event event = req.getContext().asType(Event.class);
					event = eventService.importRegistrationDataCsv(event);

					if (event.getCapacity() > event.getTotalEntry()) {

						CSVImporter csvImporter = new CSVImporter(configFile.getAbsolutePath(),
								importFile.getAbsolutePath());
						csvImporter.addListener(new Listener() {

							@Override
							public void imported(Integer total, Integer success) {
								System.out.println("Total" + total + " : " + "Success" + success);
							}

							@Override
							public void imported(Model bean) {
								Event event = req.getContext().asType(Event.class);
								Event event2 = eventService.importRegistrationDataCsv(event);
								try {
									int count = eventService.calculateTotalEntry(event2);

									if (event2.getCapacity() > count) {

										((EventRegistration) bean).setEvent(event2);
										EventRegistration obj = ((EventRegistration) bean);
										List<EventRegistration> eventRegistrations = event2.getEventRegistrations();
										eventRegistrations.add(obj);
										event2.setEventRegistrations(eventRegistrations);
										eventRegistrations = eventService
												.importRegistrationDataSave(eventRegistrations);
									} else {
										res.setError(I18n.get(IExceptionMessage.HALF_EVENT_CAPACITY));
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							@Override
							public void handle(Model bean, Exception e) {
								System.out.println(e.getMessage());
							}
						});
						csvImporter.run();
						res.setFlash(I18n.get(IExceptionMessage.IMPORT_OK));
						reader.close();
					} else {
						res.setError(I18n.get(IExceptionMessage.EVENT_CAPACITY));
					}
					FileUtils.forceDelete(configFile);

					FileUtils.forceDelete(tempDir);

					if (metaFile != null) {
						metaFileRepo.remove(metaFile);
					}
				} else {
					res.setError(I18n.get(IExceptionMessage.FILE_FORMATE));
				}
			} else {
				res.setError(I18n.get(IExceptionMessage.VALIDATE_FILE_TYPE));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void calculateRegistrationAmount(ActionRequest request, ActionResponse response) throws Exception {
		Event event = request.getContext().asType(Event.class);
		List<EventRegistration> eventRegistrationsList = eventService.calculateRegistrationAmount(event);
		response.setValue("eventRegistrations", eventRegistrationsList);

	}

}
