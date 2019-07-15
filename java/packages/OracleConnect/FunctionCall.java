package OracleConnect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import errorlog.LogError;

public class FunctionCall {
    
    private static Connection conn;
    private static Statement stmt;
    private static ResultSet rs;
    private static CallableStatement callStmt;
    
    /*
     * args :-
     *  0. Callable oracle function
     *  1. arg1 value
     *  2. arg1 data type
     *  3. arg2 value
     *  4. arg2 data type
     *      ...
    */
    
    public String callFunction(String args[], String userId) {
        
        String  argValue = "";
        int     paramIndx = 1;
        String  stmtResult;
        
        try {
            System.out.println("CallFunction");
            // Load the driver class.
            Class.forName("oracle.jdbc.driver.OracleDriver");

             // Create the connection to the database.
            conn = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:xe", "shoplist", "shoplist");
         
            // Assign the sub program name to the sub program statement string.
            StringBuilder programStmt = new StringBuilder("call " + args[0] + "(");
            
            for (int i=1; i<args.length; i++) {
                // Ignore the data type arguments when building the program statement.
                if (i % 2 == 0) {
                    continue;
                }
                // Assign parameter placeholders for each argument value to the statement.
                programStmt.append("?,");
            }
            
            // Remove the last ',' from the statement if parameters have been supplied.
            if (args.length > 1) {
                programStmt.deleteCharAt(programStmt.length() - 1);
            }
           
            // Add the parameter closing parenthesis.
            programStmt.append(") into ?");
            
            // Create a callable statement to call a database procedure.
            // Format - 'call program_name(?, ?, ...) into ?'
            callStmt = conn.prepareCall(programStmt.toString());
            
            // Set the parameters to the call statement.
            for (int i=1; i<args.length; i++) {
                
                if (i % 2 > 0) {
                    // Current argument is a parameter value, get the value.
                    argValue = args[i];
                } else {
                    // Current argument is a parameter data type, get data type
                    // and set parameter.
                    switch (args[i].toLowerCase()) {
                        case "int":
                            callStmt.setInt(paramIndx, Integer.valueOf(argValue));
                            break;
                        case "string":
                            callStmt.setString(paramIndx, argValue);
                            break;
                        case "date":
                            java.util.Date argDate = new SimpleDateFormat("dd/MM/yyyy")
                                    .parse(argValue);
                            callStmt.setDate(paramIndx,
                                    new java.sql.Date(argDate.getTime()));
                                        // Need to convert util date to sql date.
                    }
                    paramIndx++;
                }
            }
            
            // Register an out parameter for the SQL functions return result.
            callStmt.registerOutParameter(paramIndx, Types.VARCHAR);
            
            // Execute the statement and get the result
            callStmt.execute();
            stmtResult = callStmt.getString(paramIndx);
            
            // Close the connection object.
            conn.close();
            
            // Return the result of the function call;
            return stmtResult;
            
        } catch (ClassNotFoundException | SQLException | ParseException e) {
            LogError err = new LogError();
            err.writeToFile(e.toString(), userId);
            
            return "0"; // Return value of '0' means error.
            
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
            if (callStmt != null) {
                try {
                    callStmt.close();
                } catch (SQLException e) {
                    LogError err = new LogError();
                    err.writeToFile(e.toString(), userId);
                }
            }
        }
    }
}
