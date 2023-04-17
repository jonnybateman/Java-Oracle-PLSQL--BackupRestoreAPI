# Java Socket/Oracle PLSQL Backup & Restore API

A collection of Java and Oracle PL/SQL scripts to backup or restore data held in a SQLite database belonging to an Android application, in this case a shopping list app.

## User Account

Before a backup or restoration can be performed a user account needs to be created on the server. The method ClientValidateUser.validateUser() will send a request to the server to create a user for the specified username. If one does not already exist a valid user id will be returned to the client. User details will be stored in the app's sqlite database. Multiple server connection threads are supported for multiple client sessions.

## Backup

A backup request instigates ClientBackupXML.createBackupXML(). This method will write data from all sqlite database tables into a XML file. This file is then sent by ClientBackup.backupData() to the server for processing. The client-server communication channel utilises java socket programming. The user_id will be sent to the server. If a valid response is received the client will then send the xml file containing the table data to the server. The file is stored in an Oracle directory.

Using JDBC to connect to the server's Oracle backup database `FunctionCall.functionCall()` calls the database function `process_xml_file()` to process the uploaded xml file into the database.

This repository can be split into two distinct groups of files:
1. .java files for accepting an incoming server socket connection from a client app and establishing a JDBC connection to the Oracle backup database on the server. Multiple server connection threads are supported for multiple client sessions.
2. Oracle PL/SQL files for backing up table data passed in xml format from the client and generation of data xml files for the restoration of data on the client.

Included in the repository are the Oracle database schema files to which client data is backed up. In this case the client is the SmartList Android application.
