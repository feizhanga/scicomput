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
import static testnetcdf.test.cDblFractionQuarter;
import static testnetcdf.test.cIntCountDimensions;
import static testnetcdf.test.cIntPixelsOneX;
import static testnetcdf.test.cIntPixelsOneY;
import static testnetcdf.test.cIntPixelsQuarterX;
import static testnetcdf.test.cIntPixelsQuarterY;
import static testnetcdf.test.cIntQuarters;
import static testnetcdf.test.cStrDirectoryDegreeQuarter;
import ucar.ma2.InvalidRangeException;
/**
 * A class to be used to run performance tests comparing the same yearly aggregated
 * data stored as NetCDF files in the same spatial coordinates, but different 
 * dimension order.
 */
public class testDim {
    public static final int cIntCountTests = 6; // time lat lon, lat lon time, time lat lon band, lat lon time band, band time lat lon, time band lat lon
    public static final int cIntConfigurationTimeLatLonDimensionTime = 0;
    public static final int cIntConfigurationTimeLatLonDimensionLat = 1;
    public static final int cIntConfigurationTimeLatLonDimensionLon = 2;
    public static final int cIntConfigurationTimeLatLonDimensionBand = 3;
    public static final int cIntConfigurationLatLonTimeDimensionTime = 2;
    public static final int cIntConfigurationLatLonTimeDimensionLat = 0;
    public static final int cIntConfigurationLatLonTimeDimensionLon = 1;
    public static final int cIntConfigurationLatLonTimeDimensionBand = 3;
    public static final int cIntConfigurationBandTimeLatLonDimensionTime = 1;
    public static final int cIntConfigurationBandTimeLatLonDimensionLat = 2;
    public static final int cIntConfigurationBandTimeLatLonDimensionLon = 3;
    public static final int cIntConfigurationBandTimeLatLonDimensionBand = 0;
    public static final int cIntConfigurationTimeBandLatLonDimensionTime = 0;
    public static final int cIntConfigurationTimeBandLatLonDimensionLat = 2;
    public static final int cIntConfigurationTimeBandLatLonDimensionLon = 3;
    public static final int cIntConfigurationTimeBandLatLonDimensionBand = 1;
    
    public static final int cIntTestTimeLatLon = 0;
    public static final int cIntTestLatLonTime = 1;
    public static final int cIntTestTimeLatLonBand = 2;
    public static final int cIntTestLatLonTimeBand = 3;
    public static final int cIntTestBandTimeLatLon = 4;
    public static final int cIntTestTimeBandLatLon = 5;
    
    
    public log gLogV;
    public functions gFunGen;
    public aggregate gAgrTimeLatLon,gAgrTimeLatLonBand,gAgrLatLonTime,gAgrLatLonTimeBand;
    public aggregate gAgrBandTimeLatLon,gAgrTimeBandLatLon;
    
    /**
     * Class constructor.
     * 
     * @param strDirectorySingle: The directory which contains the files with Single
     *                            time records per file.
     *                            NOTE: This is only used to calculate the details
     *                                  for each record. The files are never processed.
     * @param strDirectoryTimeLatLon: The directory which contains yearly time aggregated files
     *                                with Band as a Variable and in time latitude longitude
     *                                dimension order.
     * @param strDirectoryTimeLatLonBand: The directory which contains yearly time aggregated files
     *                                    with Band as a Dimension and in time latitude longitude band
     *                                    dimension order.
     * @param strDirectoryLatLonTime: The directory which contains yearly time aggregated files
     *                                with Band as a Variable and in latitude longitude time
     *                                dimension order.
     * @param strDirectoryLatLonTimeBand: The directory which contains yearly time aggregated files
     *                                    with Band as a Dimension and in latitude longitude time band
     *                                    dimension order.
     * @param strDirectoryBandTimeLatLon: The directory which contains yearly time aggregated files
     *                                    with Band as a Dimension and in band time latitude longitude
     *                                    dimension order.
     * @param strDirectoryTimeBandLatLon: The directory which contains yearly time aggregated files
     *                                    with Band as a Dimension and in time band latitude longitude
     *                                    dimension order.
     * @param logV: The class to keep track of any debug information.
     * @throws ParseException
     * @throws IOException 
     */
    testDim(String strDirectorySingle, String strDirectoryTimeLatLon, String strDirectoryTimeLatLonBand,
            String strDirectoryLatLonTime, String strDirectoryLatLonTimeBand, 
            String strDirectoryBandTimeLatLon, String strDirectoryTimeBandLatLon, log logV) throws ParseException, IOException {
        gLogV = logV;
        /* First load all one degree file names, then the half, then the quarter
         * These structures will contain the names of the files for single record, monthly, quarterly, and yearly instances. */
        gLogV.println("Loading Time Lat Lon");
        gAgrTimeLatLon = new aggregate(strDirectorySingle+ "/" + cStrDirectoryDegreeQuarter + "/",
            strDirectoryTimeLatLon + "/" + cStrDirectoryDegreeQuarter + "/",false,logV);
        gAgrTimeLatLonBand = new aggregate(strDirectorySingle+ "/" + cStrDirectoryDegreeQuarter + "/",
            strDirectoryTimeLatLonBand + "/" + cStrDirectoryDegreeQuarter + "/",false,logV);
        gAgrLatLonTime = new aggregate(strDirectorySingle+ "/" + cStrDirectoryDegreeQuarter + "/",
            strDirectoryLatLonTime + "/" + cStrDirectoryDegreeQuarter + "/",false,logV);
        gAgrLatLonTimeBand = new aggregate(strDirectorySingle+ "/" + cStrDirectoryDegreeQuarter + "/",
            strDirectoryLatLonTimeBand + "/" + cStrDirectoryDegreeQuarter + "/",false,logV);
        gAgrBandTimeLatLon = new aggregate(strDirectorySingle+ "/" + cStrDirectoryDegreeQuarter + "/",
            strDirectoryBandTimeLatLon + "/" + cStrDirectoryDegreeQuarter + "/",false,logV); 
        gAgrTimeBandLatLon = new aggregate(strDirectorySingle+ "/" + cStrDirectoryDegreeQuarter + "/",
            strDirectoryTimeBandLatLon + "/" + cStrDirectoryDegreeQuarter + "/",false,logV);
        
        gFunGen = gAgrTimeLatLon.gFunGen;
    }
    
    /**
     * Function to execute the query to compare the performance of NetCDF using
     * the various dimension orders (and Band as a Variable or Dimension).
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
        int[][][] xIntVariableOrigin,xIntVariableShape;
        int[][][] xIntDimensionOrigin,xIntDimensionShape;
        int[] aIntFileCount;
        long[] aLngProcessTime,aLngSum;
        int intMaxRecords,intFiles,intTest,intFile,intAttempt,intAbandonded;
        int intRecords,intLen;
        int intRow,intCol,intX,intY;
        int intMaxX,intMaxY,intCtr,intTotalQtrX,intTotalQtrY,intBands,intBandOrigin;
        Date dtmStart,dtmEnd;
        double dblLat,dblLon,dblStartLat,dblEndLat,dblEndLon,dblSizeLat,dblSizeLon;
        double dblMaxFractionLon,dblMaxFractionLat,dblFractionLon,dblFractionLat;
        String strCache,strStats,strPrefix;
        
        gLogV.println("Running Dimension Tests with Pixels_X=" + Integer.toString(intPixelsX) + ",Pixels_Y=" +
            Integer.toString(intPixelsY) + ",Band_Start=" + Integer.toString(intBandStart) + ",Band_End=" +
            Integer.toString(intBandEnd) + ",Records_Per_Test=" + Integer.toString(intRecordsPerTest) + ",Tests=" +
            Integer.toString(intTests) + ",Seed=" + Long.toString(lngSeed));
        strCache = strDirectoryOut + "/cache.txt";
        strStats = strDirectoryOut + "dimension_test_" + Integer.toString(intPixelsX) + "_" +
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
        intBands = intBandEnd-intBandStart+1;intBandOrigin = intBandStart-1;
        intMaxRecords = intRecordsPerTest*16*5;// 16 quarter degree tiles per single degree tile, and allow for more time points
        xStrFileNames = new String[cIntCountTests][intMaxRecords];
        aLngProcessTime = new long[cIntCountTests];
        aLngSum = new long[cIntCountTests];
        aIntFileCount = new int[cIntCountTests];
        xIntVariableOrigin = new int[cIntCountTests][intMaxRecords][cIntCountDimensions];
        xIntVariableShape = new int[cIntCountTests][intMaxRecords][cIntCountDimensions];
        // the orgin and shape with band as a variable requires one one dimension
        xIntDimensionOrigin = new int[cIntCountTests][intMaxRecords][cIntCountDimensions+1];
        xIntDimensionShape = new int[cIntCountTests][intMaxRecords][cIntCountDimensions+1];
        intFiles = gAgrTimeLatLon.gAStrFileNamesYear.length;
        rndGen = new Random(lngSeed);
        intMaxX = cIntPixelsQuarterX - intPixelsX;
        intMaxY = cIntPixelsQuarterY - intPixelsY;
        intTotalQtrX = (int)Math.ceil(((double)intPixelsX)/cIntPixelsQuarterX);
        intTotalQtrY = (int)Math.ceil(((double)intPixelsY)/cIntPixelsQuarterY);
        dblMaxFractionLon = 1-((double)cIntPixelsQuarterX*intTotalQtrX)/cIntPixelsOneX;
        dblMaxFractionLat = 1-((double)cIntPixelsQuarterY*intTotalQtrY)/cIntPixelsOneY;
        dblSizeLat = ((double)intPixelsY)/cIntPixelsOneY;
        dblSizeLon = ((double)intPixelsX)/cIntPixelsOneX;
        intCtr = 0;intAbandonded = 0;
        for (intTest = 0; intTest < intTests; intTest++) {
            /* Keep generating random offsets until find one which has sufficient records in the year; as 
             * rigging the test to only compare overheads if records within a single year are chosen; with different
             * file characteristics. */
            for (intAttempt = 0; intAttempt < 20; intAttempt++) {
                intFile = rndGen.nextInt(intFiles);
                intRecords = gAgrTimeLatLon.gAIntRecordsYear[intFile];
                if (intRecords >= intRecordsPerTest) {
                    intX =  ((intMaxX <= 0) ? 0 : rndGen.nextInt(intMaxX));
                    intY =  ((intMaxY <= 0) ? 0 : rndGen.nextInt(intMaxY));
                    if (gAgrTimeLatLon.gAIntOffsetYear[intFile] + intRecordsPerTest > intRecords) {
                        intFile = intFile - gAgrTimeLatLon.gAIntOffsetYear[intFile]; 
                        if (intRecords > intRecordsPerTest) // randomise within range
                            intFile += rndGen.nextInt(intRecords-intRecordsPerTest);
                    }
                    dblLat = gAgrTimeLatLon.gADblLat[intFile];dblLon = gAgrTimeLatLon.gADblLon[intFile];
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
                        dtmStart = gAgrTimeLatLon.CalculateStartDate(dblStartLat, dblEndLat, dblLon, dblEndLon, 
                            cDblFractionQuarter, cDblFractionQuarter, intRecordsPerTest,
                            gAgrTimeLatLon.gADtmTime[intFile],gFunGen);
                        // work out exactly the end date in order to get the specified number of records
                        dtmEnd = gAgrTimeLatLon.CalculateEndDate(dblStartLat, dblEndLat, dblLon, dblEndLon, 
                            cDblFractionQuarter, cDblFractionQuarter, dtmStart, intRecordsPerTest);
                    }
                    else {
                        dtmStart = gAgrTimeLatLon.gADtmTime[intFile];
                        dtmEnd = gAgrTimeLatLon.gADtmTime[intFile+intRecordsPerTest-1];
                    }
                    // define the queries
                    for (intCol = 0; intCol < aIntFileCount.length; intCol++) 
                        aIntFileCount[intCol] = 0;
                    gAgrTimeLatLon.CalculateDimensionQueries(dblStartLat, dblEndLat, dblLon, dblEndLon, dtmStart, dtmEnd,cIntTestTimeLatLon, 
                        aIntFileCount, xStrFileNames, xIntVariableOrigin, xIntVariableShape,
                        cIntPixelsQuarterX,cIntPixelsQuarterY,cIntTestTimeLatLon,intBandOrigin,intBands);
                    gAgrLatLonTime.CalculateDimensionQueries(dblStartLat, dblEndLat, dblLon, dblEndLon, dtmStart, dtmEnd,cIntTestLatLonTime, 
                        aIntFileCount, xStrFileNames, xIntVariableOrigin, xIntVariableShape,
                        cIntPixelsQuarterX,cIntPixelsQuarterY,cIntTestLatLonTime,intBandOrigin,intBands);
                    // the following tests need the dimension origin and shape as there is an extra dimension for the band
                    gAgrTimeLatLonBand.CalculateDimensionQueries(dblStartLat, dblEndLat, dblLon, dblEndLon, dtmStart, dtmEnd,cIntTestTimeLatLonBand, 
                        aIntFileCount, xStrFileNames, xIntDimensionOrigin, xIntDimensionShape,
                        cIntPixelsQuarterX,cIntPixelsQuarterY,cIntTestTimeLatLonBand,intBandOrigin,intBands);
                    gAgrLatLonTimeBand.CalculateDimensionQueries(dblStartLat, dblEndLat, dblLon, dblEndLon, dtmStart, dtmEnd,cIntTestLatLonTimeBand, 
                        aIntFileCount, xStrFileNames, xIntDimensionOrigin, xIntDimensionShape,
                        cIntPixelsQuarterX,cIntPixelsQuarterY,cIntTestLatLonTimeBand,intBandOrigin,intBands);
                    gAgrBandTimeLatLon.CalculateDimensionQueries(dblStartLat, dblEndLat, dblLon, dblEndLon, dtmStart, dtmEnd,cIntTestBandTimeLatLon, 
                        aIntFileCount, xStrFileNames, xIntDimensionOrigin, xIntDimensionShape,
                        cIntPixelsQuarterX,cIntPixelsQuarterY,cIntTestBandTimeLatLon,intBandOrigin,intBands);
                    gAgrTimeBandLatLon.CalculateDimensionQueries(dblStartLat, dblEndLat, dblLon, dblEndLon, dtmStart, dtmEnd,cIntTestTimeBandLatLon, 
                        aIntFileCount, xStrFileNames, xIntDimensionOrigin, xIntDimensionShape,
                        cIntPixelsQuarterX,cIntPixelsQuarterY,cIntTestTimeBandLatLon,intBandOrigin,intBands);
                    
                    // now execute the queries
                    for (intCol = 0; intCol < cIntCountTests; intCol++) {
                        // flush the disk buffer before the read test
                        gLogV.FlushDiskBuffer(strCache, intFlushBufferSize,lngFlushSize);
                        System.gc();
                        aLngProcessTime[intCol] = 0;
                        aLngSum[intCol] = 0;
                        intLen = aIntFileCount[intCol];
                        for (intRow = 0; intRow < intLen; intRow++) {
                            if ((intCol == cIntTestTimeLatLon) || (intCol == cIntTestLatLonTime))
                                aLngProcessTime[intCol] += gFunGen.ReadNetCDFEnvelopeWithBandAsVariable(xStrFileNames[intCol][intRow],
                                    xIntVariableOrigin[intCol][intRow], xIntVariableShape[intCol][intRow], intBandOrigin, intBandOrigin+intBands);
                            else if ((intCol == cIntTestTimeLatLonBand) || (intCol == cIntTestLatLonTimeBand)  || 
                            (intCol == cIntTestBandTimeLatLon) || (intCol == cIntTestTimeBandLatLon))
                                aLngProcessTime[intCol] += gFunGen.ReadNetCDFEnvelopeWithBandAsDimension(xStrFileNames[intCol][intRow],
                                    xIntDimensionOrigin[intCol][intRow], xIntDimensionShape[intCol][intRow]);
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
