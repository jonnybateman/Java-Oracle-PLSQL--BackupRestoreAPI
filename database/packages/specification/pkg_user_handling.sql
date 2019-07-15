create or replace PACKAGE pkg_user_handling
IS
  FUNCTION check_user_name(p_user_name IN users_xml.user_name%TYPE)
      RETURN BOOLEAN;
      
  FUNCTION create_user(p_user_name IN users_xml.user_name%TYPE)
      RETURN NUMBER;
  
  FUNCTION modify_user(p_user_id IN users_xml.user_id%TYPE,
                      p_user_name IN users_xml.user_name%TYPE)
      RETURN NUMBER;
      
END pkg_user_handling;
