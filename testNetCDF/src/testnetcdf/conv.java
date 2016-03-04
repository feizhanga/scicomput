/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package testnetcdf;

import java.io.FileOutputStream;
import java.util.List;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.text.ParseException;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriter;
// use the following with NetCDF 4.3
//import ucar.nc2.jni.netcdf.Nc4Chunking;
//import ucar.nc2.jni.netcdf.Nc4ChunkingStrategyImpl;
// use the following with NetCDF 4.5
import ucar.nc2.write.Nc4Chunking; 
import ucar.nc2.write.Nc4ChunkingStrategy;


/**
 * A class to be used to to read a list of NetCDF files and work out some basic information
 * which is stored within them.  It also includes a function to convert the data
 * into one degree, half degree, and quarter degree tiles.
 */
public class conv {
    public static final String cStrFileNamePrefixLandsat5NBAR = functions.cStrFileNamePrefixLandsat5NBAR;
    public static final String cStrFileNameSuffixNetCDF = functions.cStrFileNameSuffixNetCDF;
    public static final String cStrStartTime = gen.cStrStartTime;
    public static final String cStrDirectoryDegreeOne = functions.cStrDirectoryDegreeOne;
    public static final String cStrDirectoryDegreeHalf = functions.cStrDirectoryDegreeHalf;
    public static final String cStrDirectoryDegreeQuarter = functions.cStrDirectoryDegreeQuarter;
    public static final int cIntNullValueBandValue = gen.cIntNullValueBandValue;
    
    protected log gLogV;
    protected String[] gAStrFileNamesRaw;
    protected double[] gADblLat,gADblLon;
    protected Date[] gADtmTime;
    protected byte[] gABytPartial;
    protected double gDblLatMin,gDblLatMax,gDblLonMin,gDblLonMax;
    protected Date gDtmTimeMin,gDtmTimeMax;

    /**
     * Class constructor for the conv class which is used to read a list of NetCDF3 
     * formatted files.
     * 
     * @param strDirectoryIn: The directory which contains the NetCDF 3 formatted files.
     * @param dblLatMin: The minimum latitude range to process.
     * @param dblLatMax: The maximum latitude range to process.
     * @param dblLonMin: The minimum longitude range to process.
     * @param dblLonMax: The maximum longitude range to process.
     * @param strTimeStart: The initial time to process.
     * @param strTimeEnd: The final time to process
     * @param logV: The log class so debug information can be logged to file.
     * @throws ParseException 
     */
    conv(String strDirectoryIn,double dblLatMin, double dblLatMax, double dblLonMin, double dblLonMax,
            String strTimeStart, String strTimeEnd,log logV) throws ParseException {
        functions funGen;
        
        gLogV = logV;
        gDblLatMin = dblLatMin;gDblLatMax = dblLatMax;gDblLonMin = dblLonMin;gDblLonMax = dblLonMax;
        gDtmTimeMin = gLogV.Time(strTimeStart, 0);
        gDtmTimeMax = gLogV.Time(strTimeEnd, -1); // subtract a 
        gLogV.println("Loading File Names in directory " + strDirectoryIn + 
                " with constraints Lat=" + Double.toString(gDblLatMin) + ":" + Double.toString(gDblLatMax) + 
                " Lon=" + Double.toString(gDblLonMin) + ":" + Double.toString(gDblLonMax) + 
                " Time=" + gLogV.TimeOut(gDtmTimeMin) + " - " + gLogV.TimeOut(gDtmTimeMax));
        
        funGen = new functions();
        funGen.ListAndSort(strDirectoryIn,cStrFileNamePrefixLandsat5NBAR,cStrFileNameSuffixNetCDF,gDblLatMin,gDblLatMax,
            gDblLonMin,gDblLonMax,gDtmTimeMin,gDtmTimeMax);
        gAStrFileNamesRaw = funGen.gAStrFileNames;
        gADblLat = funGen.gADblLat;gADblLon = funGen.gADblLon;
        gADtmTime = funGen.gADtmTime;gABytPartial = funGen.gABytPartial;
        gLogV.println("Calculated Partial for " + Integer.toString(gADtmTime.length));
    }
    
    /**
     * Function to work out the pixel use for North/South overlapped Landsat tiles.
     * 
     * @param strDirectoryStats: The directory to export the statistics to.
     * @param intPixelsPerX: The number of pixels in the X dimension (longitude).
     * @param intPixelsPerY: The number of pixels in the Y dimension (latitude).
     * @param intBands: The number of bands to process.
     * @param intThread: The thread identifier.
     *                   NOTE: This is not a multi-threaded application, but it
     *                         is used to keep track of what data needs to be processed.
     * @param intThreads: The number of threads used to generate all the data.
     *                    NOTE: As per the comment for intThread, this is not a 
     *                          multi-threaded application, but this is used to
     *                          keep track of what data needs to be processed.
     * @throws IOException 
     */
    public void CalculateRange(String strDirectoryStats,int intPixelsPerX, int intPixelsPerY, 
            int intBands,int intThread, int intThreads) throws IOException {
        OutputStreamWriter oswStats;
        convVars cov1,cov2,cov3;
        int intFile,intOff;
        String strStats;
        
        strStats = strDirectoryStats + "calculate_envelope_" + Integer.toString(intPixelsPerX) + "_" +
            Integer.toString(intPixelsPerY) + "_" +
            Integer.toString(intThread) + "_" + Integer.toString(intThreads) + 
            "_" + gLogV.TimeOut(gLogV.TimeNow()) + ".csv";
        oswStats = new OutputStreamWriter(new FileOutputStream(strStats));
        oswStats.write("FILE,THREAD,THREADS,FILENAME,PARTIAL,PIXELS_ENV,PIXELS_NULL_IN_ENV,"
            + "PIXELS_NULL_OTHER,BAND_NULL_IN_RANGE,DIFFERENT_PIXEL,DIFFERENT_BAND,"
            + "SAME_PIXEL,SAME_BAND,MIN_X,MAX_X,MIN_Y,MAX_Y\r\n");
        oswStats.close();
        cov1 = new convVars(intPixelsPerX,intPixelsPerY,intBands);
        cov2 = new convVars(intPixelsPerX,intPixelsPerY,intBands);
        cov3 = new convVars(intPixelsPerX,intPixelsPerY,intBands);
        intOff = 0;
        gLogV.println("Thread " + Integer.toString(intThread) + " Starting to Processed " + Integer.toString(gAStrFileNamesRaw.length) + " files");
        for (intFile = intThread; intFile < gAStrFileNamesRaw.length; intFile += intThreads) { 
            if (gABytPartial[intFile] == 0) {
                cov1.init(gAStrFileNamesRaw[intFile],gABytPartial[intFile]);
                cov1.WriteStats(intFile,intThread,intThreads,strStats);
                ++intOff;
            }
            else if (gABytPartial[intFile] == 2) {
                cov1.init(gAStrFileNamesRaw[intFile-1],gABytPartial[intFile-1]);
                cov2.init(gAStrFileNamesRaw[intFile],gABytPartial[intFile]);
                cov3.Merge(cov1, cov2);
                cov1.WriteStats(intFile,intThread,intThreads,strStats);
                cov2.WriteStats(intFile,intThread,intThreads,strStats);
                cov3.WriteStats(intFile,intThread,intThreads,strStats);
                ++intOff;
            }
            if (intOff == 100) {
                gLogV.println("Thread " + Integer.toString(intThread) + " Processed " + 
                    Integer.toString(intFile) + " of " + Integer.toString(gAStrFileNamesRaw.length) + " files");
                intOff = 0;
            }
        }
        gLogV.println("Done");
    }
    
    /**
     * Function to take the source NetCDF3 formatted data and create NetCDF4 
     * formatted data for a single time point, with one degree, half degree,
     * and quarter degree tiles.
     * 
     * @param strDirectoryOutput: The directory to write the output to.
     * @param intPixelsPerX: The number of pixels in the X dimension (longitude).
     * @param intPixelsPerY: The number of pixels in the Y dimension (latitude).
     * @param intBands: The number of bands to process.
     * @param intThread: The thread identifier.
     *                   NOTE: This is not a multi-threaded application, but it
     *                         is used to keep track of what data needs to be processed.
     * @param intThreads: The number of threads used to generate all the data.
     *                    NOTE: As per the comment for intThread, this is not a 
     *                          multi-threaded application, but this is used to
     *                          keep track of what data needs to be processed.
     * @throws IOException
     * @throws ParseException
     * @throws InvalidRangeException 
     */
    public void Convert(String strDirectoryOutput,int intPixelsPerX, int intPixelsPerY, 
            int intBands,int intThread, int intThreads) throws IOException,ParseException,InvalidRangeException {
        OutputStreamWriter oswStats;
        Nc4Chunking nchStrategy;
        convVars cov1,cov2,cov3;
        functions funGen;
        int intFile,intOff;
        short[][][] xShtData;
        short[][][][] xShtOne;
        short[][][][][][] xShtHlf,xShtQtr;
        int[][] xIntHlfCount,xIntQtrCount;
        int intRow,intCol,intBand;
        int intHlfY,intHlfX,intHlfRows,intHlfCols,intHlfRow,intHlfCol,intHlfPosY,intHlfPosX;
        int intQtrY,intQtrX,intQtrRows,intQtrCols,intQtrRow,intQtrCol,intQtrPosY,intQtrPosX;
        short shtVal;
        double[] aDblTime,aDblLatOne,aDblLonOne,aDblLatHlf,aDblLonHlf,aDblLatQtr,aDblLonQtr;
        double dblLatOne,dblLonOne,dblLatHlf,dblLonHlf,dblLatQtr,dblLonQtr;
        Date dtmTime;
        long lngTotalTimeOne,lngTotalTimeHlf,lngTotalTimeQtr;
        long lngTimeOne,lngTimeHlf,lngTimeQtr;
        int intTotalFilesOne,intTotalFilesHlf,intTotalFilesQtr;
        int intFilesOne,intFilesHlf,intFilesQtr;
        String strStats,strFileName,strDirectoryOne,strDirectoryHlf,strDirectoryQtr;
        
        /* The default is as follows, but want to specify contiguous for each variable, so need to do from Attribute 
         * nchStrategy = Nc4ChunkingStrategyImpl.factory(Nc4Chunking.Strategy.standard, 0, false); 
         */
        // use the following for NetCDF 4.3
        //nchStrategy = Nc4ChunkingStrategyImpl.factory(Nc4Chunking.Strategy.fromAttribute, 0, false); // specify from attribute without compression (0) or shuffling
        // use the following with NetCDF 4.5
        nchStrategy = Nc4ChunkingStrategy.factory(Nc4Chunking.Strategy.standard,0, false); // specify without compression (0) or shuffling
        funGen = new functions();
        
        strStats = strDirectoryOutput + "convert_envelope_" + Integer.toString(intPixelsPerX) + "_" +
            Integer.toString(intPixelsPerY) + "_" +
            Integer.toString(intThread) + "_" + Integer.toString(intThreads) + 
            "_" + gLogV.TimeOut(gLogV.TimeNow()) + ".csv";
        oswStats = new OutputStreamWriter(new FileOutputStream(strStats));
        oswStats.write("FILENAME,PARTIAL,FILES_ONE,FILES_HLF,FILES_QTR,TIME_ONE,TIME_HLF,TIME_QTR\r\n");
        oswStats.close();
        cov1 = new convVars(intPixelsPerX,intPixelsPerY,intBands);
        cov2 = new convVars(intPixelsPerX,intPixelsPerY,intBands);
        cov3 = new convVars(intPixelsPerX,intPixelsPerY,intBands);
        xShtOne = new short[intBands][1][intPixelsPerY][intPixelsPerX]; // only 1 time point
        intHlfRows = 2;intHlfCols = 2;
        intHlfY = intPixelsPerY/intHlfRows;intHlfX = intPixelsPerX/intHlfCols;
        intQtrRows = 4;intQtrCols = 4;
        intQtrY = intPixelsPerY/intQtrRows;intQtrX = intPixelsPerX/intQtrCols;
        xShtHlf = new short[intHlfRows][intHlfCols][intBands][1][intHlfY][intHlfX]; // only 1 time point
        xShtQtr = new short[intQtrRows][intQtrCols][intBands][1][intQtrY][intQtrX];
        xIntHlfCount = new int[intHlfRows][intHlfCols];
        xIntQtrCount = new int[intQtrRows][intQtrCols];
        intOff = 0;
        strDirectoryOne = strDirectoryOutput + "/" + cStrDirectoryDegreeOne + "/";gLogV.MakeDirectory(strDirectoryOne);
        strDirectoryHlf = strDirectoryOutput + "/" + cStrDirectoryDegreeHalf + "/";gLogV.MakeDirectory(strDirectoryHlf);
        strDirectoryQtr = strDirectoryOutput + "/" + cStrDirectoryDegreeQuarter + "/";gLogV.MakeDirectory(strDirectoryQtr);
        aDblTime = new double[1]; // only one time point
        aDblLatOne = new double[intPixelsPerY];
        aDblLonOne = new double[intPixelsPerX];
        aDblLatHlf = new double[intPixelsPerY/2];
        aDblLonHlf = new double[intPixelsPerX/2];
        aDblLatQtr = new double[intPixelsPerY/4];
        aDblLonQtr = new double[intPixelsPerX/4];
        
        gLogV.println("Thread " + Integer.toString(intThread) + " Starting to Processed " + Integer.toString(gAStrFileNamesRaw.length) + " files");
        lngTotalTimeOne = 0;lngTotalTimeHlf = 0;lngTotalTimeQtr = 0;
        intTotalFilesOne = 0;intTotalFilesHlf = 0;intTotalFilesQtr = 0;
        for (intFile = intThread; intFile < gAStrFileNamesRaw.length; intFile += intThreads) { 
            if (gABytPartial[intFile] == 0) {
                cov1.init(gAStrFileNamesRaw[intFile],gABytPartial[intFile]);
                xShtData = cov1.gXShtData;
                ++intOff;
            }
            else if (gABytPartial[intFile] == 2) {
                cov1.init(gAStrFileNamesRaw[intFile-1],gABytPartial[intFile-1]);
                cov2.init(gAStrFileNamesRaw[intFile],gABytPartial[intFile]);
                cov3.Merge(cov1, cov2);
                xShtData = cov3.gXShtData;
                ++intOff;
            }
            else {
                xShtData = null;
            }
            if (xShtData != null) {
                dblLatOne = gADblLat[intFile];dblLonOne = gADblLon[intFile];
                dtmTime = gADtmTime[intFile];
                aDblTime[0] = funGen.DifferenceSeconds(gLogV.Time(cStrStartTime, 0), dtmTime); 
                
                // erase the counts
                for (intHlfRow = 0; intHlfRow < xIntHlfCount.length; intHlfRow++) {
                    for (intHlfCol = 0; intHlfCol < xIntHlfCount[0].length; intHlfCol++) {
                        xIntHlfCount[intHlfRow][intHlfCol] = 0;
                    }
                }
                for (intQtrRow = 0; intQtrRow < xIntQtrCount.length; intQtrRow++) {
                    for (intQtrCol = 0; intQtrCol < xIntQtrCount[0].length; intQtrCol++) {
                        xIntQtrCount[intQtrRow][intQtrCol] = 0;
                    }
                }
                // copy the data into the relevant one, half and quarter degree tiles
                intHlfPosY = 0;intQtrPosY = 0;
                for (intRow = 0,intHlfRow = 0,intQtrRow = 0; intRow < intPixelsPerY; intRow++) {
                    intHlfPosX = 0;intQtrPosX = 0;
                    for (intCol = 0,intHlfCol = 0,intQtrCol = 0; intCol < intPixelsPerX; intCol++) {
                        for (intBand = 0; intBand < intBands; intBand++) {
                            shtVal = xShtData[intRow][intCol][intBand]; // stored in different order in the cov class
                            xShtOne[intBand][0][intRow][intCol] = shtVal;
                            xShtHlf[intHlfPosY][intHlfPosX][intBand][0][intHlfRow][intHlfCol] = shtVal;
                            xShtQtr[intQtrPosY][intQtrPosX][intBand][0][intQtrRow][intQtrCol] = shtVal;
                            if (shtVal != cIntNullValueBandValue) {
                                xIntHlfCount[intHlfPosY][intHlfPosX]++;
                                xIntQtrCount[intQtrPosY][intQtrPosX]++; 
                            }
                        }
                        if (++intHlfCol == intHlfX) {
                            intHlfCol = 0;intHlfPosX++;
                        }
                        if (++intQtrCol == intQtrX) {
                            intQtrCol = 0;intQtrPosX++;
                        }
                    }
                    if (++intHlfRow == intHlfY) {
                        intHlfRow = 0;intHlfPosY++;
                        
                    }
                    if (++intQtrRow == intQtrY) {
                        intQtrRow = 0;intQtrPosY++;
                    }
                }
                lngTimeOne = 0;lngTimeHlf = 0;lngTimeQtr = 0;
                intFilesOne = 0;intFilesHlf = 0;intFilesQtr = 0;
                // write the data to the one degree tile
                strFileName = funGen.FileName(strDirectoryOne,cStrFileNamePrefixLandsat5NBAR,
                    cStrFileNameSuffixNetCDF,dblLatOne,dblLonOne,dtmTime,true);
                lngTimeOne += funGen.WriteNetCDFWithBandAsVariable(NetcdfFileWriter.Version.netcdf4, strFileName,
                    false,nchStrategy,true,true,0,false,aDblTime,dblLatOne,dblLonOne,
                    aDblLatOne,aDblLonOne,xShtOne,functions.cStrCoordinateDescriptionTimeLatLon,
                    functions.cStrCoordinateFieldsTimeLatLon,intPixelsPerX,intPixelsPerY,null);// as contiguous don't need to provide the shape
                intFilesOne++;
                // write the relevant half tiles
                dblLatHlf = dblLatOne+0.5;dblLonHlf = dblLonOne;
                for (intHlfRow = 0; intHlfRow < xIntHlfCount.length; intHlfRow++) {
                    for (intHlfCol = 0; intHlfCol < xIntHlfCount[0].length; intHlfCol++) {
                        if (xIntHlfCount[intHlfRow][intHlfCol] > 0) {
                            strFileName = funGen.FileName(strDirectoryHlf,cStrFileNamePrefixLandsat5NBAR,
                                cStrFileNameSuffixNetCDF,dblLatHlf,dblLonHlf,dtmTime,false);
                            lngTimeHlf += funGen.WriteNetCDFWithBandAsVariable(NetcdfFileWriter.Version.netcdf4, strFileName,
                                false,nchStrategy,true,true,0,false,aDblTime,dblLatHlf,dblLonHlf,
                                aDblLatHlf,aDblLonHlf,xShtHlf[intHlfRow][intHlfCol],
                                functions.cStrCoordinateDescriptionTimeLatLon,functions.cStrCoordinateFieldsTimeLatLon,
                                intHlfX,intHlfY,null); // as contiguous don't need to provide the shape
                            intFilesHlf++;
                        }
                        dblLonHlf += 0.5;
                    }
                    dblLatHlf -= 0.5;dblLonHlf = dblLonOne;
                }
                // write the relevant quarter tiles
                dblLatQtr = dblLatOne+0.75;dblLonQtr = dblLonOne;
                for (intQtrRow = 0; intQtrRow < xIntQtrCount.length; intQtrRow++) {
                    for (intQtrCol = 0; intQtrCol < xIntQtrCount[0].length; intQtrCol++) {
                        if (xIntQtrCount[intQtrRow][intQtrCol] > 0) {
                            strFileName = funGen.FileName(strDirectoryQtr,cStrFileNamePrefixLandsat5NBAR,
                                cStrFileNameSuffixNetCDF,dblLatQtr,dblLonQtr,dtmTime,false);
                            lngTimeQtr += funGen.WriteNetCDFWithBandAsVariable(NetcdfFileWriter.Version.netcdf4, strFileName,
                                false,nchStrategy,true,true,0,false,aDblTime,dblLatQtr,dblLonQtr,
                                aDblLatQtr,aDblLonQtr,xShtQtr[intQtrRow][intQtrCol],
                                functions.cStrCoordinateDescriptionTimeLatLon,functions.cStrCoordinateFieldsTimeLatLon,
                                intQtrX,intQtrY,null);// as contiguous don't need to provide the shape
                            intFilesQtr++; 
                        }
                        dblLonQtr += 0.25;
                    }
                    dblLatQtr -= 0.25;dblLonQtr = dblLonOne;
                }
                oswStats = new OutputStreamWriter(new FileOutputStream(strStats,true));
                oswStats.write("\"" + gAStrFileNamesRaw[intFile] + "\"," +
                    (gABytPartial[intFile] == 0 ? "FALSE" : "TRUE") + 
                    Integer.toString(intFilesOne) + "," + 
                    Integer.toString(intFilesHlf) + "," + 
                    Integer.toString(intFilesQtr) + "," + 
                    Long.toString(lngTimeOne) + "," + 
                    Long.toString(lngTimeHlf) + "," + 
                    Long.toString(lngTimeQtr) + "\r\n");
                oswStats.close();
                intTotalFilesOne += intFilesOne;
                intTotalFilesHlf += intFilesHlf;
                intTotalFilesQtr += intFilesQtr;
                lngTotalTimeOne += lngTimeOne;
                lngTotalTimeHlf += lngTimeHlf;
                lngTotalTimeQtr += lngTimeQtr;
            }
            if (intOff == 100) {
                gLogV.println("Thread " + Integer.toString(intThread) + " Processed " + 
                    Integer.toString(intFile) + " of " + Integer.toString(gAStrFileNamesRaw.length) + " files.  Produced " + 
                    Integer.toString(intTotalFilesOne) + "," + 
                    Integer.toString(intTotalFilesHlf) + "," + 
                    Integer.toString(intTotalFilesQtr) + " Files with cost " + 
                    Long.toString(lngTotalTimeOne) + "," + 
                    Long.toString(lngTotalTimeHlf) + "," + 
                    Long.toString(lngTotalTimeQtr) + " milliseconds");
                intOff = 0;
            }
        }
        gLogV.println("Done");
    }
}

class convVars {
    public static final byte cBytMerged1 = 4;
    public static final byte cBytMerged2 = 8;
    public static final byte cBytMerged3 = 16;

    public NetcdfFile gNcfD;
    public List<Variable> gLVar;
    public Variable gVarV;
    public Array gAr;
    public short[][][] gXShtData;
    public long gLngVal;
    public int gIntPixels,gIntPixelsX,gIntPixelsY,gIntBands;
    public int gIntMinX,gIntMaxX,gIntMinY,gIntMaxY,gIntNullPixel,gIntNullBand;
    public int gIntPixelsInRange,gIntPixelsOutOfRange,gIntNullBandInRange;
    public int gIntDifferentBand,gIntDifferentPixel;
    public int gIntSameBand,gIntSamePixel;
    public String gStrFileName;
    public byte gBytPartial;
    
    /**
     * Class to keep track of the information for a given NetCDF file.
     * 
     * @param intPixelsX: The number of pixels in the X dimension (longitude).
     * @param intPixelsY: The number of pixels in the Y dimension (latitude).
     * @param intBands: The number of bands to be processed.
     */
    convVars(int intPixelsX, int intPixelsY,int intBands) {
        gIntPixelsX = intPixelsX;
        gIntPixelsY = intPixelsY;
        gIntPixels = gIntPixelsX*gIntPixelsY;
        gIntBands = intBands;
        gIntDifferentBand = 0;gIntDifferentPixel = 0;
    }
    
    /**
     * Function to initialise the data to a blank state, so it can be re-used.
     */
    public void init() {
        int intRow, intCol, intBand;
        
        if (gXShtData == null) 
            gXShtData = new short[gIntPixelsY][gIntPixelsX][gIntBands];
        else {
            // erase the data first
            for (intRow = gIntMinY; intRow <= gIntMaxY; intRow++) {
                for (intCol = gIntMinX; intCol <= gIntMaxX; intCol++) {
                    for (intBand = 0; intBand < gIntBands; intBand++) {
                        gXShtData[intRow][intCol][intBand] = gen.cIntNullValueBandValue;
                    }
                }
            }
        }
        gIntMinX = Integer.MAX_VALUE;gIntMinY = Integer.MAX_VALUE;
        gIntMaxX = Integer.MIN_VALUE;gIntMaxY = Integer.MIN_VALUE;
        gIntNullPixel = 0;gIntNullBand = 0;
        gIntDifferentBand = 0;gIntDifferentPixel = 0;
    }
    
    /**
     * Function to load the given NetCDF file with a specification of whether
     * the file contains partial data (North/South overlap).
     * 
     * @param strFileName: The NetCDF filename.
     * @param bytPartial: Specifies whether this is a normal tile (0), 
     *                    the first instance in a North/South overlap (1),
     *                    or the second instance in a North/South overlap (2).
     * @throws IOException 
     */
    public void init(String strFileName,byte bytPartial) throws IOException {
        init();
        gNcfD = NetcdfFile.open(strFileName, null);
        gLVar = gNcfD.getVariables();
        if (strFileName.endsWith(".tif"))
            gStrFileName = strFileName.substring(strFileName.length()-51, strFileName.length());
        else
            gStrFileName = strFileName.substring(strFileName.length()-50, strFileName.length());
        gBytPartial = bytPartial;
        Load();
    }
    
    /**
     * Function to load data from a NetCDF file.
     * @throws IOException 
     */
    public void Load() throws IOException {
        int intVar,intCol,intRow,intOff,intBand;
        
        intBand = 0;
        for (intVar = 0; intVar < gLVar.size(); intVar++) {
            gVarV = gLVar.get(intVar);
            if (gVarV.getName().startsWith("Band")) {
                gAr = gVarV.read();intCol = 0;intRow = 0;
                for (intOff = 0; intOff < gAr.getSize(); intOff++) {
                    gLngVal = gAr.getShort(intOff);
                    Store(gLngVal,intRow,intCol,intBand);
                    if (++intCol == gIntPixelsX) {
                        intCol = 0;
                        intRow++;
                    }
                }
                intBand++;
            }
        }
        gNcfD.close();
        CalculateStats();
    }
    
    /**
     * Function to merge data from two tiles, because they exhibited a North/South overlap.
     * 
     * @param cov1: The class containing data for the first instance.
     * @param cov2: The class containing data for the second instance.
     */
    public void Merge(convVars cov1,convVars cov2) {
        int intCol,intRow,intBand;
        int intDifferent,intSame,intNullBand,intPixels1,intPixels2;
        
        init();
        gIntMinX = Math.min(cov1.gIntMinX, cov2.gIntMinX);gIntMaxX = Math.max(cov1.gIntMaxX, cov2.gIntMaxX);
        gIntMinY = Math.min(cov1.gIntMinY, cov2.gIntMinY);gIntMaxY = Math.max(cov1.gIntMaxY, cov2.gIntMaxY);
        gIntPixelsInRange = (gIntMaxY-gIntMinY+1)*(gIntMaxX-gIntMinX+1);
        gIntPixelsOutOfRange = (gIntPixels-gIntPixelsInRange);
        intCol = cov1.gIntPixelsInRange - cov1.gIntNullPixel;
        intRow = cov2.gIntPixelsInRange - cov2.gIntNullPixel;
        if (gIntPixelsInRange == cov2.gIntPixelsInRange) {
            if (gIntPixelsInRange == cov1.gIntPixelsInRange) {
                if (cov1.gIntNullPixel < cov2.gIntNullPixel) 
                    gBytPartial = cBytMerged1;
                else if (cov1.gIntNullPixel > cov2.gIntNullPixel) 
                    gBytPartial = cBytMerged2;
                else if (cov1.gIntNullBand < cov2.gIntNullBand) 
                    gBytPartial = cBytMerged1;
                else if (cov1.gIntNullBand > cov2.gIntNullBand) 
                    gBytPartial = cBytMerged2;
                else  // same number of pixels, same number of null pixels, and same number of null bands
                    gBytPartial = cBytMerged3;
            }
            else 
                gBytPartial = cBytMerged2;
        }
        else if (gIntPixelsInRange == cov1.gIntPixelsInRange) 
            gBytPartial = cBytMerged1;
        else 
            gBytPartial = cBytMerged3;
        gIntDifferentBand = 0;gIntDifferentPixel = 0;
        gIntNullPixel = 0;gIntNullBandInRange = 0;intPixels1 = 0;intPixels2 = 0;
        for (intRow = gIntMinY; intRow <= gIntMaxY; intRow++) {
            for (intCol = gIntMinX; intCol <= gIntMaxX; intCol++) {
                intDifferent = 0;intSame = 0;intNullBand = 0;
                for (intBand = 0; intBand < gIntBands; intBand++) {
                    gXShtData[intRow][intCol][intBand] = cov1.gXShtData[intRow][intCol][intBand];
                    if (gXShtData[intRow][intCol][intBand] == gen.cIntNullValueBandValue) {
                        gXShtData[intRow][intCol][intBand] = cov2.gXShtData[intRow][intCol][intBand]; 
                        if (gXShtData[intRow][intCol][intBand] == gen.cIntNullValueBandValue) {
                            gIntNullBandInRange++;
                            intNullBand++;
                        }
                        else 
                            intPixels2++;
                    }
                    else if (cov2.gXShtData[intRow][intCol][intBand] != gen.cIntNullValueBandValue) {
                        if (gXShtData[intRow][intCol][intBand] != cov2.gXShtData[intRow][intCol][intBand]) {
                            intDifferent++;
                            if (gBytPartial == cBytMerged2) { // keep the second as it is statistically better
                                gXShtData[intRow][intCol][intBand] = cov2.gXShtData[intRow][intCol][intBand];
                                intPixels2++;
                            }
                            else
                                intPixels1++;
                        }
                        else {
                            intSame++;
                            if (gBytPartial == cBytMerged2)  
                                intPixels2++;
                            else
                                intPixels1++;
                        }
                    }
                    else
                        intPixels1++;
                }
                if (intNullBand == gIntBands)
                    gIntNullPixel++;
                if (intDifferent > 0) {
                    gIntDifferentBand += intDifferent;
                    if (intDifferent == gIntBands)
                        gIntDifferentPixel++;
                }
                if (intSame > 0) {
                    gIntSameBand += intSame;
                    if (intSame == gIntBands) 
                        gIntSamePixel++;
                }
            }
        }
        if ((gBytPartial != cBytMerged3) && (intPixels1 > 0) && (intPixels2 > 0)) 
            gBytPartial += cBytMerged3; // need to merge to get all the relevant data
        if (gBytPartial == cBytMerged1) 
            gStrFileName = cov1.gStrFileName;
        else if (gBytPartial == cBytMerged2) 
            gStrFileName = cov2.gStrFileName;
        else 
            gStrFileName = cov1.gStrFileName + ";" + cov2.gStrFileName;
        gIntNullBand = gIntNullBandInRange + gIntPixelsOutOfRange*gIntBands;
    }
    
    /**
     * Function to store a pixel value in the class.
     * 
     * @param lngVal: The pixel value.
     * @param intRow: The row to store the pixel data.
     * @param intCol: The column to store the pixel data.
     * @param intBand: The band to store the pixel data.
     */
    public void Store(long lngVal, int intRow, int intCol, int intBand) {
        gXShtData[intRow][intCol][intBand] = (short)lngVal;
        if (lngVal != gen.cIntNullValueBandValue) {
            if (intCol < gIntMinX)
                gIntMinX = intCol;
            if (intCol > gIntMaxX)
                gIntMaxX = intCol;
            if (intRow < gIntMinY)
                gIntMinY = intRow;
            if (intRow > gIntMaxY)
                gIntMaxY = intRow;
        }
        else {
            gIntNullBand++;
        }
    }
    
    /**
     * Function to Calculate the statistics associated with this file.
     */
    public void CalculateStats() {
        int intRow, intCol, intBand;
        // work out whether the entire pixel is null
        gIntNullPixel = 0;
        for (intRow = gIntMinY; intRow <= gIntMaxY; intRow++) {
            for (intCol = gIntMinX; intCol <= gIntMaxX; intCol++) {
                gLngVal = gen.cIntNullValueBandValue;
                for (intBand = 0; intBand < gIntBands; intBand++) {
                    if (gXShtData[intRow][intCol][intBand] != gen.cIntNullValueBandValue) {
                        gLngVal = gXShtData[intRow][intCol][intBand];
                        break;
                    }
                }
                if (gLngVal == gen.cIntNullValueBandValue) 
                    gIntNullPixel++;
            }
        }
        if (gIntMinX < Integer.MAX_VALUE) {
            gIntPixelsInRange = (gIntMaxY-gIntMinY+1)*(gIntMaxX-gIntMinX+1);
            gIntPixelsOutOfRange = (gIntPixels-gIntPixelsInRange);
            gIntNullBandInRange = gIntNullBand - gIntPixelsOutOfRange*gIntBands;
        }
        else {
            gIntPixelsInRange = 0;
            gIntPixelsOutOfRange = 0;
            gIntNullBandInRange = 0;
        }
    }
    
    /**
     * Function to write the statistics for this file.
     * 
     * @param intFile: The file number.
     * @param intThread: The thread used to process this file.
     * @param intThreads: The total number of threads used to process all files.
     * @param strStats: The statistics file name.
     * @throws IOException 
     */
    public void WriteStats(int intFile,int intThread,int intThreads,String strStats) throws IOException {
        OutputStreamWriter oswStats;
        oswStats = new OutputStreamWriter(new FileOutputStream(strStats,true));
        oswStats.write(Integer.toString(intFile) + "," + Integer.toString(intThread) + "," + Integer.toString(intThreads) + ",");
        oswStats.write("\"" + gStrFileName + "\"," + Byte.toString(gBytPartial) + ",");
        if (gIntMinX < Integer.MAX_VALUE) {
            oswStats.write(Integer.toString(gIntPixelsInRange) + "," + 
                Integer.toString(gIntNullPixel) + "," + 
                Integer.toString(gIntPixelsOutOfRange) + "," +     
                Integer.toString(gIntNullBandInRange) + "," + 
                Integer.toString(gIntDifferentPixel) + "," +     
                Integer.toString(gIntDifferentBand) + "," +     
                Integer.toString(gIntSamePixel) + "," +     
                Integer.toString(gIntSameBand) + "," +    
                Integer.toString(gIntMinX) + "," + 
                Integer.toString(gIntMaxX) + "," + Integer.toString(gIntMinY) + "," + 
                Integer.toString(gIntMaxY) + "\r\n");
        }
        oswStats.close();
    }
}