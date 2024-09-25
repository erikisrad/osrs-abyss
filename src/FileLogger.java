import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLogger {

    String logName;

    public void setLogName(){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
        this.logName = "abyss_output" + dateFormat.format(date) + ".txt";
    }

    public FileLogger(){

    }

    public void writeLog(String msg){
        setLogName();
        File file = new File(this.logName);
        BufferedWriter out;

        try{
            out = new BufferedWriter(new FileWriter(file, true));
        }catch(IOException e){
            Logger.error("failed to create log: " + e);
            return;
        }

        try {
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            out.write(dateFormat.format(date) + ", " + msg + System.lineSeparator());
            out.close();
        } catch (IOException e) {
            Logger.error("failed to write log message: " + msg);
            Logger.error(e);
        }
    }

    public int readInStat(String lineFilter){
        setLogName();
        int value = 0;
        try(BufferedReader br = new BufferedReader(new FileReader(this.logName))) {
            String line = br.readLine();

            while (line != null) {
                if(line.contains(lineFilter)){
                    int last = Integer.parseInt(line.substring(line.lastIndexOf(' ') + 1));
                    value += last;
                }
                line = br.readLine();
            }

        } catch (Exception err) {
            Logger.error("failed to read in stats: " + err);
            return 0;
        }

        return value;
    }
}
