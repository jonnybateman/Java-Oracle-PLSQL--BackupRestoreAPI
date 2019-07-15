create or replace PACKAGE BODY pkg_user_handling
IS
  FUNCTION check_user_name(p_user_name IN users_xml.user_name%TYPE)
      RETURN BOOLEAN
  IS
    l_user_exists NUMBER(1);
    
    CURSOR c_user_exists IS
      SELECT 1
      FROM users_xml
      WHERE user_name = p_user_name;
  
  BEGIN
    OPEN c_user_exists;
    FETCH c_user_exists INTO l_user_exists;
    
    IF c_user_exists%FOUND THEN
      RETURN TRUE;
    ELSE
      RETURN FALSE;
    END IF;
  
  END check_user_name;
  
  FUNCTION create_user(p_user_name IN users_xml.user_name%TYPE)
      RETURN NUMBER
  IS
    l_user_id users_xml.user_id%TYPE;
    
  BEGIN
    -- Check supplied user name does not already exist.
    IF NOT check_user_name(p_user_name) THEN
      -- User name does not exist, create new user record and return the user_id
      INSERT INTO users_xml (user_id,
                            user_name)
      VALUES (user_id_seq.NEXTVAL,
              p_user_name)
      RETURNING user_id INTO l_user_id;
      
      COMMIT;
      
      RETURN l_user_id;
    ELSE
      -- User name already exists, return 0 (no user_id).
      RETURN 0;
    END IF;
    
  END create_user;
  
  FUNCTION modify_user(p_user_id IN users_xml.user_id%TYPE,
                       p_user_name IN users_xml.user_name%TYPE)
      RETURN NUMBER
  IS
    l_affected_rows NUMBER(1);
    l_user_id       users_xml.user_id%TYPE;
    
  BEGIN
    -- Check the user name does not already exist.
    IF NOT check_user_name(p_user_name) THEN
    
      -- Change the user name for an existing user.
      UPDATE users_xml
      SET user_name = p_user_name
      WHERE user_id = p_user_id
      RETURNING user_id INTO l_user_id;
      
      COMMIT;
      
      RETURN l_user_id;
    
    ELSE
      RETURN 0; -- Username exists so return no user id.
    
    END IF;
    
  END modify_user;

END pkg_user_handling;
