package org.sunbird.message;

/**
 * This interface will hold all the response key and message
 *
 * @author Amit Kumar
 */
public interface IResponseMessage extends IUserResponseMessage, IOrgResponseMessage {

  String INVALID_REQUESTED_DATA = "INVALID_REQUESTED_DATA {0}";
  String INVALID_OPERATION_NAME = "INVALID_OPERATION_NAME";
  String INTERNAL_ERROR = "INTERNAL_ERROR";
  String UNAUTHORIZED = "UNAUTHORIZED";
  String MANDATORY_PARAMETER_MISSING = "Mandatory parameter {0} is missing.";
  String INVALID_VALUE = "{0} VALUE IS INVALID, {1}";
  String DATA_TYPE_REQUIRED = "{0} SHOULD BE {1}";
}
