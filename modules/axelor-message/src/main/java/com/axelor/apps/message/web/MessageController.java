/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.message.web;

import com.axelor.apps.event.db.EventRegistration;
import com.axelor.apps.event.db.repo.EventRegistrationRepository;
import com.axelor.apps.event.service.EventRegistrationService;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.tool.ModelTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Singleton
public class MessageController {

	@Inject
	private MessageRepository messageRepo;

	@Inject
	private MessageService messageService;
	
	@Inject
	private EventRegistrationService eventRegistrationService;
	
	public void sendMessage(ActionRequest request, ActionResponse response) {
		Message message = request.getContext().asType(Message.class);

		try {
			message = messageService.sendMessage(messageRepo.find(message.getId()));
			response.setReload(true);

			if (message.getStatusSelect() == MessageRepository.STATUS_SENT) {

				if (message.getSentByEmail()) {
					Set<EmailAddress> email = message.getToEmailAddressSet();
					List<String> emailList = new ArrayList<>();
					for (EmailAddress emailAddress : email) {
						String str = emailAddress.getAddress();
						emailList.add(str);
					}

					List<EventRegistration> eventRegistration = Beans.get(EventRegistrationRepository.class).all()
							.filter("self.email IN (?)", emailList).fetch();
					eventRegistrationService.setSendMassage(eventRegistration);
					
					response.setFlash(I18n.get(IExceptionMessage.MESSAGE_4));
				} else {
					response.setFlash(I18n.get(IExceptionMessage.MESSAGE_5));
				}

			} else {
				response.setFlash(I18n.get(IExceptionMessage.MESSAGE_6));
			}
		} catch (AxelorException e) {
			TraceBackService.trace(response, e);
		}
	}

	@SuppressWarnings("unchecked")
	public void sendMessages(ActionRequest request, ActionResponse response) {
		List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
		try {
			if (idList == null) {
				throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD,
						I18n.get(IExceptionMessage.MESSAGE_MISSING_SELECTED_MESSAGES));
			}
			int error = ModelTool.apply(Message.class, idList, model -> messageService.sendMessage((Message) model));
			response.setFlash(String.format(I18n.get(IExceptionMessage.MESSAGES_SENT), idList.size() - error, error));
			response.setReload(true);
		} catch (AxelorException e) {
			TraceBackService.trace(response, e);
		}
	}

	@SuppressWarnings("unchecked")
	public void regenerateMessages(ActionRequest request, ActionResponse response) {
		List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
		try {
			if (idList == null) {
				throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD,
						I18n.get(IExceptionMessage.MESSAGE_MISSING_SELECTED_MESSAGES));
			}
			int error = ModelTool.apply(Message.class, idList,
					model -> messageService.regenerateMessage((Message) model));
			response.setFlash(
					String.format(I18n.get(IExceptionMessage.MESSAGES_REGENERATED), idList.size() - error, error));
			response.setReload(true);
		} catch (AxelorException e) {
			TraceBackService.trace(response, e);
		}
	}
}
