/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testnetcdf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.Random;
import ucar.ma2.InvalidRangeException;

/**
 * A class to be used to run performance tests comparing the same data stored
 * as NetCDF files with one degree, half degree and quarter degree tiles.
 */
public class test {
    public static final String cStrDirectoryDegreeOne = conv.cStrDirectoryDegreeOne;
    public static final String cStrDirectoryDegreeHalf = conv.cStrDirectoryDegreeHalf;
    public static final String cStrDirectoryDegreeQuarter = conv.cStrDirectoryDegreeQuarter;
    public static final String cStrDirectoryAggregateYear = functions.cStrDirectoryAggregateYear;
    public static final String cStrDirectoryAggregateQuarter = functions.cStrDirectoryAggregateQuarter;
    public static final String cStrDirectoryAggregateMonth = functions.cStrDirectoryAggregateMonth;
    public static final int cIntTestDegreeOneSingle = 0;
    public static final int cIntTestDegreeOneAggregateMonth = 1;
    public static final int cIntTestDegreeOneAggregateQuarter = 2;
    public static final int cIntTestDegreeOneAggregateYear = 3;
    public static final int cIntTestDegreeHalfSingle = 4;
    public static final int cIntTestDegreeHalfAggregateMonth = 5;
    public static final int cIntTestDegreeHalfAggregateQuarter = 6;
    public static final int cIntTestDegreeHalfAggregateYear = 7;
    public static final int cIntTestDegreeQuarterSingle = 8;
    public static final int cIntTestDegreeQuarterAggregateMonth = 9;
    public static final int cIntTestDegreeQuarterAggregateQuarter = 10;
    public static final int cIntTestDegreeQuarterAggregateYear = 11;
    public static final int cIntCountTests = 12;
    public static final int cIntDimensionTime = 0;
    public static final int cIntDimensionLat = 1;
    public static final int cIntDimensionLon = 2;
    public static final int cIntCountDimensions = 3;
    public static final int cIntPixelsOneX = 4000;
    public static final int cIntPixelsOneY = 4000;
    public static final int cIntPixelsHalfX = 2000;
    public static final int cIntPixelsHalfY = 2000;
    public static final int cIntPixelsQuarterX = 1000;
    public static final int cIntPixelsQuarterY = 1000;
    public static final double cDblFractionQuarter = 0.25;
    public static final double cDblPixelDegree = ((double)1)/4000;
    public static final int cIntQuarters = 4;
    public static final int cIntOriginTime = 0;
    public static final int cIntOriginLat = 1;
    public static final int cIntOriginLon = 2;
    
    public aggregate gAgrOne,gAgrHlf,gAgrQtr;
    public functions gFunGen;
    public log gLogV;
    
    /**
     * Class constructor.
     * 
     * @param strDirectorySingle: The directory which contains the files with
     *                            a single time record per NetCDF file.
     * @param strDirectoryAggregated: The directory which contains time aggregated
     *                                NetCDF files.  This assumes that this class
     *                                populated the files in the directory with
     *                                a directory for one degree, half degree,
     *                                and quarter degree tiles.
     * @param logV: The class to be used for logging debug information.
     * @throws ParseException
     * @throws IOException 
     */
    test(String strDirectorySingle, String strDirectoryAggregated, log logV) throws ParseException, IOException {
        gLogV = logV;
        /* First load all one degree file names, then the half, then the quarter
         * These structures will contain the names of the files for single record, monthly, quarterly, and yearly instances. */
        gLogV.println("Loading One Degree");
        gAgrOne = new aggregate(strDirectorySingle+ "/" + cStrDirectoryDegreeOne + "/",strDirectoryAggregated + "/" + cStrDirectoryDegreeOne + "/",true,logV);
        gLogV.println("Loading Half Degree");
        gAgrHlf = new aggregate(strDirectorySingle+ "/" + cStrDirectoryDegreeHalf + "/",strDirectoryAggregated + "/" + cStrDirectoryDegreeHalf + "/",false,logV);
        gLogV.println("Loading Quarter Degree");
        gAgrQtr = new aggregate(strDirectorySingle+ "/" + cStrDirectoryDegreeQuarter + "/",strDirectoryAggregated + "/" + cStrDirectoryDegreeQuarter + "/",false,logV);
        gFunGen = gAgrOne.gFunGen;
    }
    
    /**
     * Function to execute the query to compare the performance of NetCDF using
     * the various tile sizes (one degree, half degree, and quarter degree).
     * 
     * @param strDirectoryOut: The directory to export result to.
     * @param intPixelsX: The number of pixels in the X dimension (longitude).
     * @param intPixelsY: The number of pixels in the Y dimension (latitude). 
     * @param intBandStart: The initial Band number to process (starts at 1 not 0).
     * @param intBandEnd: The final Band number to process (for Landsat the maximum is 6).
     * @param intRecordsPerTest: The number of time points to read for each tests.
     * @param intTests: The number of tests to execute in this suite of queries.
     * @param lngSeed: The seed used to initialise the structure for generating random numbers.
     *                 NOTE: To repeat a test and work out what went wrong the seed should 
     *                       be initialised to the published value.  Otherwise it should
     *                       be initialised to the system time to ensure purely random
     *                       tests between subsequent suites of tests.
     * @param intFlushBufferSize: The size of the buffer in memory to be written for flushing the disk cache.
     * @param lngFlushSize: The total amount of data to write to disk to flush the disk cache.
     * @throws IOException
     * @throws InvalidRangeException
     * @throws ParseException 
     */
    public void execute(String strDirectoryOut,int intPixelsX, int intPixelsY,int intBandStart, int intBandEnd, int intRecordsPerTest,
            int intTests, long lngSeed,int intFlushBufferSize,long lngFlushSize) throws IOException,InvalidRangeException,ParseException {
        OutputStreamWriter oswStats;
        Random rndGen;
        String[][] xStrFileNames;
        int[][][] xIntOrigin,xIntShape;
        int[] aIntFileCount;
        long[] aLngProcessTime,aLngSum;
        int intMaxRecords,intFiles,intTest,intFile,intAttempt,intAbandonded;
        int intRecords,intLen;
        int intRow,intCol,intX,intY;
        int intMaxX,intMaxY,intCtr,intTotalQtrX,intTotalQtrY;
        int intBandOrigin,intBands;
        Date dtmStart,dtmEnd;
        double dblLat,dblLon,dblStartLat,dblEndLat,dblEndLon,dblSizeLat,dblSizeLon;
        double dblMaxFractionLon,dblMaxFractionLat,dblFractionLon,dblFractionLat;
        String strCache,strStats,strPrefix;
        
        gLogV.println("Running Performance Tests with Pixels_X=" + Integer.toString(intPixelsX) + ",Pixels_Y=" +
            Integer.toString(intPixelsY) + ",Band_Start=" + Integer.toString(intBandStart) + ",Band_End=" +
            Integer.toString(intBandEnd) + ",Records_Per_Test=" + Integer.toString(intRecordsPerTest) + ",Tests=" +
            Integer.toString(intTests) + ",Seed=" + Long.toString(lngSeed));
        strCache = strDirectoryOut + "/cache.txt";
        strStats = strDirectoryOut + "performance_test_" + Integer.toString(intPixelsX) + "_" +
            Integer.toString(intPixelsY) + "_" + Integer.toString(intBandStart) + "_" +
            Integer.toString(intBandEnd) + "_" + Integer.toString(intRecordsPerTest) + "_" +
            Integer.toString(intTests) + "_" + Long.toString(lngSeed) + 
            "_" + gLogV.TimeOut(gLogV.TimeNow()) + ".csv";
        oswStats = new OutputStreamWriter(new FileOutputStream(strStats));
        oswStats.write("PIXELS_X,PIXELS_Y,BAND_START,BAND_END,RECORDS_PER_TEST,TESTS,SEED,TEST_NUM,TEST_CLASS,"
            + "LAT,LON,START,END,FILES,TIME,SUM\r\n");
        oswStats.close();
        strPrefix = Integer.toString(intPixelsX) + "," + Integer.toString(intPixelsY) + "," +
            Integer.toString(intBandStart) + "," + Integer.toString(intBandEnd) + "," +
            Integer.toString(intRecordsPerTest) + "," + Integer.toString(intTests) + "," +
            Long.toString(lngSeed) + ",";
        intMaxRecords = intRecordsPerTest*16*5;// 16 quarter degree tiles per single degree tile, and allow for more time points
        xStrFileNames = new String[cIntCountTests][intMaxRecords];
        aLngProcessTime = new long[cIntCountTests];
        aLngSum = new long[cIntCountTests];
        aIntFileCount = new int[cIntCountTests];
        xIntOrigin = new int[cIntCountTests][intMaxRecords][cIntCountDimensions];
        xIntShape = new int[cIntCountTests][intMaxRecords][cIntCountDimensions];
        intFiles = gAgrQtr.gAStrFileNamesYear.length;
        rndGen = new Random(lngSeed);
        intMaxX = cIntPixelsQuarterX - intPixelsX;
        intMaxY = cIntPixelsQuarterY - intPixelsY;
        intTotalQtrX = (int)Math.ceil(((double)intPixelsX)/cIntPixelsQuarterX);
        intTotalQtrY = (int)Math.ceil(((double)intPixelsY)/cIntPixelsQuarterY);
        dblMaxFractionLon = 1-((double)cIntPixelsQuarterX*intTotalQtrX)/cIntPixelsOneX;
        dblMaxFractionLat = 1-((double)cIntPixelsQuarterY*intTotalQtrY)/cIntPixelsOneY;
        dblSizeLat = ((double)intPixelsY)/cIntPixelsOneY;
        dblSizeLon = ((double)intPixelsX)/cIntPixelsOneX;
        intBands = intBandEnd-intBandStart+1;intBandOrigin = intBandStart-1;
        intCtr = 0;intAbandonded = 0;
        for (intTest = 0; intTest < intTests; intTest++) {
            /* Keep generating random offsets until find one which has sufficient records in the year; as 
             * rigging the test to only compare overheads if records within a single year are chosen; with different
             * file characteristics. */
            for (intAttempt = 0; intAttempt < 20; intAttempt++) {
                intFile = rndGen.nextInt(intFiles);
                intRecords = gAgrQtr.gAIntRecordsYear[intFile];
                if (intRecords >= intRecordsPerTest) {
                    intX =  ((intMaxX <= 0) ? 0 : rndGen.nextInt(intMaxX));
                    intY =  ((intMaxY <= 0) ? 0 : rndGen.nextInt(intMaxY));
                    if (gAgrQtr.gAIntOffsetYear[intFile] + intRecordsPerTest > intRecords) {
                        intFile = intFile - gAgrQtr.gAIntOffsetYear[intFile]; 
                        if (intRecords > intRecordsPerTest) // randomise within range
                            intFile += rndGen.nextInt(intRecords-intRecordsPerTest);
                    }
                    dblLat = gAgrQtr.gADblLat[intFile];dblLon = gAgrQtr.gADblLon[intFile];
                    if (dblSizeLat > cDblFractionQuarter) {
                        dblFractionLat = Math.abs(dblLat - Math.floor(dblLat));
                        if (dblFractionLat > dblMaxFractionLat)  
                            dblLat = Math.floor(dblLat) + dblMaxFractionLat;
                    }
                    if (dblSizeLon > cDblFractionQuarter) {
                        dblFractionLon = Math.abs(dblLon - Math.floor(dblLon));
                        if (dblFractionLon > dblMaxFractionLon) 
                            dblLon = Math.floor(dblLon) + dblMaxFractionLon;
                    }
                    // offset by the actual position (NOTE: For lat really show offset by a single pixel for the corner)
                    dblStartLat = dblLat + ((double)intY)/cIntPixelsOneY;
                    dblEndLat = dblStartLat + dblSizeLat;
                    dblLon += ((double)intX)/cIntPixelsOneX;dblEndLon = dblLon + dblSizeLon;
                    if ((dblSizeLat > cDblFractionQuarter) || (dblSizeLon > cDblFractionQuarter)) {
                        // work out the start date based on the offset within the year
                        dtmStart = gAgrQtr.CalculateStartDate(dblStartLat, dblEndLat, dblLon, dblEndLon, 
                            cDblFractionQuarter, cDblFractionQuarter, 
                            intRecordsPerTest,gAgrQtr.gADtmTime[intFile],gFunGen);
                        // work out exactly the end date in order to get the specified number of records
                        dtmEnd = gAgrQtr.CalculateEndDate(dblStartLat, dblEndLat, dblLon, dblEndLon, 
                            cDblFractionQuarter, cDblFractionQuarter, dtmStart, intRecordsPerTest);
                    }
                    else {
                        dtmStart = gAgrQtr.gADtmTime[intFile];
                        dtmEnd = gAgrQtr.gADtmTime[intFile+intRecordsPerTest-1];
                    }
                    // define the queries
                    for (intCol = 0; intCol < aIntFileCount.length; intCol++) 
                        aIntFileCount[intCol] = 0;
                    gAgrOne.CalculateQueries(dblStartLat, dblEndLat, dblLon, dblEndLon, dtmStart, dtmEnd,cIntTestDegreeOneSingle, 
                        aIntFileCount, xStrFileNames, xIntOrigin, xIntShape,cIntPixelsOneX,cIntPixelsOneY);
                    gAgrHlf.CalculateQueries(dblStartLat, dblEndLat, dblLon, dblEndLon, dtmStart, dtmEnd,cIntTestDegreeHalfSingle,
                        aIntFileCount, xStrFileNames, xIntOrigin, xIntShape,cIntPixelsHalfX,cIntPixelsHalfY);
                    gAgrQtr.CalculateQueries(dblStartLat, dblEndLat, dblLon, dblEndLon, dtmStart, dtmEnd,cIntTestDegreeQuarterSingle, 
                        aIntFileCount, xStrFileNames, xIntOrigin, xIntShape,cIntPixelsQuarterX,cIntPixelsQuarterY);
                    // now execute the queries
                    for (intCol = 0; intCol < cIntCountTests; intCol++) {
                        // flush the disk buffer before the read test
                        gLogV.FlushDiskBuffer(strCache, intFlushBufferSize,lngFlushSize);
                        System.gc();
                        aLngProcessTime[intCol] = 0;
                        aLngSum[intCol] = 0;
                        intLen = aIntFileCount[intCol];
                        for (intRow = 0; intRow < intLen; intRow++) {
                            aLngProcessTime[intCol] += gFunGen.ReadNetCDFEnvelopeWithBandAsVariable(xStrFileNames[intCol][intRow],
                                xIntOrigin[intCol][intRow], xIntShape[intCol][intRow], intBandOrigin, intBandOrigin+intBands);
                            aLngSum[intCol] += gFunGen.gLngSum;
                        }
                    }
                    oswStats = new OutputStreamWriter(new FileOutputStream(strStats,true));
                    for (intCol = 0; intCol < cIntCountTests; intCol++) {
                        oswStats.write(strPrefix + Integer.toString(intTest) + "," +
                            Integer.toString(intCol) + "," + 
                            Double.toString(Math.floor(dblEndLat*cIntQuarters)/cIntQuarters) + "," + 
                            Double.toString(Math.floor(dblLon*cIntQuarters)/cIntQuarters) + "," + 
                            gFunGen.TimeFull(dtmStart) + "," + gFunGen.TimeFull(dtmEnd) + "," + 
                            Integer.toString(aIntFileCount[intCol]) + "," + 
                            Long.toString(aLngProcessTime[intCol]) + "," + Long.toString(aLngSum[intCol]) + "\r\n");
                    }
                    oswStats.close();
                    break;
                }
            }
            if (intAttempt == 20)
                intAbandonded++;
            if (++intCtr == 10) {
                gLogV.println("Processed Test " + Integer.toString(intTest+1) + " of " + 
                    Integer.toString(intTests) + " and Abandonded " + Integer.toString(intAbandonded));
                intCtr = 0;
            }
        }
    }
}
