package errorlog;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class LogError {
    
    private final static String FILE_DIRECTORY =
            "C:\\Users\\jonny\\NetBeansIDE8.2\\ServerBackup\\test\\";
    
    public void writeToFile (String exception, String userId) {
        
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        
        try {
            // Create the filename.
            Date date = new Date();
            String filename = dateFormat.format(date) + "_" + userId +
                    "_ERROR_LOG.txt";
          
            // Write the passed exception stack trace to the file. If file
            // already exists the stack trace will be appended to the file.
            FileWriter fw = new FileWriter(FILE_DIRECTORY + filename, true);
            //PrintWriter pw = new PrintWriter(fw);
            //e.printStackTrace(pw);
            fw.append(exception);
            
            // Close the resources.
            //pw.close();
            fw.close();
            
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }
    
}
