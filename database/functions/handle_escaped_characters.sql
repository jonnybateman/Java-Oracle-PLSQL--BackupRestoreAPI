create or replace FUNCTION handle_escaped_characters(p_check_string VARCHAR2)
RETURN VARCHAR2
IS
  TYPE v_escaped_character_type IS VARRAY(5) OF VARCHAR2(6);
  v_escaped_characters   v_escaped_character_type;
  
  TYPE v_readable_character_type IS VARRAY(5) OF VARCHAR2(6);
  v_readable_characters  v_readable_character_type;
  
  l_checked_string         VARCHAR2(60);
  l_temp_string            VARCHAR2(60);
  
BEGIN
  v_escaped_characters := v_escaped_character_type('&amp;'
                                                  ,'&gt;'
                                                  ,'&lt;'
                                                  ,'&quot;'
                                                  ,'&apos;');
  
  v_readable_characters := v_readable_character_type('&','>','<','"','''');
  
  l_temp_string := p_check_string;
  
  FOR i IN 1..5 LOOP
    l_checked_string := replace(l_temp_string, v_readable_characters(i),
        v_escaped_characters(i));
    l_temp_string := l_checked_string;
  END LOOP;
  
  RETURN l_checked_string;
END;
