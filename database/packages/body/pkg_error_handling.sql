create or replace PACKAGE BODY pkg_error_handling
IS
  PROCEDURE record_error(user_id IN users_xml.user_id%TYPE)
  IS
    l_code  PLS_INTEGER := SQLCODE;
    l_mesg  VARCHAR2(32767) := SQLERRM;
  BEGIN
    INSERT INTO error_log(error_id
                          ,error_code
                          ,error_message
                          ,backtrace
                          ,callstack
                          ,created_on
                          ,created_by_user)
    VALUES (error_id_seq.NEXTVAL
            ,l_code
            ,l_mesg
            ,SYS.DBMS_UTILITY.format_error_backtrace
            ,SYS.DBMS_UTILITY.format_call_stack
            ,SYSDATE
            ,user_id);
  END record_error;
  
  PROCEDURE record_exception(user_id IN users_xml.user_id%TYPE,
                            error_code IN error_log.error_code%TYPE,
                            message IN error_log.error_message%TYPE)
  IS
  BEGIN
    INSERT INTO exception_log(exception_id
                              ,exception_err
                              ,exception_msg
                              ,user_id
                              ,created_on)
    VALUES (exception_id_seq.NEXTVAL
            ,error_code
            ,message
            ,user_id
            ,SYSDATE);
  END record_exception;

END pkg_error_handling;
