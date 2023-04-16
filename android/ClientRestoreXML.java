package com.development.smartlist;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class ClientRestoreXML {

    private static final String DB_TYPE_INTEGER = "integer";
    private static final String DB_TYPE_TEXT = "text";
    private static final String DB_TYPE_REAL = "real";

    boolean restoreUserData(String fileName) {

        final Context context = GlobalClass.getAppContext();

        String xmlFile = "/storage/emulated/0/Download/" + fileName;
        int elementsTotal = 0;
        int recordsTotal = 0;

        DBAdapter dbAdapter = new DBAdapter(context);

        try {
            dbAdapter.open();
            dbAdapter.beginTransaction();

            File file = new File(xmlFile);

            DocumentBuilderFactory docBuildfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuiler = docBuildfactory.newDocumentBuilder();
            //InputStream inputStream = context.getAssets().open(xmlFile);
            Document document = docBuiler.parse(file);

            // Setup an array of the tables we need to insert data into.
            String[] tables = context.getResources().getStringArray(R.array.array_tables);

            // Setup an array of element tags, each of which correspond to a table we need to insert
            // data into.
            String[] tab_element_tags = context.getResources().getStringArray(R.array.array_record_tag);

            // For each table get associated xml nodes (rows).
            for (int tagIndx=0; tagIndx<tables.length; tagIndx++) {

                Log.d("ClientRestoreXML","Table:" + tables[tagIndx]);

                // Get the column names and types for the current table.
                LinkedHashMap<String, String> columnNamesTypes = dbAdapter.getTableColumns(tables[tagIndx]);

                Log.d("ClientRestoreXML","No. of columns:" + columnNamesTypes.size());

                // For the current table get a list of element nodes.
                NodeList tableNodes = document.getElementsByTagName(tab_element_tags[tagIndx]);

                // Keep track of the total number of elements processed.
                elementsTotal = elementsTotal + tableNodes.getLength();

                // Clear the current database table ready for the restored data to be inserted.
                dbAdapter.deleteTableData(tables[tagIndx]);

                // Loop through the nodes(rows) for the current table.
                for (int nodeIndx=0; nodeIndx<tableNodes.getLength(); nodeIndx++) {

                    // Create an element object for the table's current node (row).
                    Element tableElement = (Element) tableNodes.item(nodeIndx);

                    Log.d("ClientRestoreXML","TableNode:" + tableElement.getTagName());

                    // Create ContentValues object to store column/value pairs that will be used
                    // to create a database record for the current table element.
                    ContentValues values = new ContentValues();

                    // For each column in the hashmap (map of current table's columns) for the current node (row).
                    for (Map.Entry<String, String> entry : columnNamesTypes.entrySet()) {

                        Log.d("ClientRestoreXML","Column Name:" + entry.getKey());
                        // Get value and value type for the current table element.
                        String valueString = tableElement.getElementsByTagName(entry.getKey()).item(0).getTextContent();
                        Log.d("ClientRestoreXML","Column value:" + valueString);
                        String valueType = entry.getValue();

                        // Cast the value to its associated type if necessary and store for later use.
                        switch (valueType) {
                            case DB_TYPE_TEXT:
                                // Store value in ContentValues object for later insertion into database.
                                if (valueString.length() > 0) {
                                    // Only need to store a value for this column if it has got a value.
                                    // NULL will simply be inserted by the database if no value supplied.
                                    values.put(entry.getKey(), valueString);
                                }
                                break;
                            case DB_TYPE_INTEGER:
                                // Store value in ContentValues object for later insertion into database.
                                if (valueString.length() > 0) {
                                    values.put(entry.getKey(), Integer.parseInt(valueString));
                                }
                                break;
                            case DB_TYPE_REAL:
                                // Store value in ContentValues object for later insertion into database.
                                if (valueString.length() > 0) {
                                    values.put(entry.getKey(), Float.parseFloat(valueString));
                                }
                                break;
                        }
                    }

                    // From the dataMap for the current element call generic database insert routine
                    // to create record.
                    if (values.size() > 0) {
                        long id = dbAdapter.insertTableElement(tables[tagIndx], values);

                        if (id != -1) {
                            // No error occurred inserting record for current element. Keep track of
                            // total records inserted.
                            recordsTotal++;
                        }
                    }
                }
            }

            if (recordsTotal == elementsTotal && elementsTotal > 0) {
                dbAdapter.setTransaction();
                return true;
            } else {
                return false;
            }

        } catch (IOException | SAXException | ParserConfigurationException e) {
            Log.d("ClientRestoreXML",e.toString());
            e.printStackTrace();
            return false;

        } finally {
            dbAdapter.endTransaction();
            dbAdapter.close();
        }

    }
}
