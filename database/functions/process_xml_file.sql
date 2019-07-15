create or replace FUNCTION process_xml_file (file_name IN VARCHAR2)
    RETURN VARCHAR2
IS
  
  l_bfile           BFILE;
  l_clob            CLOB;
  l_xml_parser      DBMS_XMLPARSER.parser;
  l_dom_doc         DBMS_XMLDOM.domDocument;
  l_node_list       DBMS_XMLDOM.domNodeList;
  l_node            DBMS_XMLDOM.domNode;
  l_user_node_list  DBMS_XMLDOM.domNodeList;
  l_user_node       DBMS_XMLDOM.domNode;
  
  l_dest_offset     INTEGER := 1;
  l_src_offset      INTEGER := 1;
  l_bfile_csid      NUMBER := 0;
  l_lang_context    INTEGER := 0;
  l_warning         INTEGER := 0;
  l_user_id         NUMBER;
  l_node_count      NUMBER := 0;
  l_table_tag       VARCHAR2(30);
  l_current_table   VARCHAR2(30);
  l_exists          VARCHAR2(1);
  
  insertion_error   EXCEPTION;
  no_user_exists    EXCEPTION;
  
  TYPE t_items_type IS TABLE OF items_xml%ROWTYPE INDEX BY PLS_INTEGER;
    t_items         t_items_type;
  TYPE t_categories_type IS TABLE OF categories_xml%ROWTYPE INDEX BY PLS_INTEGER;
    t_categories    t_categories_type;
  TYPE t_brands_type IS TABLE OF brands_xml%ROWTYPE INDEX BY PLS_INTEGER;
    t_brands        t_brands_type;
  TYPE t_item_info_type IS TABLE OF item_info_xml%ROWTYPE INDEX BY PLS_INTEGER;
    t_item_info     t_item_info_type;
  TYPE t_list_items_type IS TABLE OF list_items_xml%ROWTYPE INDEX BY PLS_INTEGER;
    t_list_items    t_list_items_type;
  TYPE t_lists_type IS TABLE OF lists_xml%ROWTYPE INDEX BY PLS_INTEGER;
    t_lists         t_lists_type;
  TYPE t_shops_type IS TABLE OF shops_xml%ROWTYPE INDEX BY PLS_INTEGER;
    t_shops         t_shops_type;
    
  CURSOR c_tables IS
    SELECT table_name
    FROM user_tables
    WHERE table_name LIKE '%_XML'
    AND table_name != 'USERS_XML';
  
  CURSOR c_table_columns(v_table_name IN VARCHAR2) IS
    SELECT column_name
    FROM user_tab_columns
    WHERE table_name = v_table_name;
  
  PROCEDURE user_backup_file_update(p_user_id IN NUMBER, p_result IN VARCHAR2)
  IS
  BEGIN
    UPDATE users_xml
    SET backup_file = file_name,
        backup_result = p_result,
        backup_date = SYSDATE
    WHERE user_id = p_user_id;
  END;
  
BEGIN
  
  -- Get the file from the oracle directory.
  l_bfile := BFILENAME('XMLDIR', file_name);
  -- Ready a temporary clob to hold the data from the file.
  DBMS_LOB.createtemporary(l_clob, cache=>false);
  -- Open the xml file for reading.
  DBMS_LOB.open(l_bfile, DBMS_LOB.lob_readonly);
  
  -- Load the data from the xml file into the temporary clob.
  DBMS_LOB.loadclobfromfile(
      dest_lob      => l_clob,
      src_bfile     => l_bfile,
      amount        => DBMS_LOB.getlength(l_bfile),
      dest_offset   => l_dest_offset,
      src_offset    => l_src_offset,
      bfile_csid    => l_bfile_csid,
      lang_context  => l_lang_context,
      warning       => l_warning);
  -- No longer need the BFILE object.
  DBMS_LOB.close(l_bfile);
  
  -- Now the XML document must be parsed and a DOM document created from it.
  l_xml_parser := DBMS_XMLPARSER.newparser; -- parser initialized.
  DBMS_XMLPARSER.parseclob(l_xml_parser, l_clob);
  l_dom_doc := DBMS_XMLPARSER.getdocument(l_xml_parser);
  
  -- Free up the parser and clob, no longer needed.
  DBMS_XMLPARSER.freeParser(l_xml_parser);
  DBMS_LOB.freeTemporary(l_clob);
  
  -- Create a node list for the USER tag.
  l_user_node_list := DBMS_XMLDOM.getElementsByTagName(l_dom_doc, 'user');
  -- Create a node for the first USER tag in the list since there will be only one
  -- USER tag (root element).
  l_user_node := DBMS_XMLDOM.item(l_user_node_list, 0);
  -- Extract the value of the ID attribute for the USER tag.
  DBMS_XSLPROCESSOR.valueOf(l_user_node, '@id', l_user_id);
  
  -- Check the user exists before proceeding.
  SELECT CASE
            WHEN EXISTS (SELECT 1
                         FROM users_xml
                         WHERE user_id = l_user_id)
              THEN 'Y'
              ELSE 'N'
         END
  INTO l_exists
  FROM dual;
  
  IF l_exists = 'N' THEN
    RAISE no_user_exists;
  END IF;
  
  -- Loop through the XML tables that are to have data inserted from the XML file.
  <<table_list>>
  FOR r_tables IN c_tables LOOP
  
    dbms_output.put_line('Table Name:' || r_tables.table_name);
  
    l_current_table := r_tables.table_name;
  
    -- From the table name get the targeted XML tag name.
    SELECT REPLACE(LOWER(comments), 'xml_tag:')
    INTO l_table_tag
    FROM user_tab_comments
    WHERE table_name = r_tables.table_name;
    
    -- Get a list of all the element nodes for the current table from the DOM document.
    l_node_list := DBMS_XSLPROCESSOR.selectnodes(DBMS_XMLDOM.makeNode(
        l_dom_doc), '/user/' || LOWER(REPLACE(r_tables.table_name, '_XML')) || '/' || l_table_tag);
    l_node_count := DBMS_XMLDOM.getLength(l_node_list);
    
    dbms_output.put_line('Node Count:' || l_node_count);
    
    -- If there are no nodes for the current table then exit the current iteration of the loop.
    CONTINUE WHEN l_node_count = 0;
  
    -- Loop thru the list of nodes and insert a record into the current table for
    -- each item record.
    <<table_nodes>>
    FOR i IN 0..l_node_count - 1 LOOP
      -- Get the current node's data from the node list.
      l_node := DBMS_XMLDOM.item(l_node_list, i);
      
      CASE l_table_tag      
        WHEN pkg_global_constants.g_tag_item THEN
          -- Assign values of the node to the elements of the associative array.
          DBMS_XSLPROCESSOR.valueof(l_node, 'item_id/text()', t_items(i).item_id);
          DBMS_XSLPROCESSOR.valueof(l_node, 'item_name/text()', t_items(i).item_name);
          DBMS_XSLPROCESSOR.valueof(l_node, 'barcode/text()', t_items(i).barcode);
          DBMS_XSLPROCESSOR.valueof(l_node, 'cat_id/text()', t_items(i).cat_id);
          DBMS_XSLPROCESSOR.valueof(l_node, 'quantity_unit/text()', t_items(i).quantity_unit);
          t_items(i).user_id := l_user_id;
        
        WHEN pkg_global_constants.g_tag_category THEN
          DBMS_XSLPROCESSOR.valueOf(l_node, 'cat_id/text()', t_categories(i).cat_id);
          DBMS_XSLPROCESSOR.valueOf(l_node, 'category_name/text()', t_categories(i).category_name);
          t_categories(i).user_id := l_user_id;
        
        WHEN pkg_global_constants.g_tag_brand THEN
          DBMS_XSLPROCESSOR.valueOf(l_node, 'brand_id/text()', t_brands(i).brand_id);
          DBMS_XSLPROCESSOR.valueOf(l_node, 'brand_name/text()', t_brands(i).brand_name);
          t_brands(i).user_id := l_user_id;
          
        WHEN pkg_global_constants.g_tag_item_info THEN
          DBMS_XSLPROCESSOR.valueOf(l_node, 'item_id/text()', t_item_info(i).item_id);
          DBMS_XSLPROCESSOR.valueOf(l_node, 'price/text()', t_item_info(i).price);
          DBMS_XSLPROCESSOR.valueOf(l_node, 'shop_id/text()', t_item_info(i).shop_id);
          DBMS_XSLPROCESSOR.valueOf(l_node, 'brand_id/text()', t_item_info(i).brand_id);
          DBMS_XSLPROCESSOR.valueOf(l_node, 'location/text()', t_item_info(i).location);
          DBMS_XSLPROCESSOR.valueOf(l_node, 'quantity/text()', t_item_info(i).quantity);
          t_item_info(i).user_id := l_user_id;
        
        WHEN pkg_global_constants.g_tag_list_item THEN
          DBMS_XSLPROCESSOR.valueOf(l_node, 'list_id/text()', t_list_items(i).list_id);
          DBMS_XSLPROCESSOR.valueOf(l_node, 'item_id/text()', t_list_items(i).item_id);
          DBMS_XSLPROCESSOR.valueOf(l_node, 'item_name/text()', t_list_items(i).item_name);
          DBMS_XSLPROCESSOR.valueOf(l_node, 'category_name/text()', t_list_items(i).category_name);
          DBMS_XSLPROCESSOR.valueOf(l_node, 'quantity_unit/text()', t_list_items(i).quantity_unit);
          DBMS_XSLPROCESSOR.valueOf(l_node, 'checked/text()', t_list_items(i).checked);
          t_list_items(i).user_id := l_user_id;
          
        WHEN pkg_global_constants.g_tag_list THEN
          DBMS_XSLPROCESSOR.valueOf(l_node, 'list_id/text()', t_lists(i).list_id);
          DBMS_XSLPROCESSOR.valueOf(l_node, 'list_name/text()', t_lists(i).list_name);
          t_lists(i).user_id := l_user_id;
        
        WHEN pkg_global_constants.g_tag_shop THEN
          DBMS_XSLPROCESSOR.valueOf(l_node, 'shop_id/text()', t_shops(i).shop_id);
          DBMS_XSLPROCESSOR.valueOf(l_node, 'shop_name/text()', t_shops(i).shop_name);
          t_shops(i).user_id := l_user_id;
          
      END CASE;
    END LOOP table_nodes;
    
    -- Delete data from the current table for the user.
    EXECUTE IMMEDIATE 'DELETE FROM ' || r_tables.table_name || ' WHERE user_id = :x'
    USING l_user_id;
  
    -- Insert the data from the associative array into the current table table.  
    CASE l_table_tag
      WHEN pkg_global_constants.g_tag_item THEN
        FORALL i IN t_items.first..t_items.last
          INSERT INTO items_xml VALUES t_items(i);
      
      WHEN pkg_global_constants.g_tag_category THEN
        FORALL i IN t_categories.first..t_categories.last
          INSERT INTO categories_xml VALUES t_categories(i);
      
      WHEN pkg_global_constants.g_tag_brand THEN
        FORALL i IN t_brands.first..t_brands.last
          INSERT INTO brands_xml VALUES t_brands(i);
      
      WHEN pkg_global_constants.g_tag_item_info THEN
        FORALL i IN t_item_info.first..t_item_info.last
          INSERT INTO item_info_xml VALUES t_item_info(i);
      
      WHEN pkg_global_constants.g_tag_list_item THEN
        FORALL i IN t_list_items.first..t_list_items.last
          INSERT INTO list_items_xml VALUES t_list_items(i);
      
      WHEN pkg_global_constants.g_tag_list THEN
        FORALL i IN t_lists.first..t_lists.last
          INSERT INTO lists_xml VALUES t_lists(i);
      
      WHEN pkg_global_constants.g_tag_shop THEN
        FORALL i IN t_shops.first..t_shops.last
          INSERT INTO shops_xml VALUES t_shops(i);
    END CASE;
  
    IF SQL%ROWCOUNT <> l_node_count THEN 
      -- Issue has occurred wrong number of inserted records to nodes.
      RAISE insertion_error;
    END IF;
  
  END LOOP table_list;
  
  COMMIT;
  
  -- Free any resources associated with the DOM document.
  DBMS_XMLDOM.freeDocument(l_dom_doc);
  
  -- Update the user history
  user_backup_file_update(l_user_id, 'SUCCESS');
  
  RETURN 'SUCCESS'; -- Successful
  
EXCEPTION

  WHEN no_user_exists THEN
    -- Free up the DOM document resources.
    DBMS_XMLDOM.freeDocument(l_dom_doc);
    -- Log the exception.
    pkg_error_handling.record_exception(l_user_id,
                    null,
                    'User does not exist');
    COMMIT;
    RETURN 'FAIL'; -- Unsuccessful
    
  WHEN insertion_error THEN
    ROLLBACK;
    -- Update the user history
    user_backup_file_update(l_user_id, 'FAIL');
    -- Log the exception.
    pkg_error_handling.record_exception(l_user_id,
                    null,
                    'Wrong number of records inserted: '|| l_current_table);
    COMMIT;
    RETURN 'FAIL';
    
  WHEN others THEN
    ROLLBACK; -- Roll back all changes made in regards to this backup.
    -- Update the user history
    user_backup_file_update(l_user_id, 'FAIL');
    -- Log the error
    pkg_error_handling.record_error(l_user_id);
    COMMIT;
    
    dbms_output.put_line('ERROR:' || SQLERRM);
    IF DBMS_LOB.isTemporary(l_clob) = 1 THEN
      DBMS_LOB.freeTemporary(l_clob);
    END IF;
  
    -- Free up the document parser resources.
    DBMS_XMLPARSER.freeParser(l_xml_parser);
  
    -- Free up the DOM document resources.
    DBMS_XMLDOM.freeDocument(l_dom_doc);
    
    RETURN 'FAIL';
  
END;
