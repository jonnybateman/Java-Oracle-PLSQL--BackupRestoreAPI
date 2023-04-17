# Java Socket/Oracle PLSQL Backup & Restore API

A collection of Java and Oracle PL/SQL scripts to backup or restore data held in a SQLite database belonging to an Android application, in this case a shopping list app.

## User Account

Before a backup or restoration can be performed a user account needs to be created on the server. The method `ClientValidateUser.validateUser()` will send a request to the server, via a Java socket, to create a user for the specified username. If one does not already exist a valid user id will be returned to the client. User details will be stored in the app's sqlite database. Multiple server connection threads are supported for multiple client sessions.

## Backup

A backup request instigates `ClientBackupXML.createBackupXML()`. This method will write data from all sqlite database tables in the client app into a XML file. This file is then sent by `ClientBackup.backupData()` to the server for processing. The client-server communication channel utilises java socket programming. The user_id will be sent to the server. If a valid response is received the client will then send the xml file containing the table data to the server. The file is stored in an Oracle directory.

Using JDBC to connect to the server's Oracle backup database the Java method `FunctionCall.functionCall()` calls the database function `process_xml_file().sql` to process the uploaded xml file into the database. The client database backup is now complete.

## Restore

A restore data request is instigated by the client by calling `ClientRestore.restoreData()`. This opens a Java socket and passes the user id to the server. 

If the user id is valid the server opens a JDBC connection to the Oracle backup database and calls `create_xml_file.sql` to generate a xml file containing all table data for the specified user. File is stored in a specified Oracle directory. The file is then sent to the client where it will be processed (via `ClientRestoreXML.resoreUserData()`) and the client app's database tables updated accordingly.

##Server Oracle Database

Included in the repository are the Oracle database schema files to which client data is backed up.
