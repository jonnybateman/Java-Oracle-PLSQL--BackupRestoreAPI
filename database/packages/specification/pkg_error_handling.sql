create or replace PACKAGE pkg_error_handling
IS
  PROCEDURE record_error(user_id IN users_xml.user_id%TYPE);
  
  PROCEDURE record_exception(user_id IN users_xml.user_id%TYPE,
                            error_code IN error_log.error_code%TYPE,
                            message IN error_log.error_message%TYPE);
END pkg_error_handling;
