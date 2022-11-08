# Server-Side Backup Processing
This repository can be split into two distinct groups of files:
1. .java files for accepting an incoming server socket connection from a client app and establishing a JDBC connection to the Oracle backup database on the server. Multiple server connection threads are supported for multiple client sessions.
2. Oracle PL/SQL files for backing up table data passed in xml format from the client and generation of data xml files for the restoration of data on the client.

Included in the repository are the Oracle database schema files to which client data is backed up. In this case the client is the SmartList Android application.
