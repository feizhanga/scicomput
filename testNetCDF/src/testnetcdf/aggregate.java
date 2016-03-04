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
import static testnetcdf.functions.cStrCoordinateDescriptionBandTimeLatLon;
import static testnetcdf.functions.cStrCoordinateDescriptionLatLonTime;
import static testnetcdf.functions.cStrCoordinateDescriptionLatLonTimeBand;
import static testnetcdf.functions.cStrCoordinateDescriptionTimeBandLatLon;
import static testnetcdf.functions.cStrCoordinateDescriptionTimeLatLon;
import static testnetcdf.functions.cStrCoordinateDescriptionTimeLatLonBand;
import static testnetcdf.functions.cStrCoordinateFieldsBandTimeLatLon;
import static testnetcdf.functions.cStrCoordinateFieldsLatLonTime;
import static testnetcdf.functions.cStrCoordinateFieldsLatLonTimeBand;
import static testnetcdf.functions.cStrCoordinateFieldsTimeBandLatLon;
import static testnetcdf.functions.cStrCoordinateFieldsTimeLatLon;
import static testnetcdf.functions.cStrCoordinateFieldsTimeLatLonBand;
import static testnetcdf.functions.cStrDirectoryAggregateMonth;
import static testnetcdf.functions.cStrDirectoryAggregateQuarter;
import static testnetcdf.functions.cStrDirectoryAggregateYear;
import static testnetcdf.functions.cStrFileNamePrefixLandsat5NBAR;
import static testnetcdf.functions.cStrFileNameSuffixNetCDF;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriter;
// use the following with NetCDF 4.3
//import ucar.nc2.jni.netcdf.Nc4Chunking;
//import ucar.nc2.jni.netcdf.Nc4ChunkingStrategyImpl;
// use the following with NetCDF 4.5
import ucar.nc2.write.Nc4Chunking; 
import ucar.nc2.write.Nc4ChunkingStrategy;


import static testnetcdf.testDim.cIntConfigurationBandTimeLatLonDimensionBand;
import static testnetcdf.testDim.cIntConfigurationBandTimeLatLonDimensionLat;
import static testnetcdf.testDim.cIntConfigurationBandTimeLatLonDimensionLon;
import static testnetcdf.testDim.cIntConfigurationBandTimeLatLonDimensionTime;

import static testnetcdf.testDim.cIntConfigurationTimeLatLonDimensionTime;
import static testnetcdf.testDim.cIntConfigurationTimeLatLonDimensionLat;
import static testnetcdf.testDim.cIntConfigurationTimeLatLonDimensionLon;
import static testnetcdf.testDim.cIntConfigurationTimeLatLonDimensionBand;
import static testnetcdf.testDim.cIntConfigurationLatLonTimeDimensionTime;
import static testnetcdf.testDim.cIntConfigurationLatLonTimeDimensionLat;
import static testnetcdf.testDim.cIntConfigurationLatLonTimeDimensionLon;
import static testnetcdf.testDim.cIntConfigurationLatLonTimeDimensionBand;
import static testnetcdf.testDim.cIntConfigurationTimeBandLatLonDimensionBand;
import static testnetcdf.testDim.cIntConfigurationTimeBandLatLonDimensionLat;
import static testnetcdf.testDim.cIntConfigurationTimeBandLatLonDimensionLon;
import static testnetcdf.testDim.cIntConfigurationTimeBandLatLonDimensionTime;
import static testnetcdf.testDim.cIntTestBandTimeLatLon;
import static testnetcdf.testDim.cIntTestLatLonTime;
import static testnetcdf.testDim.cIntTestLatLonTimeBand;
import static testnetcdf.testDim.cIntTestTimeBandLatLon;
import static testnetcdf.testDim.cIntTestTimeLatLon;
import static testnetcdf.testDim.cIntTestTimeLatLonBand;


/**
 * Function to take a list of NetCDF files which cover a single time point and aggregate the 
 * data into specific time ranges (calendar month, quarter, and year).
 */
public class aggregate {
    protected log gLogV;
    protected functions gFunGen;
    // list of files
    protected String[] gAStrFileNamesSingle,gAStrFileNamesMonth,gAStrFileNamesQuarter,gAStrFileNamesYear;
    protected double[] gADblLat,gADblLon;
    protected Date[] gADtmTime;
    protected int[] gAIntRecordsMonth,gAIntRecordsQuarter,gAIntRecordsYear;
    protected int[] gAIntOffsetMonth,gAIntOffsetQuarter,gAIntOffsetYear;
    protected int gIntMaxFilesPerYear;
    protected double gDblMinLat,gDblMaxLat,gDblMinLon,gDblMaxLon;
    
    /**
     * Class constructor to load the list of files in the specific directory.
     * 
     * @param strDirectoryIn: The directory which contains the data to be read.
     * @param strDirectoryOut: The directory export the aggregated data to.
     * @param blnOneDegree: Specifies whether the files contain one degree longitude 
     *                      latitude pairs (TRUE)), or partial degree (FALSE - half
     *                      degree, quarter degree).
     * 
     *                      This affects the file names which are read/written.
     * @param logV: The class to keep track of debug information.
     * @throws ParseException
     * @throws IOException 
     */
    aggregate(String strDirectoryIn, String strDirectoryOut, boolean blnOneDegree, 
            log logV) throws ParseException, IOException {
        this((new functions()).ListAndSort(strDirectoryIn,cStrFileNamePrefixLandsat5NBAR,
            cStrFileNameSuffixNetCDF),strDirectoryOut,blnOneDegree,logV);
    }
    
    /**
     * Class constructor to be used if want to re-use an existing list of files.
     * 
     * @param funGen: The functions class to be used to process the data.
     * @param strDirectoryOut: The directory export the aggregated data to.
     * @param blnOneDegree: Specifies whether the files contain one degree longitude 
     *                      latitude pairs (TRUE)), or partial degree (FALSE - half
     *                      degree, quarter degree).
     * 
     *                      This affects the file names which are read/written.
     * @param logV: The class to keep track of debug information.
     * @throws ParseException
     * @throws IOException 
     */
    aggregate(functions funGen, String strDirectoryOut, boolean blnOneDegree, log logV) throws ParseException, IOException {
        String strDirectoryYear,strDirectoryQuarter,strDirectoryMonth;
        int intFiles,intFile,intPrev;
        
        gLogV = logV;
        gFunGen = funGen;
        gAStrFileNamesSingle = gFunGen.gAStrFileNames;
        gADblLat = gFunGen.gADblLat;gADblLon = gFunGen.gADblLon;
        gADtmTime = gFunGen.gADtmTime;
        // work out the year offsets
        intFiles = gADblLat.length;
        gAIntOffsetMonth = new int[intFiles];
        gAIntOffsetQuarter = new int[intFiles];
        gAIntOffsetYear = new int[intFiles];
        gDblMinLat = Double.MAX_VALUE; gDblMaxLat = Double.MIN_VALUE;
        gDblMinLon = Double.MAX_VALUE; gDblMaxLon = Double.MIN_VALUE;
        for (intPrev = 0,intFile = 1; intFile < intFiles; intFile++,intPrev++) {
            if ((gADblLat[intFile] == gADblLat[intPrev]) && 
            (gADblLon[intFile] == gADblLon[intPrev]) && 
            gFunGen.SameYear(gADtmTime[intFile], gADtmTime[intPrev])) {
                gAIntOffsetYear[intFile] = gAIntOffsetYear[intPrev]+1;
                if (gFunGen.SameQuarter(gADtmTime[intFile], gADtmTime[intPrev])) {
                    gAIntOffsetQuarter[intFile] = gAIntOffsetQuarter[intPrev]+1;
                    if (gFunGen.SameMonth(gADtmTime[intFile], gADtmTime[intPrev])) 
                        gAIntOffsetMonth[intFile] = gAIntOffsetMonth[intPrev]+1;
                    else
                        gAIntOffsetMonth[intFile] = 0;
                }
                else {
                    gAIntOffsetQuarter[intFile] = 0;gAIntOffsetMonth[intFile] = 0;
                }
            }
            else {
                gAIntOffsetYear[intFile] = 0;gAIntOffsetQuarter[intFile] = 0;
                gAIntOffsetMonth[intFile] = 0;
                gDblMinLat = Math.min(gADblLat[intFile], gDblMinLat);
                gDblMinLon = Math.min(gADblLon[intFile], gDblMinLon);
                gDblMaxLat = Math.max(gADblLat[intFile], gDblMaxLat);
                gDblMaxLon = Math.max(gADblLon[intFile], gDblMaxLon);
            }
        }
        // keep track of the number of records per year, month, quarter
        gAIntRecordsMonth = gFunGen.CalculateRecords(gAIntOffsetMonth,new int[intFiles]);
        gAIntRecordsQuarter = gFunGen.CalculateRecords(gAIntOffsetQuarter,new int[intFiles]);
        gAIntRecordsYear = gFunGen.CalculateRecords(gAIntOffsetYear,new int[intFiles]);
        strDirectoryYear = strDirectoryOut + "/" + cStrDirectoryAggregateYear + "/";gLogV.MakeDirectory(strDirectoryYear);
        strDirectoryQuarter = strDirectoryOut + "/" + cStrDirectoryAggregateQuarter + "/";gLogV.MakeDirectory(strDirectoryQuarter);
        strDirectoryMonth = strDirectoryOut + "/" + cStrDirectoryAggregateMonth + "/";gLogV.MakeDirectory(strDirectoryMonth);
        // calculate the file names per year, month, quarter
        gAStrFileNamesMonth = gFunGen.CalculateFileNames(gAIntOffsetMonth,gAIntRecordsMonth,gADblLat,gADblLon,gADtmTime,
            strDirectoryMonth, cStrFileNamePrefixLandsat5NBAR,cStrFileNameSuffixNetCDF,new String[intFiles],1,blnOneDegree);
        gAStrFileNamesQuarter = gFunGen.CalculateFileNames(gAIntOffsetMonth,gAIntRecordsMonth,gADblLat,gADblLon,gADtmTime,
            strDirectoryQuarter, cStrFileNamePrefixLandsat5NBAR,cStrFileNameSuffixNetCDF,new String[intFiles],3,blnOneDegree);
        gAStrFileNamesYear = gFunGen.CalculateFileNames(gAIntOffsetMonth,gAIntRecordsMonth,gADblLat,gADblLon,gADtmTime,
            strDirectoryYear, cStrFileNamePrefixLandsat5NBAR,cStrFileNameSuffixNetCDF,new String[intFiles],12,blnOneDegree);
        
        gIntMaxFilesPerYear = 0;
        for (intFile = 0; intFile < intFiles; intFile++) {
            if (gAIntOffsetYear[intFile] == 0) {
                // work out the maximum number of records
                gIntMaxFilesPerYear = Math.max(gAIntRecordsYear[intFile], gIntMaxFilesPerYear);
            }
        }
        gLogV.println("Loaded " + Integer.toString(intFiles) + " with the Maximum of " + 
                Integer.toString(gIntMaxFilesPerYear) + " files per year.");
    }
    
    /**
     * Function to aggregate all the data into calendar month, quarter, and yearly files.
     * 
     * @param strDirectoryOut: The directory to export the data to.
     * @param intPixelsPerX: The number of pixels in the X dimension (longitude).
     * @param intPixelsPerY: The number of pixels in the Y dimension (latitude).
     * @param intBands: The number of bands to be processed.
     * @param intThread: The thread identifier.
     *                   NOTE: This is not a multi-threaded application, but it
     *                         is used to keep track of what data needs to be processed.
     * @param intThreads: The number of threads used to generate all the data.
     *                    NOTE: As per the comment for intThread, this is not a 
     *                          multi-threaded application, but this is used to
     *                          keep track of what data needs to be processed.
     * @param blnExportAll: Specifies whether the monthly, quarterly, and yearly files 
     *                      should be generated (TRUE).  Otherwise only the yearly files
     *                      are generated.
     * @param intConfiguration: The configuration of the data to be written to the NetCDF file.
     * 
     *                          The possible values are:
     *                              cIntTestTimeLatLon : The Band is a variable and the 
     *                                                   dimensions are time latitude longitude.
     *                              cIntTestLatLonTime : The Band is a variable and the 
     *                                                   dimensions are latitude longitude time.
     *                              cIntTestBandTimeLatLon : The Band is a dimension and the
     *                                                       dimensions are band time latitude longitude.
     *                              cIntTestTimeBandLatLon : The Band is a dimension and the
     *                                                       dimensions are time band latitude longitude.
     *                              cIntTestTimeLatLonBand : The Band is a dimension and the
     *                                                       dimensions are time latitude longitude band.
     *                              cIntTestLatLonTimeBand : The Band is a dimension and the
     *                                                       dimensions are latitude longitude time band.
     * 
     * @throws IOException
     * @throws InvalidRangeException
     * @throws ParseException 
     */
    public void Aggegate(String strDirectoryOut,int intPixelsPerX, int intPixelsPerY, 
            int intBands,int intThread, int intThreads, boolean blnExportAll,
            int intConfiguration) throws IOException,InvalidRangeException,ParseException {
        OutputStreamWriter oswStats;
        Nc4Chunking nchStrategy;
        String strStats,strResult;
        short[][][][] xShtAll,xShtSub;
        double[] aDblLat,aDblLon,aDblTimeAll,aDblTimeSub;
        double dblLat,dblLon;
        int[] aIntBand;
        int intFiles,intFile,intYear,intQuarter,intMonth,intRow,intSingle;
        long lngTimeYear,lngTimeMonth,lngTimeQuarter;
        int intFilesYear,intFilesMonth,intFilesQuarter;
        
        
        /* The default is as follows, but want to specify contiguous for each variable, so need to do from Attribute 
         * nchStrategy = Nc4ChunkingStrategyImpl.factory(Nc4Chunking.Strategy.standard, 0, false); 
         */
        // use the following with NetCDF 4.3
        //nchStrategy = Nc4ChunkingStrategyImpl.factory(Nc4Chunking.Strategy.fromAttribute, 0, false); // specify from attribute without compression (0) or shuffling
        // use the following with NetCDF 4.5
        nchStrategy = Nc4ChunkingStrategy.factory(Nc4Chunking.Strategy.standard,0, false); // specify without compression (0) or shuffling
        intFiles = gAStrFileNamesYear.length;
        if (intConfiguration == cIntTestLatLonTime) {
            xShtAll = new short[intBands][intPixelsPerY][intPixelsPerX][gIntMaxFilesPerYear];
            xShtSub = new short[intBands][][][];
        }
        else if (intConfiguration == cIntTestTimeLatLonBand) {
            xShtAll = new short[gIntMaxFilesPerYear][intPixelsPerY][intPixelsPerX][intBands];
            xShtSub = new short[1][][][];
        }
        else if (intConfiguration == cIntTestLatLonTimeBand) {
            xShtAll = new short[intPixelsPerY][intPixelsPerX][gIntMaxFilesPerYear][intBands];
            xShtSub = new short[intPixelsPerY][][][];
        }
        else if (intConfiguration == cIntTestBandTimeLatLon) {
            xShtAll = new short[intBands][gIntMaxFilesPerYear][intPixelsPerY][intPixelsPerX];
            xShtSub = new short[intBands][][][];
        }
        else if (intConfiguration == cIntTestTimeBandLatLon) {
            xShtAll = new short[gIntMaxFilesPerYear][intBands][intPixelsPerY][intPixelsPerX];
            xShtSub = new short[1][][][];
        }
        else { // default is (intConfiguration == cIntTestTimeLatLon) 
            xShtAll = new short[intBands][gIntMaxFilesPerYear][intPixelsPerY][intPixelsPerX];
            xShtSub = new short[intBands][][][];
        }
        
        aDblLat = new double[intPixelsPerY];
        aDblLon = new double[intPixelsPerX];
        aIntBand = new int[intBands];
        for (intRow = 0; intRow < intBands; intRow++) {
            aIntBand[intRow] = intRow + 1;
        }
        
        strStats = strDirectoryOut + "aggregate_" + Integer.toString(intPixelsPerX) + "_" +
            Integer.toString(intPixelsPerY) + "_" +
            Integer.toString(intThread) + "_" + Integer.toString(intThreads) + 
            "_" + gLogV.TimeOut(gLogV.TimeNow()) + ".csv";
        oswStats = new OutputStreamWriter(new FileOutputStream(strStats));
        oswStats.write("LAT,LON,YEAR,RECORDS,FILES_YEAR,FILES_QUARTER,FILES_MONTH,TIME_YEAR,TIME_QUARTER,TIME_MONTH\r\n");
        oswStats.close();
        
        for (intFile = intThread; intFile < intFiles; intFile += intThreads) { 
            if (gAIntOffsetYear[intFile] == 0) {
                intYear = gAIntRecordsYear[intFile];
                dblLat = gADblLat[intFile];
                dblLon = gADblLon[intFile];
                gLogV.println("Processing " + Double.toString(dblLat) + "," + Double.toString(dblLon) + "," + gFunGen.TimeYear(gADtmTime[intFile]));
                // load the raw data
                aDblTimeAll = new double[intYear];
                for (intRow = 0,intSingle = intFile; intRow < intYear; intRow++,intSingle++) {
                    // store the time
                    aDblTimeAll[intRow] = gFunGen.SecondsSince1970(gADtmTime[intSingle]);
                    gFunGen.ReadNetCDF(gAStrFileNamesSingle[intSingle], xShtAll, intRow, intPixelsPerX,intConfiguration);
                }
                intFilesYear = 1;intFilesMonth = 0;intFilesQuarter = 0;
                lngTimeMonth = 0;lngTimeQuarter = 0;lngTimeYear = 0;
                if (blnExportAll) { // only export the month and quarter if required
                    // export the monthly files
                    gLogV.println("Exporting Months for " + Double.toString(dblLat) + "," + Double.toString(dblLon) + "," + gFunGen.TimeYear(gADtmTime[intFile]));
                    for (intRow = 0,intSingle = intFile; intRow < intYear; intRow++,intSingle++) {
                        if (gAIntOffsetMonth[intSingle] == 0) {
                            intMonth = gAIntRecordsMonth[intSingle];
                            aDblTimeSub = new double[intMonth];
                            if (intConfiguration == cIntTestTimeLatLon) {
                                gFunGen.ExtractSubsetWithBandAsVariableTimeLatLon(xShtAll, aDblTimeAll, xShtSub, aDblTimeSub, intRow, intMonth);
                                lngTimeMonth += gFunGen.WriteNetCDFWithBandAsVariable(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesMonth[intSingle],
                                    false,nchStrategy,true,true,0,false,aDblTimeSub,dblLat,dblLon,aDblLat,aDblLon,xShtSub,
                                    cStrCoordinateDescriptionTimeLatLon,cStrCoordinateFieldsTimeLatLon,
                                    intPixelsPerX,intPixelsPerY,null);// as contiguous don't need to provide the shape
                            }
                            else if (intConfiguration == cIntTestLatLonTime) {
                                gFunGen.ExtractSubsetWithBandAsVariableLatLonTime(xShtAll, aDblTimeAll, xShtSub, aDblTimeSub, intRow, intMonth,
                                    intPixelsPerX,intPixelsPerY);
                                lngTimeMonth = gFunGen.WriteNetCDFWithBandAsVariable(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesMonth[intSingle],
                                    false,nchStrategy,true,true,0,false,aDblTimeAll,dblLat,dblLon,aDblLat,aDblLon,xShtSub,
                                    cStrCoordinateDescriptionLatLonTime,cStrCoordinateFieldsLatLonTime,
                                    intPixelsPerX,intPixelsPerY,null);// as contiguous don't need to provide the shape
                            }
                            else if (intConfiguration == cIntTestTimeLatLonBand) {
                                xShtSub = new short[intMonth][][][]; // have to create a structure with the correct size
                                gFunGen.ExtractSubsetWithBandAsDimensionTimeLatLon(xShtAll, aDblTimeAll, xShtSub, aDblTimeSub, intRow, intMonth);
                                lngTimeMonth = gFunGen.WriteNetCDFWithBandAsDimension(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesMonth[intSingle],
                                    false,nchStrategy,true,true,0,false,aDblTimeAll,dblLat,dblLon,aDblLat,aDblLon,aIntBand,xShtSub,intPixelsPerX,intPixelsPerY,
                                    null,cStrCoordinateDescriptionTimeLatLonBand,cStrCoordinateFieldsTimeLatLonBand);// as contiguous don't need to provide the shape
                            }
                            else if (intConfiguration == cIntTestLatLonTimeBand) {
                                gFunGen.ExtractSubsetWithBandAsDimensionLatLonTime(xShtAll, aDblTimeAll, 
                                    xShtSub, aDblTimeSub, intRow, intMonth,intPixelsPerX,intPixelsPerY);
                                lngTimeMonth = gFunGen.WriteNetCDFWithBandAsDimension(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesMonth[intSingle],
                                    false,nchStrategy,true,true,0,false,aDblTimeAll,dblLat,dblLon,aDblLat,aDblLon,aIntBand,xShtSub,intPixelsPerX,intPixelsPerY,
                                    null,cStrCoordinateDescriptionLatLonTimeBand,cStrCoordinateFieldsLatLonTimeBand);// as contiguous don't need to provide the shape
                            }
                            else if (intConfiguration == cIntTestBandTimeLatLon) {
                                gFunGen.ExtractSubsetWithBandAsDimensionBandTimeLatLon(xShtAll, aDblTimeAll, 
                                    xShtSub, aDblTimeSub, intRow, intMonth,intPixelsPerX,intPixelsPerY);
                                lngTimeMonth = gFunGen.WriteNetCDFWithBandAsDimension(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesMonth[intSingle],
                                    false,nchStrategy,true,true,0,false,aDblTimeAll,dblLat,dblLon,aDblLat,aDblLon,aIntBand,xShtSub,intPixelsPerX,intPixelsPerY,
                                    null,cStrCoordinateDescriptionBandTimeLatLon,cStrCoordinateFieldsBandTimeLatLon);// as contiguous don't need to provide the shape
                            }
                            else if (intConfiguration == cIntTestTimeBandLatLon) {
                                xShtSub = new short[intMonth][][][]; // have to create a structure with the correct size
                                gFunGen.ExtractSubsetWithBandAsDimensionTimeBandLatLon(xShtAll, aDblTimeAll, 
                                    xShtSub, aDblTimeSub, intRow, intMonth,intPixelsPerX,intPixelsPerY);
                                lngTimeMonth = gFunGen.WriteNetCDFWithBandAsDimension(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesMonth[intSingle],
                                    false,nchStrategy,true,true,0,false,aDblTimeAll,dblLat,dblLon,aDblLat,aDblLon,aIntBand,xShtSub,intPixelsPerX,intPixelsPerY,
                                    null,cStrCoordinateDescriptionTimeBandLatLon,cStrCoordinateFieldsTimeBandLatLon);// as contiguous don't need to provide the shape
                            }
                            intFilesMonth++;
                        }
                    }
                    // export the quarterly files
                    gLogV.println("Exporting Quarters for " + Double.toString(dblLat) + "," + Double.toString(dblLon) + "," + gFunGen.TimeYear(gADtmTime[intFile]));
                    for (intRow = 0,intSingle = intFile; intRow < intYear; intRow++,intSingle++) {
                        if (gAIntOffsetQuarter[intSingle] == 0) {
                            intQuarter = gAIntRecordsQuarter[intSingle];
                            aDblTimeSub = new double[intQuarter];
                            if (intConfiguration == cIntTestTimeLatLon) {
                                gFunGen.ExtractSubsetWithBandAsVariableTimeLatLon(xShtAll, aDblTimeAll, xShtSub, aDblTimeSub, intRow, intQuarter);
                                lngTimeQuarter += gFunGen.WriteNetCDFWithBandAsVariable(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesQuarter[intSingle],
                                    false,nchStrategy,true,true,0,false,aDblTimeSub,dblLat,dblLon,aDblLat,aDblLon,xShtSub,
                                    cStrCoordinateDescriptionTimeLatLon,cStrCoordinateFieldsTimeLatLon,
                                    intPixelsPerX,intPixelsPerY,null);// as contiguous don't need to provide the shape
                            }
                            else if (intConfiguration == cIntTestLatLonTime) {
                                gFunGen.ExtractSubsetWithBandAsVariableLatLonTime(xShtAll, aDblTimeAll, xShtSub, aDblTimeSub, intRow, intQuarter,
                                    intPixelsPerX,intPixelsPerY);
                                lngTimeQuarter = gFunGen.WriteNetCDFWithBandAsVariable(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesQuarter[intSingle],
                                    false,nchStrategy,true,true,0,false,aDblTimeAll,dblLat,dblLon,aDblLat,aDblLon,xShtSub,
                                    cStrCoordinateDescriptionLatLonTime,cStrCoordinateFieldsLatLonTime,
                                    intPixelsPerX,intPixelsPerY,null);// as contiguous don't need to provide the shape
                            }
                            else if (intConfiguration == cIntTestTimeLatLonBand) {
                                xShtSub = new short[intQuarter][][][]; // have to create a structure with the correct size
                                gFunGen.ExtractSubsetWithBandAsDimensionTimeLatLon(xShtAll, aDblTimeAll, xShtSub, aDblTimeSub, intRow, intQuarter);
                                lngTimeQuarter = gFunGen.WriteNetCDFWithBandAsDimension(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesQuarter[intSingle],
                                    false,nchStrategy,true,true,0,false,aDblTimeAll,dblLat,dblLon,aDblLat,aDblLon,aIntBand,xShtSub,intPixelsPerX,intPixelsPerY,
                                    null,cStrCoordinateDescriptionTimeLatLonBand,cStrCoordinateFieldsTimeLatLonBand);
                            }
                            else if (intConfiguration == cIntTestLatLonTimeBand) {
                                gFunGen.ExtractSubsetWithBandAsDimensionLatLonTime(xShtAll, aDblTimeAll, 
                                    xShtSub, aDblTimeSub, intRow, intQuarter,intPixelsPerX,intPixelsPerY);
                                lngTimeQuarter = gFunGen.WriteNetCDFWithBandAsDimension(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesQuarter[intSingle],
                                    false,nchStrategy,true,true,0,false,aDblTimeAll,dblLat,dblLon,aDblLat,aDblLon,aIntBand,xShtSub,intPixelsPerX,intPixelsPerY,
                                    null,cStrCoordinateDescriptionLatLonTimeBand,cStrCoordinateFieldsLatLonTimeBand);// as contiguous don't need to provide the shape
                            }
                            else if (intConfiguration == cIntTestBandTimeLatLon) {
                                gFunGen.ExtractSubsetWithBandAsDimensionBandTimeLatLon(xShtAll, aDblTimeAll, 
                                    xShtSub, aDblTimeSub, intRow, intQuarter,intPixelsPerX,intPixelsPerY);
                                lngTimeMonth = gFunGen.WriteNetCDFWithBandAsDimension(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesQuarter[intSingle],
                                    false,nchStrategy,true,true,0,false,aDblTimeAll,dblLat,dblLon,aDblLat,aDblLon,aIntBand,xShtSub,intPixelsPerX,intPixelsPerY,
                                    null,cStrCoordinateDescriptionBandTimeLatLon,cStrCoordinateFieldsBandTimeLatLon);// as contiguous don't need to provide the shape
                            }
                            else if (intConfiguration == cIntTestTimeBandLatLon) {
                                xShtSub = new short[intQuarter][][][]; // have to create a structure with the correct size
                                gFunGen.ExtractSubsetWithBandAsDimensionTimeBandLatLon(xShtAll, aDblTimeAll, 
                                    xShtSub, aDblTimeSub, intRow, intQuarter,intPixelsPerX,intPixelsPerY);
                                lngTimeMonth = gFunGen.WriteNetCDFWithBandAsDimension(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesQuarter[intSingle],
                                    false,nchStrategy,true,true,0,false,aDblTimeAll,dblLat,dblLon,aDblLat,aDblLon,aIntBand,xShtSub,intPixelsPerX,intPixelsPerY,
                                    null,cStrCoordinateDescriptionTimeBandLatLon,cStrCoordinateFieldsTimeBandLatLon);// as contiguous don't need to provide the shape
                            }
                            intFilesQuarter++;
                        }
                    }
                }
                // export the yearly file
                gLogV.println("Exporting Year for " + Double.toString(dblLat) + "," + Double.toString(dblLon) + "," + gFunGen.TimeYear(gADtmTime[intFile]));
                aDblTimeSub = new double[intYear];
                if (intConfiguration == cIntTestTimeLatLon) {
                    gFunGen.ExtractSubsetWithBandAsVariableTimeLatLon(xShtAll, aDblTimeAll, xShtSub, aDblTimeSub, 0, intYear);
                    lngTimeYear += gFunGen.WriteNetCDFWithBandAsVariable(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesYear[intFile],
                        false,nchStrategy,true,true,0,false,aDblTimeSub,dblLat,dblLon,aDblLat,aDblLon,xShtSub,
                        cStrCoordinateDescriptionTimeLatLon,cStrCoordinateFieldsTimeLatLon,
                        intPixelsPerX,intPixelsPerY,null);// as contiguous don't need to provide the shape
                }
                else if (intConfiguration == cIntTestLatLonTime) {
                    gFunGen.ExtractSubsetWithBandAsVariableLatLonTime(xShtAll, aDblTimeAll, xShtSub, aDblTimeSub, 0, intYear,
                        intPixelsPerX,intPixelsPerY);
                    lngTimeYear = gFunGen.WriteNetCDFWithBandAsVariable(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesYear[intFile],
                        false,nchStrategy,true,true,0,false,aDblTimeAll,dblLat,dblLon,aDblLat,aDblLon,xShtSub,
                        cStrCoordinateDescriptionLatLonTime,cStrCoordinateFieldsLatLonTime,intPixelsPerX,intPixelsPerY,null);// as contiguous don't need to provide the shape
                }
                else if (intConfiguration == cIntTestTimeLatLonBand) {
                    xShtSub = new short[intYear][][][]; // have to create a structure with the correct size
                    gFunGen.ExtractSubsetWithBandAsDimensionTimeLatLon(xShtAll, aDblTimeAll, xShtSub, aDblTimeSub, 0, intYear);
                    lngTimeYear = gFunGen.WriteNetCDFWithBandAsDimension(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesYear[intFile],
                        false,nchStrategy,true,true,0,false,aDblTimeAll,dblLat,dblLon,aDblLat,aDblLon,aIntBand,xShtSub,intPixelsPerX,intPixelsPerY,
                        null,cStrCoordinateDescriptionTimeLatLonBand,cStrCoordinateFieldsTimeLatLonBand);// as contiguous don't need to provide the shape
                }
                else if (intConfiguration == cIntTestLatLonTimeBand) {
                    gFunGen.ExtractSubsetWithBandAsDimensionLatLonTime(xShtAll, aDblTimeAll, 
                        xShtSub, aDblTimeSub, 0, intYear,intPixelsPerX,intPixelsPerY);
                    lngTimeYear = gFunGen.WriteNetCDFWithBandAsDimension(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesYear[intFile],
                        false,nchStrategy,true,true,0,false,aDblTimeAll,dblLat,dblLon,aDblLat,aDblLon,aIntBand,xShtSub,intPixelsPerX,intPixelsPerY,
                        null,cStrCoordinateDescriptionLatLonTimeBand,cStrCoordinateFieldsLatLonTimeBand);// as contiguous don't need to provide the shape
                }
                else if (intConfiguration == cIntTestBandTimeLatLon) {
                    gFunGen.ExtractSubsetWithBandAsDimensionBandTimeLatLon(xShtAll, aDblTimeAll, 
                        xShtSub, aDblTimeSub, 0, intYear,intPixelsPerX,intPixelsPerY);
                    lngTimeMonth = gFunGen.WriteNetCDFWithBandAsDimension(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesYear[intFile],
                        false,nchStrategy,true,true,0,false,aDblTimeAll,dblLat,dblLon,aDblLat,aDblLon,aIntBand,xShtSub,intPixelsPerX,intPixelsPerY,
                        null,cStrCoordinateDescriptionBandTimeLatLon,cStrCoordinateFieldsBandTimeLatLon);// as contiguous don't need to provide the shape
                }
                else if (intConfiguration == cIntTestTimeBandLatLon) {
                    xShtSub = new short[intYear][][][]; // have to create a structure with the correct size
                    gFunGen.ExtractSubsetWithBandAsDimensionTimeBandLatLon(xShtAll, aDblTimeAll, 
                        xShtSub, aDblTimeSub, 0, intYear,intPixelsPerX,intPixelsPerY);
                    lngTimeMonth = gFunGen.WriteNetCDFWithBandAsDimension(NetcdfFileWriter.Version.netcdf4, gAStrFileNamesYear[intFile],
                        false,nchStrategy,true,true,0,false,aDblTimeAll,dblLat,dblLon,aDblLat,aDblLon,aIntBand,xShtSub,intPixelsPerX,intPixelsPerY,
                        null,cStrCoordinateDescriptionTimeBandLatLon,cStrCoordinateFieldsTimeBandLatLon);// as contiguous don't need to provide the shape
                }
                oswStats = new OutputStreamWriter(new FileOutputStream(strStats,true));
                strResult = Double.toString(dblLat) + "," + Double.toString(dblLon) + "," +
                    gFunGen.TimeYear(gADtmTime[intFile]) + "," + Integer.toString(intYear) + "," +
                    Integer.toString(intFilesYear) + "," +
                    Integer.toString(intFilesQuarter) + "," + Integer.toString(intFilesMonth) + "," +
                    Long.toString(lngTimeYear) + "," + Long.toString(lngTimeQuarter) + "," + 
                    Long.toString(lngTimeMonth);
                oswStats.write(strResult + "\r\n");
                oswStats.close();
                gLogV.println("Processed: " + strResult);
            }
        }
    }
    
    /**
     * Function to work out the offset of the file which has the specified latitude,
     * longitude and time values.
     * 
     * This code is extremely inefficient, it is better to store this in a lookup table, but
     * for testing it is irrelevant.
     * 
     * @param dblLat: The latitude value.
     * @param dblLon: The longitude value.
     * @param dtmTime: The time value.
     * 
     * @return The offset of the file which contains the specified parameter values.
     */
    public int Offset(double dblLat, double dblLon, Date dtmTime)  {
        for (int intOff = 0; intOff < gADblLon.length; intOff++) {
            if ((gADblLat[intOff] == dblLat) && (gADblLon[intOff] == dblLon) &&
            (gFunGen.DifferenceMilliseconds(dtmTime, gADtmTime[intOff]) == 0)) {
                return intOff;
            }
        }
        return -1;
    }
    
    /**
     * Function to work out the minimum start date which allows at least X records
     * to be found within the yearly aggregated file within the specified latitude, 
     * longitude range.
     * 
     * @param dblLatMin: The minimum latitude range to process.
     * @param dblLatMax: The maximum latitude range to process.
     * @param dblLonMin: The minimum longitude range to process.
     * @param dblLonMax: The maximum longitude range to process.
     * @param dblFractionX: The size of the X dimension stored within an individual file (longitude).
     * @param dblFractionY: The size of the Y dimension stored within an individual file (latitude).
     * @param intTimePoints: The number of records which must be stored within the year.
     * @param dtmDefault: The desired start date.
     * @param funGen: The class which includes the functionality for check dates.
     * 
     * @return The dtmDefault value if every file has sufficient records after it
     *         within the year.  Otherwise the minimum start date, in the same calendar year,
     *         which guarantees that every file has at least the specified number of time points.
     * @throws ParseException 
     */
    public Date CalculateStartDate(double dblLatMin,double dblLatMax,double dblLonMin, double dblLonMax,
            double dblFractionX,double dblFractionY,int intTimePoints, Date dtmDefault,
            functions funGen) throws ParseException {
        Date dtmStart;
        double dblLat,dblLon,dblStartLat,dblEndLat,dblStartLon,dblEndLon;
        int intOff,intRecs,intNew;
        
        // for lat need to go from ceil to floor as goes from top to bottom e.g., -36 covers range -35 to -36.
        dblStartLat = CalculateStart(dblLatMin,dblFractionY);
        dblEndLat = CalculateEnd(dblStartLat,dblLatMax,dblFractionY);
        // for lon need to go from floor to ceil as goes from left to right
        dblStartLon = CalculateStart(dblLonMin,dblFractionX);
        dblEndLon = CalculateEnd(dblStartLon,dblLonMax,dblFractionX);
        dtmStart = null;
        for (intOff = 0; intOff < gADblLon.length; intOff++) {
            dblLat = gADblLat[intOff];
            dblLon = gADblLon[intOff];
            if ((dblLat >= dblStartLat) && (dblLat < dblEndLat) &&
            (dblLon >= dblStartLon) && (dblLon < dblEndLon)) {
                if (funGen.SameYear(dtmDefault, gADtmTime[intOff])) {
                    if (gADtmTime[intOff].getTime() >= dtmDefault.getTime()) {
                        if (gAIntRecordsYear[intOff] >= intTimePoints) {
                            intRecs = gAIntRecordsYear[intOff]-gAIntOffsetYear[intOff];
                            if (intRecs < intTimePoints) {
                                intNew = intOff+intRecs-intTimePoints;
                                if ((dtmStart == null) || (dtmStart.getTime() > gADtmTime[intNew].getTime()))
                                    dtmStart = gADtmTime[intNew]; // find the minimum date that has sufficient time points
                            }
                        }
                        intOff += gAIntRecordsYear[intOff]-1; // skip over this year
                    }
                }
            }
        }
        if (dtmStart != null)
            return dtmStart;
        return dtmDefault;
    }
    
    // calculate end date, but only if it is in the same year
    
    /**
     * Function to calculate the minimum end date which guarantees that all
     * files have at least X records within the yearly aggregated file within the 
     * specified latitude, longitude range.
     * 
     * @param dblLatMin: The minimum latitude range to process.
     * @param dblLatMax: The maximum latitude range to process.
     * @param dblLonMin: The minimum longitude range to process.
     * @param dblLonMax: The maximum longitude range to process.
     * @param dblFractionX: The size of the X dimension stored within an individual file (longitude).
     * @param dblFractionY: The size of the Y dimension stored within an individual file (latitude).
     * @param dtmStart: The start date.
     * @param intTimePoints: The number of records which must be stored within the year.
     * 
     * @return The end date
     */
    public Date CalculateEndDate(double dblLatMin,double dblLatMax,double dblLonMin, double dblLonMax,
            double dblFractionX,double dblFractionY,Date dtmStart, int intTimePoints) {
        Date dtmEnd;
        double dblLat,dblLon,dblStartLat,dblEndLat,dblStartLon,dblEndLon;
        int intOff,intNxt,intRecs,intRec;
        
        // for lat need to go from ceil to floor as goes from top to bottom e.g., -36 covers range -35 to -36.
        dblStartLat = CalculateStart(dblLatMin,dblFractionY);
        dblEndLat = CalculateEnd(dblStartLat,dblLatMax,dblFractionY);
        // for lon need to go from floor to ceil as goes from left to right
        dblStartLon = CalculateStart(dblLonMin,dblFractionX);
        dblEndLon = CalculateEnd(dblStartLon,dblLonMax,dblFractionX);
        
        dtmEnd = null;
        for (intOff = 0; intOff < gADblLon.length; intOff++) {
            dblLat = gADblLat[intOff];
            dblLon = gADblLon[intOff];
            if ((dblLat >= dblStartLat) && (dblLat < dblEndLat) && 
            (dblLon >= dblStartLon) && (dblLon < dblEndLon)) {
                if (gFunGen.DifferenceMilliseconds(dtmStart, gADtmTime[intOff]) >= 0) {
                    // extend to the end of the record with the same lat lon
                    intRecs = gAIntRecordsYear[intOff]-gAIntOffsetYear[intOff];
                    if (intRecs >= intTimePoints) {
                        for (intNxt = intOff,intRec = 1;(intNxt < gADblLon.length-1) && (intRec <= intRecs); intNxt++,intRec++) {
                            if ((!((gADblLat[intNxt+1] == dblLat) && (gADblLon[intNxt+1] == dblLon))) || (intRec == intTimePoints)) {
                                if ((dtmEnd == null) || (gFunGen.DifferenceMilliseconds(dtmEnd,gADtmTime[intNxt]) < 0)) {
                                    dtmEnd = gADtmTime[intNxt];
                                    break;
                                }
                            }
                        }
                        // continue on till the next year
                        for (;intNxt < gADblLon.length; intNxt++) {
                            if (!((gADblLat[intNxt] == dblLat) && (gADblLon[intNxt] == dblLon))) {
                                break;
                            }
                        }
                        intOff = intNxt-1; // make sure don't process this again
                    }
                }
            }
        }
        if (dtmEnd == null) // shouldn't happen
            return dtmStart;
        return dtmEnd;
    }
    
    /**
     * Function to calculate the minimum edge of the tile given the value 
     * and the size of the tile.
     * 
     * @param dblMin: The value to translate.
     * @param dblFraction: The size of the tile (1 for one degree, 0.5 for half degree, 
     *                     0.25 for quarter degree tile)
     * 
     * @return The Minimum Edge of the Tile.
     */
    public double CalculateStart(double dblMin,double dblFraction) {
        double dblFloor;
        int intFractions;
        
        dblFloor = Math.floor(dblMin);
        intFractions = (int)((dblMin - dblFloor)/dblFraction);
        return dblFloor + intFractions*dblFraction;
    }
    
    /**
     * Function to calculate the maximum edge of the tile given the value and 
     * the size of the tile.
     * 
     * @param dblMin: The minimum value to translate.
     * @param dblMax: The maximum value to translate.
     * @param dblFraction: The size of the tile (1 for one degree, 0.5 for half degree, 
     *                     0.25 for quarter degree tile)
     * 
     * @return The Maximum edge of the tile.
     */
    public double CalculateEnd(double dblMin, double dblMax, double dblFraction) {
        double dblCeil,dblRange,dblVal,dblRes;
        int intFractions;
        
        dblRange = dblMax-dblMin;
        dblCeil = Math.ceil(dblMax);
        dblVal = dblMin+Math.max(dblFraction, dblRange);
        intFractions = (int)((dblCeil-dblVal)/dblFraction);
        dblRes = dblCeil - intFractions*dblFraction;
        return dblRes;
    }
    
    /**
     * Function to generate the queries to address to the single time point,
     * and time aggregated monthly, quarterly, and yearly files in order
     * to extract the same content from the different files.
     * 
     * @param dblLatMin: The minimum latitude range to process.
     * @param dblLatMax: The maximum latitude range to process.
     * @param dblLonMin: The minimum longitude range to process.
     * @param dblLonMax: The maximum longitude range to process.
     * @param dtmStart: The start date.
     * @param dtmEnd: The end date.
     * @param intInit: The position within the aIntFileCounts, xStrFileNames,
     *                 xIntOrigin, and xIntShape to store the values for the
     *                 query for a single time record.
     * @param aIntFileCounts: The array to store the number of files for each query.
     * @param xStrFileNames: The matrix to store the file names for each query.
     * @param xIntOrigin: The matrix to store the origin for each file in order 
     *                    to execute the query.
     * @param xIntShape: The matrix to store the shape of data to be read from
     *                    each file in order to execute the query.
     * @param intPixelsX: The number of pixels in the X dimension (longitude).
     * @param intPixelsY: The number of pixels in the Y dimension (latitude). 
     */
    public void CalculateQueries(double dblLatMin,double dblLatMax,double dblLonMin, double dblLonMax,
            Date dtmStart, Date dtmEnd,int intInit,int[] aIntFileCounts, String[][] xStrFileNames,
            int[][][] xIntOrigin, int[][][] xIntShape,int intPixelsX,int intPixelsY) {
        double dblLat,dblLon,dblFractionX,dblFractionY,dblStartLat,dblStartLon,dblEndLat,dblEndLon;
        double dblRngMinLat,dblRngMaxLat,dblRngMinLon,dblRngMaxLon;
        double dblOriginX,dblOriginY,dblEndX,dblEndY,dblShapeX,dblShapeY;
        int intOff,intNxt,intRecords,intEnd;
        int intOriginX,intOriginY,intShapeX,intShapeY;
        long lngDelay;
        
        // for lat need to go from ceil to floor as goes from top to bottom e.g., -36 covers range -35 to -36.
        dblFractionX = ((double)intPixelsX)/test.cIntPixelsOneX;
        dblFractionY = ((double)intPixelsY)/test.cIntPixelsOneY;
        // for lat need to go from ceil to floor as goes from top to bottom e.g., -36 covers range -35 to -36.
        dblStartLat = CalculateStart(dblLatMin,dblFractionY);
        dblEndLat = CalculateEnd(dblStartLat,dblLatMax,dblFractionY);
        // for lon need to go from floor to ceil as goes from left to right
        dblStartLon = CalculateStart(dblLonMin,dblFractionX);
        dblEndLon = CalculateEnd(dblStartLon,dblLonMax,dblFractionX);
        
        for (intOff = intInit; intOff < intInit+4; intOff++)
            aIntFileCounts[intOff] = 0;
        for (intOff = 0; intOff < gADblLon.length; intOff++) {
            dblLat = gADblLat[intOff];
            dblLon = gADblLon[intOff];
            intEnd = 0;
            if ((dblLat >= dblStartLat) && (dblLat < dblEndLat) && 
            (dblLon >= dblStartLon) && (dblLon < dblEndLon)) {
                // calculate the real boundaries for this record
                dblRngMaxLat = dblLat+dblFractionY;
                dblRngMinLat = dblLat;
                dblRngMinLon = dblLon;
                dblRngMaxLon = dblLon+dblFractionX;
                dblOriginX = Math.max(dblLonMin, dblRngMinLon);
                dblOriginY = Math.min(dblLatMax, dblRngMaxLat);
                dblEndX = Math.min(Math.min(dblOriginX+dblFractionX, dblRngMaxLon),dblLonMax);
                dblEndY = Math.max(Math.max(dblOriginY-dblFractionY, dblRngMinLat),dblLatMin);
                dblShapeX = dblEndX - dblOriginX;
                dblShapeY = dblOriginY - dblEndY;
                intOriginY = (int)Math.round((dblRngMaxLat-dblOriginY)*test.cIntPixelsOneY);
                intShapeY = (int)Math.round(dblShapeY*test.cIntPixelsOneY);
                intOriginX = (int)Math.round((dblOriginX-dblRngMinLon)*test.cIntPixelsOneX);
                intShapeX = (int)Math.round(dblShapeX*test.cIntPixelsOneY);
                // look forward for the first record with the same lat and lon which is equal to or after the start
                for (intNxt = intOff; intNxt < gADblLon.length; intNxt++) {
                    if (!((gADblLat[intNxt] == dblLat) && (gADblLon[intNxt] == dblLon))) 
                        break;
                    if (gFunGen.DifferenceMilliseconds(dtmStart, gADtmTime[intNxt]) >= 0) {
                        intOff = intNxt;
                        for (intRecords = 1; intNxt < gADblLon.length; intNxt++,intRecords++) {
                            lngDelay = gFunGen.DifferenceMilliseconds(dtmEnd, gADtmTime[intNxt]);
                            if (lngDelay == 0)
                                break;
                            else if ((lngDelay > 0) || (!((gADblLat[intNxt] == dblLat) && (gADblLon[intNxt] == dblLon)))) {
                                intNxt--;intRecords--; // went too far so step back
                                break;
                            }
                        }
                        intEnd = intOff+intRecords; // work out details for the different files
                        // Note that the band information is irrelavant for this call, so simply provide 0.
                        CalculateFiles(intOff,intEnd,intOriginX,intShapeX,intOriginY,intShapeY,
                            null,gAStrFileNamesSingle,aIntFileCounts,xStrFileNames,
                            xIntOrigin,xIntShape,intInit,cIntTestTimeLatLon,0,0);
                        CalculateFiles(intOff,intEnd,intOriginX,intShapeX,intOriginY,intShapeY,
                            gAIntOffsetMonth,gAStrFileNamesMonth,aIntFileCounts,xStrFileNames,
                            xIntOrigin,xIntShape,intInit+1,cIntTestTimeLatLon,0,0);
                        CalculateFiles(intOff,intEnd,intOriginX,intShapeX,intOriginY,intShapeY,
                            gAIntOffsetQuarter,gAStrFileNamesQuarter,aIntFileCounts,xStrFileNames,
                            xIntOrigin,xIntShape,intInit+2,cIntTestTimeLatLon,0,0);
                        CalculateFiles(intOff,intEnd,intOriginX,intShapeX,intOriginY,intShapeY,
                            gAIntOffsetYear,gAStrFileNamesYear,aIntFileCounts,xStrFileNames,
                            xIntOrigin,xIntShape,intInit+3,cIntTestTimeLatLon,0,0);
                        break;
                    }
                }
            }
            // extend to the end of the record with the same lat lon
            for (intNxt = Math.max(intEnd,intOff); intNxt < gADblLon.length; intNxt++) {
                if (!((gADblLat[intNxt] == dblLat) && (gADblLon[intNxt] == dblLon))) {
                    break;
                }
            }
            intOff = intNxt-1; // skip over the records again
        }
    }
    
    /**
     * Function to calculate the details for each time aggregated file in the 
     * range which was found to match the query.
     * 
     * @param intBeg: The initial position of files to process.
     * @param intEnd: The final position of files to process.
     * @param intOriginX: The origin in the X dimension (longitude) for the file.
     * @param intShapeX: The shape of data in the X dimension (longitude) to be 
     *                   read from the file.
     * @param intOriginY: The origin in the Y dimension (latitude) for the file.
     * @param intShapeY: The shape of data in the Y dimension (latitude) to be 
     *                   read from the file.
     * @param aIntFileCounts: The array to store the number of files for each query.
     * @param xStrFileNames: The matrix to store the file names for each query.
     * @param xIntOrigin: The matrix to store the origin for each file in order 
     *                    to execute the query.
     * @param xIntShape: The matrix to store the shape of data to be read from
     *                   each file in order to execute the query.
     * @param intRes: The position within the aIntFileCounts, xStrFileNames, 
     *                xIntOrigin, xIntShape, etcetera to store the the details
     *                for this file.
     * @param intConfiguration: The configuration of the data to be read from the NetCDF file.
     * 
     *                          The possible values are:
     *                              cIntTestTimeLatLon : The Band is a variable and the 
     *                                                   dimensions are time latitude longitude.
     *                              cIntTestLatLonTime : The Band is a variable and the 
     *                                                   dimensions are latitude longitude time.
     *                              cIntTestBandTimeLatLon : The Band is a dimension and the
     *                                                       dimensions are band time latitude longitude.
     *                              cIntTestTimeBandLatLon : The Band is a dimension and the
     *                                                       dimensions are time band latitude longitude.
     *                              cIntTestTimeLatLonBand : The Band is a dimension and the
     *                                                       dimensions are time latitude longitude band.
     *                              cIntTestLatLonTimeBand : The Band is a dimension and the
     *                                                       dimensions are latitude longitude time band.
     * @param intBandOrigin: The initial band offset to be read.
     * @param intBands: The number of bands to be processed.
     */
    private void CalculateFiles(int intBeg,int intEnd,int intOriginX,int intShapeX,
            int intOriginY,int intShapeY,int[] aIntFileOffset, String[] aStrFileNames, int[] aIntFileCounts, 
            String[][] xStrFileNames,int[][][] xIntOrigin,int[][][] xIntShape, int intRes,int intConfiguration,
            int intBandOrigin, int intBands) {
        int intSrc,intFile,intLast,intRec,intOff;
        // work out details for the single files
        intLast = intEnd-1;
        for (intSrc = intBeg,intFile = aIntFileCounts[intRes],intRec = 0,
        intOff = ((aIntFileOffset == null) ? 0 : aIntFileOffset[intBeg]);
        intSrc < intEnd; intSrc++) {
            if ((intSrc == intLast) || (!aStrFileNames[intSrc].equals(aStrFileNames[intSrc+1]))) {
                intRec++;
                if ((intConfiguration == cIntTestTimeLatLon) || (intConfiguration == cIntTestTimeLatLonBand)) {
                    xIntOrigin[intRes][intFile][cIntConfigurationTimeLatLonDimensionTime] = intOff;
                    xIntOrigin[intRes][intFile][cIntConfigurationTimeLatLonDimensionLat] = intOriginY;
                    xIntOrigin[intRes][intFile][cIntConfigurationTimeLatLonDimensionLon] = intOriginX;
                    xIntShape[intRes][intFile][cIntConfigurationTimeLatLonDimensionTime] = intRec;
                    xIntShape[intRes][intFile][cIntConfigurationTimeLatLonDimensionLat] = intShapeY;
                    xIntShape[intRes][intFile][cIntConfigurationTimeLatLonDimensionLon] = intShapeX;
                    if (intConfiguration == cIntTestTimeLatLonBand) {
                        xIntOrigin[intRes][intFile][cIntConfigurationTimeLatLonDimensionBand] = intBandOrigin;
                        xIntShape[intRes][intFile][cIntConfigurationTimeLatLonDimensionBand] = intBands;
                    }
                }
                else if ((intConfiguration == cIntTestLatLonTime) || (intConfiguration == cIntTestLatLonTimeBand)) {
                    xIntOrigin[intRes][intFile][cIntConfigurationLatLonTimeDimensionTime] = intOff;
                    xIntOrigin[intRes][intFile][cIntConfigurationLatLonTimeDimensionLat] = intOriginY;
                    xIntOrigin[intRes][intFile][cIntConfigurationLatLonTimeDimensionLon] = intOriginX;
                    xIntShape[intRes][intFile][cIntConfigurationLatLonTimeDimensionTime] = intRec;
                    xIntShape[intRes][intFile][cIntConfigurationLatLonTimeDimensionLat] = intShapeY;
                    xIntShape[intRes][intFile][cIntConfigurationLatLonTimeDimensionLon] = intShapeX;
                    if (intConfiguration == cIntTestLatLonTimeBand) {
                        xIntOrigin[intRes][intFile][cIntConfigurationLatLonTimeDimensionBand] = intBandOrigin;
                        xIntShape[intRes][intFile][cIntConfigurationLatLonTimeDimensionBand] = intBands;
                    }
                }
                else if ((intConfiguration == cIntTestBandTimeLatLon)) {
                    xIntOrigin[intRes][intFile][cIntConfigurationBandTimeLatLonDimensionTime] = intOff;
                    xIntOrigin[intRes][intFile][cIntConfigurationBandTimeLatLonDimensionLat] = intOriginY;
                    xIntOrigin[intRes][intFile][cIntConfigurationBandTimeLatLonDimensionLon] = intOriginX;
                    xIntShape[intRes][intFile][cIntConfigurationBandTimeLatLonDimensionTime] = intRec;
                    xIntShape[intRes][intFile][cIntConfigurationBandTimeLatLonDimensionLat] = intShapeY;
                    xIntShape[intRes][intFile][cIntConfigurationBandTimeLatLonDimensionLon] = intShapeX;
                    xIntOrigin[intRes][intFile][cIntConfigurationBandTimeLatLonDimensionBand] = intBandOrigin;
                    xIntShape[intRes][intFile][cIntConfigurationBandTimeLatLonDimensionBand] = intBands;
                }
                else if ((intConfiguration == cIntTestTimeBandLatLon)) {
                    xIntOrigin[intRes][intFile][cIntConfigurationTimeBandLatLonDimensionTime] = intOff;
                    xIntOrigin[intRes][intFile][cIntConfigurationTimeBandLatLonDimensionLat] = intOriginY;
                    xIntOrigin[intRes][intFile][cIntConfigurationTimeBandLatLonDimensionLon] = intOriginX;
                    xIntShape[intRes][intFile][cIntConfigurationTimeBandLatLonDimensionTime] = intRec;
                    xIntShape[intRes][intFile][cIntConfigurationTimeBandLatLonDimensionLat] = intShapeY;
                    xIntShape[intRes][intFile][cIntConfigurationTimeBandLatLonDimensionLon] = intShapeX;
                    xIntOrigin[intRes][intFile][cIntConfigurationTimeBandLatLonDimensionBand] = intBandOrigin;
                    xIntShape[intRes][intFile][cIntConfigurationTimeBandLatLonDimensionBand] = intBands;
                }
                xStrFileNames[intRes][intFile++] = aStrFileNames[intSrc];
                intRec = 0;// gets incremented in for loop
                if (intSrc != intLast) // if the last entry, then intOff will not be used anyway
                    intOff = ((aIntFileOffset == null) ? 0 : aIntFileOffset[intSrc+1]);
            }
            else
                intRec++;
        }
        aIntFileCounts[intRes] = intFile;
    }
    
    /**
     * Function to calculate the queries to execute over the NetCDF files which
     * have the same spatial and temporal range, but are stored in different
     * dimensional configuration.
     * 
     * @param dblLatMin: The minimum latitude range to process.
     * @param dblLatMax: The maximum latitude range to process.
     * @param dblLonMin: The minimum longitude range to process.
     * @param dblLonMax: The maximum longitude range to process.
     * @param dtmStart: The start date.
     * @param dtmEnd: The end date.
     * @param intInit: The position within the aIntFileCounts, xStrFileNames,
     *                 xIntOrigin, and xIntShape to store the values for the
     *                 query for a single time record.
     * @param aIntFileCounts: The array to store the number of files for each query.
     * @param xStrFileNames: The matrix to store the file names for each query.
     * @param xIntOrigin: The matrix to store the origin for each file in order 
     *                    to execute the query.
     * @param xIntShape: The matrix to store the shape of data to be read from
     *                    each file in order to execute the query.
     * @param intPixelsX: The number of pixels in the X dimension (longitude).
     * @param intPixelsY: The number of pixels in the Y dimension (latitude). 
     * @param intConfiguration: The configuration of the data to be read from the NetCDF file.
     * 
     *                          The possible values are:
     *                              cIntTestTimeLatLon : The Band is a variable and the 
     *                                                   dimensions are time latitude longitude.
     *                              cIntTestLatLonTime : The Band is a variable and the 
     *                                                   dimensions are latitude longitude time.
     *                              cIntTestBandTimeLatLon : The Band is a dimension and the
     *                                                       dimensions are band time latitude longitude.
     *                              cIntTestTimeBandLatLon : The Band is a dimension and the
     *                                                       dimensions are time band latitude longitude.
     *                              cIntTestTimeLatLonBand : The Band is a dimension and the
     *                                                       dimensions are time latitude longitude band.
     *                              cIntTestLatLonTimeBand : The Band is a dimension and the
     *                                                       dimensions are latitude longitude time band.
     * @param intBandOrigin: The initial band offset to be read.
     * @param intBands: The number of bands to be processed.
     */
    public void CalculateDimensionQueries(double dblLatMin,double dblLatMax,double dblLonMin, double dblLonMax,
            Date dtmStart, Date dtmEnd,int intInit,int[] aIntFileCounts, String[][] xStrFileNames,
            int[][][] xIntOrigin, int[][][] xIntShape,int intPixelsX,int intPixelsY,int intConfiguration,int intBandOrigin, int intBands) {
        double dblLat,dblLon,dblFractionX,dblFractionY,dblStartLat,dblStartLon,dblEndLat,dblEndLon;
        double dblRngMinLat,dblRngMaxLat,dblRngMinLon,dblRngMaxLon;
        double dblOriginX,dblOriginY,dblEndX,dblEndY,dblShapeX,dblShapeY;
        int intOff,intNxt,intRecords,intEnd;
        int intOriginX,intOriginY,intShapeX,intShapeY;
        long lngDelay;
        
        // for lat need to go from ceil to floor as goes from top to bottom e.g., -36 covers range -35 to -36.
        dblFractionX = ((double)intPixelsX)/test.cIntPixelsOneX;
        dblFractionY = ((double)intPixelsY)/test.cIntPixelsOneY;
        // for lat need to go from ceil to floor as goes from top to bottom e.g., -36 covers range -35 to -36.
        dblStartLat = CalculateStart(dblLatMin,dblFractionY);
        dblEndLat = CalculateEnd(dblStartLat,dblLatMax,dblFractionY);
        // for lon need to go from floor to ceil as goes from left to right
        dblStartLon = CalculateStart(dblLonMin,dblFractionX);
        dblEndLon = CalculateEnd(dblStartLon,dblLonMax,dblFractionX);
        
        aIntFileCounts[intInit] = 0;
        for (intOff = 0; intOff < gADblLon.length; intOff++) {
            dblLat = gADblLat[intOff];
            dblLon = gADblLon[intOff];
            intEnd = 0;
            if ((dblLat >= dblStartLat) && (dblLat < dblEndLat) && 
            (dblLon >= dblStartLon) && (dblLon < dblEndLon)) {
                // calculate the real boundaries for this record
                dblRngMaxLat = dblLat+dblFractionY;
                dblRngMinLat = dblLat;
                dblRngMinLon = dblLon;
                dblRngMaxLon = dblLon+dblFractionX;
                dblOriginX = Math.max(dblLonMin, dblRngMinLon);
                dblOriginY = Math.min(dblLatMax, dblRngMaxLat);
                dblEndX = Math.min(Math.min(dblOriginX+dblFractionX, dblRngMaxLon),dblLonMax);
                dblEndY = Math.max(Math.max(dblOriginY-dblFractionY, dblRngMinLat),dblLatMin);
                dblShapeX = dblEndX - dblOriginX;
                dblShapeY = dblOriginY - dblEndY;
                intOriginY = (int)Math.round((dblRngMaxLat-dblOriginY)*test.cIntPixelsOneY);
                intShapeY = (int)Math.round(dblShapeY*test.cIntPixelsOneY);
                intOriginX = (int)Math.round((dblOriginX-dblRngMinLon)*test.cIntPixelsOneX);
                intShapeX = (int)Math.round(dblShapeX*test.cIntPixelsOneY);
                // look forward for the first record with the same lat and lon which is equal to or after the start
                for (intNxt = intOff; intNxt < gADblLon.length; intNxt++) {
                    if (!((gADblLat[intNxt] == dblLat) && (gADblLon[intNxt] == dblLon))) 
                        break;
                    if (gFunGen.DifferenceMilliseconds(dtmStart, gADtmTime[intNxt]) >= 0) {
                        intOff = intNxt;
                        for (intRecords = 1; intNxt < gADblLon.length; intNxt++,intRecords++) {
                            lngDelay = gFunGen.DifferenceMilliseconds(dtmEnd, gADtmTime[intNxt]);
                            if (lngDelay == 0)
                                break;
                            else if ((lngDelay > 0) || (!((gADblLat[intNxt] == dblLat) && (gADblLon[intNxt] == dblLon)))) {
                                intNxt--;intRecords--; // went too far so step back
                                break;
                            }
                        }
                        intEnd = intOff+intRecords; // work out details for the different files
                        
                        CalculateFiles(intOff,intEnd,intOriginX,intShapeX,intOriginY,intShapeY,
                            gAIntOffsetYear,gAStrFileNamesYear,aIntFileCounts,xStrFileNames,
                            xIntOrigin,xIntShape,intInit,intConfiguration,intBandOrigin,intBands);
                        break;
                    }
                }
            }
            // extend to the end of the record with the same lat lon
            for (intNxt = Math.max(intEnd,intOff); intNxt < gADblLon.length; intNxt++) {
                if (!((gADblLat[intNxt] == dblLat) && (gADblLon[intNxt] == dblLon))) {
                    break;
                }
            }
            intOff = intNxt-1; // skip over the records again
        }
    }
}
