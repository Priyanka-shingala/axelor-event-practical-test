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
package com.axelor.apps.event.exceptions;

/**
 * Interface of Exceptions.
 *
 * @author dubaux
 */
public interface IExceptionMessage {

  public static final String CHECK_CAPACITY = /*$$(*/ "Event capacity is full, please try next year." /*)*/;

  static final String REGISTRATION_DATE =/*$$(*/ "Registration date is not between registration start and end date" /*)*/;
  static final String BEFORE_DAYS =/*$$(*/ "Before Days exceed duration between open and close registration dates" /*)*/;
  static final String VALIDATE_FILE_TYPE =/*$$(*/ "Please import only csv file." /*)*/;
  static final String IMPORT_OK =/*$$(*/ "Data imported succesfully" /*)*/;
  static final String FILE_FORMATE =/*$$(*/ "File formate is wrong" /*)*/;
  static final String EVENT_CAPACITY =/*$$(*/ "Sorry, Event Capacity is full" /*)*/;
  static final String HALF_EVENT_CAPACITY =/*$$(*/ "Sorry, Half data imported succesfully than Event Capacity is full" /*)*/;


}
