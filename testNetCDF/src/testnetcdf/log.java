/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package testnetcdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
/**
 *
 */
public class log {
    public static final String cStrTimeFormatOut = "yyyy-MM-dd_HH-mm-ss.SSS";
    public static final String cStrTimeFormatLog = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String cStrTimeFormatElapsedTime = "mm:ss.SSS";
    public static final String cStrTimeFormatYear = "yyyy";
    public static final String cStrTimeFormatMonth = "yyyy/MM";
    public static final String cStrTimeZoneLocal = "Australia/Sydney";
    public static final String cStrTimeZoneGMT = "GMT";
    
    protected Calendar gCldNow;
    protected SimpleDateFormat gSdfOut,gSdfYear,gSdfMonth,gSdfLog,gSdfElapsedTime;
    public OutputStreamWriter gOswLog;
    public String gStrFileName;
    
    protected Date GDtStart;
    
    /**
     * Constructor for class to log relevant information to Standard Output, and 
     * any log file which is subsequently supplied. 
     * 
     * @throws FileNotFoundException
     * @throws IOException 
     */
    log() throws FileNotFoundException, IOException {
        this("");
    }
    
    /**
     * Constructor for class to log relevant information to Standard Output, and 
     * any log file supplied. 
     * 
     * @param strFileName: The name of the file to log information to.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    log(String strFileName) throws FileNotFoundException, IOException {
        gStrFileName = strFileName;
        gCldNow = Calendar.getInstance();gCldNow.setTimeInMillis(System.currentTimeMillis());
        gSdfOut = new SimpleDateFormat(cStrTimeFormatOut);gSdfOut.setTimeZone(TimeZone.getTimeZone(cStrTimeZoneLocal));gSdfOut.setCalendar(gCldNow);
        gSdfYear = new SimpleDateFormat(cStrTimeFormatYear);gSdfYear.setTimeZone(TimeZone.getTimeZone(cStrTimeZoneLocal));gSdfYear.setCalendar(gCldNow);
        gSdfMonth = new SimpleDateFormat(cStrTimeFormatMonth);gSdfMonth.setTimeZone(TimeZone.getTimeZone(cStrTimeZoneLocal));gSdfMonth.setCalendar(gCldNow);
        gSdfLog = new SimpleDateFormat(cStrTimeFormatLog);gSdfLog.setTimeZone(TimeZone.getTimeZone(cStrTimeZoneLocal));gSdfLog.setCalendar(gCldNow);
        gSdfElapsedTime = new SimpleDateFormat(cStrTimeFormatElapsedTime);gSdfElapsedTime.setTimeZone(TimeZone.getTimeZone(cStrTimeZoneLocal));gSdfElapsedTime.setCalendar(gCldNow);
        GDtStart = gCldNow.getTime();
        if (gStrFileName.length() > 0) { 
            gOswLog = new OutputStreamWriter(new FileOutputStream(gStrFileName,false));gOswLog.close();
        }
    }
    
    /**
     * Function to make the specified directory, if it doesn't already exist.  
     * 
     * Note that the subdirectory must exist, as it doesn't actually work out 
     * the full path.
     * 
     * @param strDirectory: The name of the directory to create.
     * @return
     * @throws IOException 
     */
    public boolean MakeDirectory(String strDirectory) throws IOException {
        File filCurrent;
        filCurrent = new File(strDirectory);
        if (!filCurrent.exists()) {
            if (!filCurrent.mkdir()) {
                System.out.println("Failed to create the Directory '" + strDirectory + "'");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Function to log the given string to Standard Output, and to any log file
     * which was provided.
     * 
     * Note that the system time is added as a prefix to the line.
     * 
     * @param strValue: The value to log.
     */
    public void println(String strValue) {
        String strLine;
        strLine = gSdfLog.format(TimeNow()) + ": " + strValue;
        System.out.println(strLine);
        if (gStrFileName.length() > 0) {
            try {
                gOswLog = new OutputStreamWriter(new FileOutputStream(gStrFileName,true));
                gOswLog.write(strLine+"\r\n");// make compatible for Windows
                gOswLog.close();
            }
            catch (IOException ioeM) {
                System.out.println("Warning: IO Exception when writing to log.");
            }
        }
    }
    
    /**
     * Function to close the class.
     * 
     * It also logs runtime information.
     */
    public void close() {
        long lngMillis,lngHours,lngDays;
        Date dtmNow,dtmElapsed;
        String strElapsed;
        
        dtmNow = TimeNow();
        lngMillis = dtmNow.getTime() - GDtStart.getTime();
        lngHours = lngMillis/3600000;
        lngDays = lngHours/24;
        lngHours -= lngDays*24;
        gCldNow.setTimeInMillis(lngMillis);dtmElapsed = gCldNow.getTime();
        strElapsed = "";
        if (lngDays > 0) {
            strElapsed = Long.toString(lngDays) + " ";
            if (lngHours < 10)
                strElapsed += "0";
        }
        strElapsed += Long.toString(lngHours) + ":" + gSdfElapsedTime.format(dtmElapsed);
        println("Done");println("Elapsed Time: " + strElapsed);
    }
    
    /**
     * Function to take a string containing a timestamp in "yyyy-MM-dd_HH-mm-ss.SSS"
     * format and add the specified number of seconds, and return the date.
     * 
     * @param strStart: The string containing the timestamp.
     * @param intSeconds: The number of seconds to add to the time.
     * @return: The Date of the result.
     * @throws ParseException 
     */
    public Date Time(String strStart,int intSeconds) throws ParseException {
        gCldNow.setTime(gSdfOut.parse(strStart));
        gCldNow.add(Calendar.SECOND, intSeconds);
        return gCldNow.getTime();
    }
    
    /**
     * Function to return a "yyyy" formatted string for the given date.
     * 
     * @param dtmVal: The date in question.
     * @return: The year formatted string for the date.
     */
    public String TimeYear(Date dtmVal) {
        return gSdfYear.format(dtmVal);
    }
    
    /**
     * Function to return a "yyyy/MM" formatted string for the given date.
     * 
     * @param dtmVal: The date in question.
     * @return: The year and month formatted string for the date.
     */
    public String TimeMonth(Date dtmVal) {
        return gSdfMonth.format(dtmVal);
    }
    
    /**
     * Function to return a "yyyy-MM-dd_HH-mm-ss.SSS" formatted string for the given date.
     * 
     * @param dtmVal: The date in question.
     * @return: The timestamp formatted string for the date.
     */
    public String TimeOut(Date dtmVal) {
        return gSdfOut.format(dtmVal);
    }
    
    /**
     * Function to return a "yyyy-MM-dd HH-mm-ss.SSS" formatted string for the given date.
     * 
     * @param dtmVal: The date in question.
     * @return: The timestamp formatted string for the date.
     */
    public String TimeLog(Date dtmVal) {
        return gSdfLog.format(dtmVal);
    }
    
    /**
     * Function to return the current system time.
     * @return: The current system time.
     */
    public Date TimeNow() {
        gCldNow.setTimeInMillis(System.currentTimeMillis());
        return gCldNow.getTime();
    }
    
    /**
     * Function to export a verbose description of what went wrong, with the
     * provided stack trace.
     * 
     * Note that only the first 50 lines are exported.
     * 
     * @param aSTETraces: The stack trace describing the error messages.
     * @return: A string containing a break down of the error messages. 
     */
    public static String StackTrace(StackTraceElement[] aSTETraces) {
        String strMessage, strLine;
        int intRow, intRows, intLines;
        
        strMessage = "";
        intRows = Math.min(aSTETraces.length,50);
        for (intRow = 0, intLines = 0; intRow < intRows; intRow++) {
            strLine = "[" + intRow + "]" + aSTETraces[intRow].toString();
            intLines += 1 + strLine.split("\r\n").length;
            if (intLines > 50)
                break;
            strMessage += strLine + "\r\n";
        }
        return strMessage;
    }
    
    /**
     * Function to determine the number of bytes within the specified file.
     * 
     * @param strFileName: The name of the file in question.
     * @return The the number of bytes within the file.
     */
    public long Bytes(String strFileName) {
        File filCur;
        filCur = new File(strFileName);
        return filCur.length();
    }
    
    /**
     * Function to determine the number of bytes within the specified file, using the du command.
     * 
     * Note that this is only necessary if the volume where the file is store is compressed, as the
     * system reports the size of the decompressed file.
     * 
     * @param strFileName: The name of the file in question.
     * @param intBytesPerBlock: The number of bytes per block reported by du on the system.
     *                          For CentOS the value is 1024, whilst for Solaris it is 512 for
     *                          ZFS compressed partitions.
     * @return The number of bytes that the file takes on disk.
     */
    public long BytesFromDU(String strFileName, int intBytesPerBlock) {
        /* This loop really isn't necessary, as the only time this occurred was 
         * when the system was out of memory, so du failed regardless of how many 
         * times an attempt was made to fix it. */
        for (int intAttempt = 0; intAttempt < 3; intAttempt++) { 
            try {
                ProcessBuilder prbCreator;
                Process prcCurrent;
                InputStreamReader isrIn;
                String strRes;
                int intVal;
                char chrVal;

                // create a process to read the disk use
                prbCreator = new ProcessBuilder("du","-s",strFileName);
                prbCreator.directory(new java.io.File("/usr/bin/"));
                prcCurrent = prbCreator.start();
                isrIn = new InputStreamReader(prcCurrent.getInputStream());

                // read until find a non digit
                strRes = "";
                while (true) {
                    intVal = isrIn.read();
                    if (intVal == -1)
                        break;
                    else
                        chrVal = (char)intVal;
                    if (!Character.isDigit(chrVal))
                        break;
                    strRes += chrVal;
                }
                // read remaing bytes
                while (isrIn.read() > -1) {};
                isrIn.close();
                prcCurrent.destroy();
                return Long.parseLong(strRes)*intBytesPerBlock;
            }
            catch (Exception excError) {
                println("Warning: An Exception occurred whilst calculating the disk use for '"  +strFileName + "' on Attempt " +
                    Integer.toString(intAttempt) + "\r\n\r\n" +
                    excError.getMessage() + "\r\n\r\n" +StackTrace(excError.getStackTrace()));
                if (excError.getMessage().contains("Not enough space") && (intAttempt > 0))
                    return -1;
            }
            try {
                System.gc();
                Thread.sleep(100);
            }
            catch (InterruptedException iteError) {}
        }
        return -1;
    }
    
    /* Note that it is bad form to leave a variable like this, but it gets re-used frequently
     * for performance tests, so it is advantageous to avoid numerous garbage collection runs
     * when the variable is re-used.
    */
    private byte[] mABytData; 
    
    /**
     * Function to flush the system disk buffer by writing and then reading the specified
     * number of bytes to disk.
     * 
     * For optimum efficiency the size should be exactly the total size of cache available on 
     * all disks used on the volume where data is written/read from/to.
     * 
     * @param strFileName: The temporary filename to contain the data that is written then read.
     * @param intBufferSize: The size of the buffer to write/read a single block of data to disk.
     * @param lngSize: The total number of bytes to write/read from disk. 
     */
    public void FlushDiskBuffer(String strFileName, int intBufferSize,long lngSize) {
        FileOutputStream fosOut;
        FileInputStream fisIn;
        File filCur;
        int intLen,intOff,intPos;
        byte bytVal;
        
        if ((mABytData == null) || (mABytData.length < intBufferSize)) {
            mABytData = new byte[intBufferSize];
            bytVal = (byte)System.currentTimeMillis();
            for (intPos = 0; intPos < intBufferSize; intPos++)
                mABytData[intPos] = bytVal++;
        }
        intLen = (int)Math.max(1,lngSize/intBufferSize);
        try {
            fosOut = new FileOutputStream(strFileName);
            for (intOff = 0; intOff < intLen; intOff++) {
                fosOut.write(mABytData);
            }
            fosOut.close();
        }
        catch (IOException ioeError) {
            println("Warning: An Exception occurred whilst writing the flush '"  +strFileName + "'\r\n\r\n" +
                    ioeError.getMessage() + "\r\n\r\n" +StackTrace(ioeError.getStackTrace()));
        }
        try {
            fisIn = new FileInputStream(strFileName);
            fisIn.read(mABytData);
            fisIn.close();
        }
        catch (IOException ioeError) {
            println("Warning: An Exception occurred whilst reading the flush '"  +strFileName + "'\r\n\r\n" +
                    ioeError.getMessage() + "\r\n\r\n" + StackTrace(ioeError.getStackTrace()));
        }
        filCur = new File(strFileName);
        filCur.delete();
    }
}
