package OracleConnect;

import errorlog.LogError;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.ArrayList;

public class DmlExecute {
    
    private static Connection conn;
    private static Statement stmt;
    private static ResultSet rs;
    
    public ArrayList executeDML(String sqlStmt, String userId) {
        
        try {
            // Load the driver class.
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Create the connection to the database.
            conn = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:xe", "shoplist", "shoplist");
            
            // Create the statement object for the statement that is to be
            // executed against the database.
            stmt = conn.createStatement();
            
            // Execute the statement
            rs = stmt.executeQuery(sqlStmt);
            ResultSetMetaData rsmd = rs.getMetaData();
            
            System.out.println("ResultSetMetaData:" + rsmd.getColumnCount());
            
            // Create storage for the result set data.
            ArrayList<HashMap<Integer, Object>> rsStore = new ArrayList<>();
            
            // Loop through the result set and store the values and associated
            // data types in a collection of hashmaps. One hashmap per record.
            while (rs.next()) {
                // Create a hashmap to store current row's value and data type.
                HashMap<Integer, Object> row = new HashMap<>();
                
                
                
                // For each column of the resultSet current record.
                for (int i=1; i<=rsmd.getColumnCount(); i++) {
                    // Get column value.
                    int type = rsmd.getColumnType(i);
                    
                    System.out.println("Column type:" + type);
                    
                    switch (type) {
                        case Types.NUMERIC:
                            System.out.println("Put value into map:" + rs.getInt(i));
                            // Add column value to hashmap for current record.
                            row.put(Types.INTEGER, rs.getInt(i));
                            break;
                        case Types.VARCHAR:
                            row.put(Types.VARCHAR, rs.getString(i));
                            break;
                    }
                }
                
                // Add the hashmap for the current row to the collection of hashmaps
                rsStore.add(row);
            }
            
            return rsStore;
            
        } catch (ClassNotFoundException | SQLException e) {
            LogError err = new LogError();
            err.writeToFile(e.toString(), userId);
            return null;
            
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LogError err = new LogError();
                    err.writeToFile(e.toString(), userId);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LogError err = new LogError();
                    err.writeToFile(e.toString(), userId);
                }
            }

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LogError err = new LogError();
                    err.writeToFile(e.toString(), userId);
                }
            }

        }
    }
    
}
