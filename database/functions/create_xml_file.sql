create or replace FUNCTION create_xml_file(p_user_id IN users_xml.user_id%TYPE)
RETURN VARCHAR2
IS
  l_exists              VARCHAR2(1);
  l_file                UTL_FILE.FILE_TYPE;
  l_file_name           VARCHAR2(40);
  l_cursor_id           NUMBER;
  l_col_count           NUMBER;
  l_col_name            VARCHAR2(30);
  l_row_count           NUMBER;
  l_coll_num_indx       NUMBER(1) := 1;
  l_coll_date_indx      NUMBER(1) := 1;
  l_coll_varchar_indx   NUMBER(1) := 1;
  l_record_tag          VARCHAR2(30);
  l_number_tab_value    NUMBER;
  l_varchar_tab_value   VARCHAR2(60);
  l_searched_tab_value  VARCHAR2(60);
  l_date_tab_value      DATE;
  l_desc_tab            DBMS_SQL.desc_tab;
  t_number              DBMS_SQL.number_table;
  t_date                DBMS_SQL.date_table;
  t_varchar2            DBMS_SQL.varchar2_table;
  sql_stmt              VARCHAR2(100);
  
  no_user_exists        EXCEPTION;
  
  TYPE t_ref_cur_table_data IS REF CURSOR;
    c_cursor            t_ref_cur_table_data;
    
  TYPE rec_dbms_sql_number_table IS RECORD (
      indx              NUMBER(1),
      num_tab           DBMS_SQL.number_table);
  TYPE type_dbms_sql_number_table IS TABLE OF rec_dbms_sql_number_table INDEX BY PLS_INTEGER;
    tab_dbms_sql_number_table type_dbms_sql_number_table;
  
  TYPE rec_dbms_sql_varchar_table IS RECORD (
      indx              NUMBER(1),
      varchar_tab       DBMS_SQL.varchar2_table);
  TYPE type_dbms_sql_varchar_table IS TABLE OF rec_dbms_sql_varchar_table INDEX BY PLS_INTEGER;
    tab_dbms_sql_varchar_table type_dbms_sql_varchar_table;
  
  Type rec_dbms_sql_date_table IS RECORD (
      indx              NUMBER(1),
      date_tab          DBMS_SQL.date_table);
  TYPE type_dbms_sql_date_table IS TABLE of rec_dbms_sql_date_table INDEX BY PLS_INTEGER;
    tab_dbms_sql_date_table type_dbms_sql_date_table;
  
  CURSOR c_tables IS
    SELECT table_name
    FROM user_tables
    WHERE table_name LIKE '%_XML'
    AND table_name != 'USERS_XML';
    
BEGIN

  -- Check user exists.
  -- Check the user exists before proceeding.
  SELECT CASE
            WHEN EXISTS (SELECT 1
                         FROM users_xml
                         WHERE user_id = p_user_id)
              THEN 'Y'
              ELSE 'N'
         END
  INTO l_exists
  FROM dual;
  
  IF l_exists = 'N' THEN
    RAISE no_user_exists;
  END IF;

  -- Generate the file name for the file to be written to.
  l_file_name := TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISS') || '_' || p_user_id ||
      '_XML_RESTORE.xml';
  -- Create and open the file ready for writing to.
  l_file := UTL_FILE.fopen('XMLDIR', l_file_name, 'w');
  
  -- Write the opening user tag to the file.
  UTL_FILE.put_line(l_file, '<user id="' || p_user_id || '">');
  
  -- Loop through those tables that are to have data extracted and written to the file.
  <<table_list>>
  FOR r_tables IN c_tables LOOP
  
    -- Write the current table tag to the file
    UTL_FILE.put_line(l_file, '<' || LOWER(REPLACE(r_tables.table_name, '_XML')) || '>');
    
    -- Check there are rows for the current table.
    sql_stmt := 'SELECT COUNT(*) FROM ' || r_tables.table_name || '  WHERE user_id = :x';

    EXECUTE IMMEDIATE sql_stmt
      INTO l_row_count
      USING p_user_id;
    
    -- If there are rows to be processed for the current table.
    IF l_row_count > 0 THEN
    
      -- Get the current table's xml tag that represents a single record.
      SELECT REPLACE(LOWER(comments), 'xml_tag:')
      INTO l_record_tag
      FROM user_tab_comments
      WHERE table_name = r_tables.table_name;
      
      OPEN c_cursor FOR 'SELECT * FROM ' || r_tables.table_name || ' WHERE user_id = ' || p_user_id;
    
      -- Switch from native dynamic SQL to DBMS_SQL package.
      l_cursor_id := DBMS_SQL.to_cursor_number(c_cursor);
      -- Retrieve table's columns, column data types and number of columns.
      DBMS_SQL.describe_columns(l_cursor_id, l_col_count, l_desc_tab);
    
      -- Define an array to hold values for each of the columns.
      FOR i IN 1..l_col_count LOOP -- for each column..

        IF l_desc_tab(i).col_type = 2 THEN
          DBMS_SQL.define_array(l_cursor_id, i, t_number, l_row_count, 1);
        ELSIF l_desc_tab(i).col_type = 12 THEN
          DBMS_SQL.define_array(l_cursor_id, i, t_date, l_row_count, 1);
        ELSIF l_desc_tab(i).col_type = 1 THEN
          DBMS_SQL.define_array(l_cursor_id, i, t_varchar2, l_row_count, 1);
        END IF;
      
      END LOOP;
    
      -- Get the rows from the cursor.
      l_row_count := DBMS_SQL.fetch_rows(l_cursor_id);
      
      -- Put the retrieved rows into the defined column arrays for the current table.
      <<col_retrieve_values_loop>>
      FOR i IN 1..l_col_count LOOP
        
        IF l_desc_tab(i).col_name != 'USER_ID' THEN

          IF l_desc_tab(i).col_type = 2 THEN
            -- Retrieve all values for current column and place them in assigned collection.
            DBMS_SQL.column_value(l_cursor_id, i, t_number);
            -- Assign the collection to the DBS_SQL.number_table collection.
            tab_dbms_sql_number_table(l_coll_num_indx).num_tab := t_number;
            -- Set the index for the DBMS_SQL.number_table relating to the current column.
            tab_dbms_sql_number_table(l_coll_num_indx).indx := i;
            l_coll_num_indx := l_coll_num_indx + 1;
            
          ELSIF l_desc_tab(i).col_type = 12 THEN
            -- Retrieve all values for current column and place them in assigned collection.
            DBMS_SQL.column_value(l_cursor_id, i, t_date);
            -- Assign the collection to the DBS_SQL.number_table collection.
            tab_dbms_sql_date_table(l_coll_date_indx).date_tab := t_date;
            -- Set the index for the DBMS_SQL.date_table relating to the current column.
            tab_dbms_sql_date_table(l_coll_date_indx).indx := i;
            l_coll_date_indx := l_coll_date_indx + 1;
            
          ELSIF l_desc_tab(i).col_type = 1 THEN
            -- Retrieve all values for current column and place them in assigned collection.
            DBMS_SQL.column_value(l_cursor_id, i, t_varchar2);
            -- Assign the collection to the DBS_SQL.number_table collection.
            tab_dbms_sql_varchar_table(l_coll_varchar_indx).varchar_tab := t_varchar2;
            -- Set the index for the DBMS_SQL.varchar2_table relating to the current column.
            tab_dbms_sql_varchar_table(l_coll_varchar_indx).indx := i;
            l_coll_varchar_indx := l_coll_varchar_indx + 1;
      
          END IF;
          
        END IF;
        
      END LOOP col_retrieve_values_loop;
      
      -- Close the cursor for the current table
      DBMS_SQL.close_cursor(l_cursor_id);
      
      -- Loop through each row for the current table.
      <<table_row_list>>
      FOR i IN 1..l_row_count LOOP
      
        -- Write the opening record XML tag to the file.
        UTL_FILE.put_line(l_file, '    <' || l_record_tag || '>');
        
        -- For each row search the column arrays to find the values for that row.
        <<table_column_list>>
        FOR j IN 1..l_col_count LOOP
          -- Need to search through the various table of collections to find the collection
          -- corresponding to the current column id.
          -- Get column name.
          l_col_name := LOWER(l_desc_tab(j).col_name);
          
          -- Determine the column type for the current column index.
          IF l_desc_tab(j).col_type = 2 THEN         
            -- Column type is NUMBER, search the collection of NUMBER tables.
            FOR k IN 1..tab_dbms_sql_number_table.COUNT LOOP
            
              IF tab_dbms_sql_number_table(k).indx = j THEN
                -- Index of this collection matches the column index. Get the value
                -- for the current row from the collection.
                l_number_tab_value := tab_dbms_sql_number_table(k).num_tab(i);
                -- Write the value to the XML file.
                UTL_FILE.put_line(l_file, '        <' || l_col_name || '>' || l_number_tab_value ||
                    '</' || l_col_name || '>');
                EXIT;
              END IF;
              
            END LOOP;
            
          ELSIF l_desc_tab(j).col_type = 1 THEN
            -- Column type is VARCHAR2, search the collection of VARCHAR2 tables.
            FOR k in 1..tab_dbms_sql_varchar_table.COUNT LOOP
            
              IF tab_dbms_sql_varchar_table(k).indx = j THEN
                -- Index of this collection matches the column index. Get the value
                -- for the current row from the collection.
                l_varchar_tab_value := tab_dbms_sql_varchar_table(k).varchar_tab(i);
                -- Change string special characters so thay can be read by xml client parsing routine.
                l_searched_tab_value := HANDLE_ESCAPED_CHARACTERS(l_varchar_tab_value);
                -- Write the value to the XML file.
                UTL_FILE.put_line(l_file, '        <' || l_col_name || '>' || l_searched_tab_value ||
                    '</' || l_col_name || '>');
                EXIT;
              END IF;
              
            END LOOP;
          
          ELSIF l_desc_tab(j).col_type = 12 THEN
            -- Column type is DATE, search the collection of DATE tables.
            For k in 1..tab_dbms_sql_date_table.COUNT LOOP
            
              IF tab_dbms_sql_date_table(k).indx = j THEN
                -- Index of this collection matches the column index. Get the value
                -- for the current row from the collection.
                l_date_tab_value := tab_dbms_sql_date_table(k).date_tab(i);
                -- Write the value to the XML file.
                UTL_FILE.put_line(l_file, '        <' || l_col_name || '>' ||
                    TO_CHAR(l_date_tab_value, 'DD-MON-YYYY') || '</' || l_col_name || '>');
                EXIT;
              END IF;
              
            END LOOP;
            
          END IF;
          
        END LOOP table_column_list;
        
        -- Write closing XML tag for current record to the file.
        UTL_FILE.put_line(l_file, '    </' || l_record_tag || '>');
        
      END LOOP table_row_list;
        
      -- Clean out the collections ready for the next table.
      t_number.delete;
      t_date.delete;
      t_varchar2.delete;
      tab_dbms_sql_number_table.delete;
      tab_dbms_sql_date_table.delete;
      tab_dbms_sql_varchar_table.delete;
      -- Reset the the collection indexes.
      l_coll_num_indx := 1;
      l_coll_date_indx := 1;
      l_coll_varchar_indx := 1;
      
    END IF;
    
    -- Write closing XML tag for current table.
    UTL_FILE.put_line(l_file, '</' || LOWER(REPLACE(r_tables.table_name, '_XML')) || '>');
    
  END LOOP table_list;
  
  -- Write closing XML tag for user.
  UTL_FILE.put_line(l_file, '</user>');
  
  -- Close the file we are writing to
  UTL_FILE.fclose(l_file);
  
  RETURN l_file_name;
  
EXCEPTION
  
  WHEN no_user_exists THEN
    -- Log the exception.
    pkg_error_handling.record_exception(p_user_id,
                    null,
                    'User does not exist');
    COMMIT;
    RETURN '0';
    
  WHEN others THEN
    -- Close the file we are writing to
    IF UTL_FILE.is_open(l_file) THEN
      UTL_FILE.fclose(l_file);
    END IF;
    -- Record the error.
    pkg_error_handling.record_error(p_user_id);
    RETURN '0';
    
END;
