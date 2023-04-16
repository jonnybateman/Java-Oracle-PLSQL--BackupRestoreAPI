package com.development.smartlist;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import org.xmlpull.v1.XmlSerializer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Class creates a backup XML file of the user's data.
 */

class ClientBackupXML {

    private static final String DB_TYPE_INTEGER = "integer";
    private static final String DB_TYPE_TEXT = "text";
    private static final String DB_TYPE_REAL = "real";

    String createBackupXML() {

        final Context context = GlobalClass.getAppContext();

        FileOutputStream fos = null;
        StringWriter stringWriter = null;
        String fileName = null;

        DBAdapter dbAdapter = new DBAdapter(context);

        try {
            Log.d("ClientBackupXML","Start");
            // Retrieve the user id.
            dbAdapter.open();
            int userId = dbAdapter.getUserId();
            dbAdapter.close();

            if (userId > 0) {
                Log.d("ClientBackupXML", "userId:" + userId);

                // Create file for writing xml data to.
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
                Date date = new Date();
                String sDate = simpleDateFormat.format(date);
                // Create file name
                fileName = sDate + "_" + userId + "_backup.xml";
                File file = new File(Environment.
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

                Log.d("ClientBackupXML", "File:" + fileName);

                // Setup the output stream for writing to the file.
                fos = new FileOutputStream(file);

                // Setup the XmlSerializer to create the xml document.
                XmlSerializer xmlSerializer = Xml.newSerializer();
                stringWriter = new StringWriter();
                xmlSerializer.setOutput(stringWriter);

                // Set the XmlSerializer so that it uses xml indentation.
                xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

                xmlSerializer.startDocument("UTF-8", true);

                // Set the root element for the xml document.
                xmlSerializer.startTag(null, "user");
                xmlSerializer.attribute(null, "id", Integer.toString(userId));

                // Setup an array of the tables we need to extract data from.
                String[] tables = context.getResources().getStringArray(R.array.array_tables);
                String[] recordTag = context.getResources().getStringArray(R.array.array_record_tag);

                // For each table in the array.
                dbAdapter.open();
                for (int i = 0; i < tables.length; i++) {

                    // Create start element for current table.
                    xmlSerializer.startTag(null, tables[i]);

                    // Get the columns and associated types for the current table.
                    LinkedHashMap<String, String> columns = dbAdapter.getTableColumns(tables[i]);

                    Log.d("ClientBackupXML","ColumnsSize:" + columns.size());

                    // Get data for the current table
                    List<Object[]> records = dbAdapter.getTableData(tables[i]);

                    Log.d("ClientBackupXML", "Table:" + tables[i]);

                    // Loop through the records
                    for (int j = 0; j < records.size(); j++) {

                        // Define object to hold current record.
                        Object[] record = records.get(j);

                        Log.d("ClientBackupXML", "Record:" + record[0].toString());

                        // Create start element for current record.
                        xmlSerializer.startTag(null, recordTag[i]);

                        int recordIndx = 0;

                        // Loop through the columns of the current record and create a tag for each value.
                        for (Map.Entry<String, String> column : columns.entrySet()) {

                            Log.d("ClientBackupXML", "Column:" + column.getKey());

                            // Create start element for current column.
                            xmlSerializer.startTag(null, column.getKey());

                            Log.d("ClientBackupXML","ColumnValue:" + record[recordIndx]);
                            if (record[recordIndx] != null) {
                                // Determine the data type of the current column.
                                switch (column.getValue()) {
                                    case DB_TYPE_INTEGER:
                                        xmlSerializer.text(record[recordIndx].toString());
                                        break;
                                    case DB_TYPE_TEXT:
                                        // Replace any escape characters from the string.
                                        String s = record[recordIndx].toString().replace("&", "&amp");
                                        xmlSerializer.text(s);
                                        break;
                                    case DB_TYPE_REAL:
                                        xmlSerializer.text(record[recordIndx].toString());
                                        break;
                                }
                            } else {
                                // Value is null so set the value as an empty string in the XML file.
                                xmlSerializer.text("");
                            }

                            Log.d("ClientBackupXML", "Column element value added");
                            // Create end element for current column.
                            xmlSerializer.endTag(null, column.getKey());

                            // Increment the index for the next record object's column.
                            recordIndx++;

                        }
                        // Create end element for current record.
                        xmlSerializer.endTag(null, recordTag[i]);
                    }

                    // Create end element for current table.
                    xmlSerializer.endTag(null, tables[i]);
                }

                // End the XML document.
                xmlSerializer.endDocument();

                // Flush the content of the XmlSerializer to the writer.
                xmlSerializer.flush();

                // Write the string from the StringWriter to the xml file.
                String xml = stringWriter.toString();
                //Log.d("ClientBackupXML", xml);
                fos.write(xml.getBytes());
            }

            return fileName;

        } catch (IOException e) {
            Log.d("ClientBackupXML", e.toString());
            return null;

        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e){
                    Log.d("ClientBackupXML","Exception:" + e.toString());
                }
            }
            if (stringWriter != null) {
                try {
                    stringWriter.close();
                } catch (IOException e) {
                    Log.d("ClientBackupXML","Exception:" + e.toString());
                }
            }

        }
    }
}
