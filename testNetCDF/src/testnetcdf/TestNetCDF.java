/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package testnetcdf;

/**
 *
 */
public class TestNetCDF {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String strCommand,strArgument,strVal;
        String[] aStrParameters;
        int intArgument,intCommand;
        log logV;
    
        strCommand = "";
        logV = null; 
        try {
            String strDirectoryCompressed,strDirectoryDecompressed;
            String strDirectoryLog,strChunkingStrategy,strCompressionRates;
            double dblLatMin,dblLatMax,dblLonMin,dblLonMax;
            int intPixelsX,intPixelsY,intBands,intInitialYear,intYears,intMaxTilesPerYear;
            int intMaxGroupSize,intChunkSize,intChanceMaximum,intChunkingDeflateLevel;
            int intThreads,intThread,intBytesPerBlock;
            double dblChancePartialTile;
            long lngSeed;
            boolean blnUnlimitedTime,blnChunkingShuffle; 

            logV = new log();
            strDirectoryDecompressed = "/core/Synthetic/"; 
            strDirectoryCompressed = "/compressed/Synthetic/";
            dblLatMin = 65;dblLatMax = 69;//iLatMin = 65;iLatMax = 90;  // full range of Australia
            dblLonMin = 86;dblLonMax = 90;//iLonMin = 86;iLonMax = 116; // full range of Australia
            intPixelsX = 4000;intPixelsY = 4000;intBands = 6;
            intInitialYear = 2006;intYears = 5;intMaxTilesPerYear = 90;intMaxGroupSize = 10;
            intChanceMaximum = 100000;
            dblChancePartialTile = 0.25;
            strCompressionRates = "1,1.5,2,2.5,3,5,10";
            blnUnlimitedTime = false;
            blnChunkingShuffle = false;
            strChunkingStrategy = gen.cStrChunkingStrategyStandard; // possible values, GRIB, FROMATTRIBUTE, and STANDARD; or unchanged
            intChunkingDeflateLevel = 5;
            lngSeed = System.currentTimeMillis(); 
            intChunkSize = 10240; //10 KB
            strDirectoryLog = "";
            intThreads = 1;intThread = 0;
            intBytesPerBlock = 512; // 512 on solaris 1024 on centos
            
            for (intArgument = 0; intArgument < args.length; intArgument++) {
                strArgument = args[intArgument].toUpperCase();
                aStrParameters = strArgument.split(":");
                if (aStrParameters.length > 1) {
                    strVal = args[intArgument].substring(aStrParameters[0].length()+1);
                    if (strArgument.startsWith("-DIR_COMPRESSED:")) 
                        strDirectoryCompressed = strVal;
                    else if (strArgument.startsWith("-DIR_DECOMPRESSED:")) 
                        strDirectoryDecompressed = strVal;
                    else if (strArgument.startsWith("-LAT_MIN:")) 
                        dblLatMin = Double.parseDouble(strVal);
                    else if (strArgument.startsWith("-LAT_MAX:")) 
                        dblLatMax = Double.parseDouble(strVal);
                    else if (strArgument.startsWith("-LON_MIN:")) 
                        dblLonMin = Double.parseDouble(strVal);
                    else if (strArgument.startsWith("-LON_MAX:")) 
                        dblLonMax = Double.parseDouble(strVal);
                    else if (strArgument.startsWith("-PIXELS_X:")) 
                        intPixelsX = Integer.parseInt(strVal);
                    else if (strArgument.startsWith("-PIXELS_Y:")) 
                        intPixelsY = Integer.parseInt(strVal);
                    else if (strArgument.startsWith("-BANDS:")) 
                        intBands = Integer.parseInt(strVal);
                    else if (strArgument.startsWith("-INITIAL_YEAR:")) 
                        intInitialYear = Integer.parseInt(strVal);
                    else if (strArgument.startsWith("-YEARS:")) 
                        intYears = Integer.parseInt(strVal);
                    else if (strArgument.startsWith("-MAX_TILES_PER_YEAR:")) 
                        intMaxTilesPerYear = Integer.parseInt(strVal);
                    else if (strArgument.startsWith("-MAX_GROUP_SIZE:")) 
                        intMaxGroupSize = Integer.parseInt(strVal);
                    else if (strArgument.startsWith("-CHANCE_MAXIMUM:")) 
                        intChanceMaximum = Integer.parseInt(strVal);
                    else if (strArgument.startsWith("-CHANCE_PARTIAL_TILE:")) 
                        dblChancePartialTile = Double.parseDouble(strVal);
                    else if (strArgument.startsWith("-COMPRESSION_RATES:")) 
                        strCompressionRates = strVal;
                    else if (strArgument.startsWith("-UNLIMITED_TIME:")) 
                        blnUnlimitedTime = strVal.toUpperCase().equals("TRUE");
                    else if (strArgument.startsWith("-SEED:")) 
                        lngSeed = Long.parseLong(strVal);
                    else if (strArgument.startsWith("-LOG:")) 
                        strDirectoryLog = strVal;
                    else if (strArgument.startsWith("-THREADS:")) 
                        intThreads = Integer.parseInt(strVal);
                    else if (strArgument.startsWith("-THREAD:")) 
                        intThread = Integer.parseInt(strVal);
                    else if (strArgument.startsWith("-CHUNKING_STRATEGY:")) 
                        strChunkingStrategy = strVal.toUpperCase();
                    else if (strArgument.startsWith("-CHUNKING_DEFLATE_LEVEL:")) 
                        intChunkingDeflateLevel = Integer.parseInt(strVal);
                    else if (strArgument.startsWith("-CHUNKING_SHUFFLE:")) 
                        blnChunkingShuffle = strVal.toUpperCase().equals("TRUE");                    
                    else if (strArgument.startsWith("-CHUNK_SIZE:")) 
                        intChunkSize = Integer.parseInt(strVal);
                }
            }
            
            for (intCommand = 0; intCommand < args.length; intCommand++) {
                strCommand = args[intCommand].toUpperCase();
                if (strCommand.equals("-GENERATE_SYNTHETIC")) {
                    gen syn;
                    String sFormat;

                    sFormat = gen.cStrFormatNetCDF;
                    intBytesPerBlock = 1024; // 512 on solaris 1024 on centos
                    for (intArgument = 0; intArgument < args.length; intArgument++) {
                        strArgument = args[intArgument].toUpperCase();
                        aStrParameters = strArgument.split(":");
                        if (aStrParameters.length > 1) {
                            strVal = args[intArgument].substring(aStrParameters[0].length()+1);
                            if (strArgument.startsWith("-FORMAT:")) 
                                sFormat = strVal.toUpperCase();
                            else if (strArgument.startsWith("-BYTES_PER_BLOCK:")) 
                                intBytesPerBlock = Integer.parseInt(strVal);     
                       }
                    }
                    if (strDirectoryLog.length() == 0)
                        strDirectoryLog = strDirectoryDecompressed;
                    logV = new log(strDirectoryLog + "/create_" + logV.TimeOut(logV.TimeNow()) + ".log");
                    syn = new gen(strDirectoryCompressed,strDirectoryDecompressed,
                        dblLatMin,dblLatMax,dblLonMin,dblLonMax,intPixelsX,intPixelsY,intBands,
                        intInitialYear,intYears,intMaxTilesPerYear,intMaxGroupSize,intChanceMaximum,
                        dblChancePartialTile,strCompressionRates,blnUnlimitedTime,
                        lngSeed,strChunkingStrategy,intChunkingDeflateLevel,blnChunkingShuffle,logV);
                    syn.GenerateNetCDF(sFormat, intChunkSize, intThreads, intThread, intBytesPerBlock);
                    break;
                }
                else if (strCommand.equals("-READ_SYNTHETIC")) {
                    gen syn;
                    String sFormat,sDirectoryStats,sDirectoryRAM;
                    int iTests,iPixelsPerTestX,iPixelsPerTestY;
                    int iFlushBufferSize,iMaxBufferSize;
                    long lFlushSize,lSeedRead;
                    boolean bCopyToMemory;

                    sFormat = gen.cStrFormatNetCDF;
                    sDirectoryStats = "/core/Stats/"; 
                    sDirectoryRAM = "/volatile/";
                    lSeedRead = System.currentTimeMillis(); 
                    iTests = 300;
                    iPixelsPerTestX = 4000;
                    iPixelsPerTestY = 4000;
                    iFlushBufferSize = 134217728; // 128MB
                    lFlushSize = 134217728;
                    intBytesPerBlock = 512; // 512 on solaris 1024 on centos
                    iMaxBufferSize = 32000000; // 4000x4000 pixels x 2 bytes per band
                    bCopyToMemory = false;
                    for (intArgument = 0; intArgument < args.length; intArgument++) {
                        strArgument = args[intArgument].toUpperCase();
                        aStrParameters = strArgument.split(":");
                        if (aStrParameters.length > 1) {
                            strVal = args[intArgument].substring(aStrParameters[0].length()+1);
                            if (strArgument.startsWith("-FORMAT:")) 
                                sFormat = strVal.toUpperCase();
                            else if (strArgument.startsWith("-DIR_STATS:")) 
                                sDirectoryStats = strVal;
                            else if (strArgument.startsWith("-DIR_RAM:")) 
                                sDirectoryRAM = strVal;
                            else if (strArgument.startsWith("-TESTS:")) 
                                iTests = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-PIXELS_PER_TEST_X:")) 
                                iPixelsPerTestX = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-PIXELS_PER_TEST_Y:")) 
                                iPixelsPerTestY = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-SEED_READ:")) 
                                lSeedRead = Long.parseLong(strVal);
                            else if (strArgument.startsWith("-FLUSH_BUFFER_SIZE:")) 
                                iFlushBufferSize = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-FLUSH_SIZE:")) 
                                lFlushSize = Long.parseLong(strVal);
                            else if (strArgument.startsWith("-BYTES_PER_BLOCK:")) 
                                intBytesPerBlock = Integer.parseInt(strVal);     
                            else if (strArgument.startsWith("-MAX_BUFFER_SIZE:")) 
                                iMaxBufferSize = Integer.parseInt(strVal);     
                            else if (strArgument.startsWith("-COPY_TO_MEMORY:")) 
                                bCopyToMemory = strVal.toUpperCase().equals("TRUE");          
                       }
                    }
                    if (strDirectoryLog.length() == 0)
                        strDirectoryLog = strDirectoryDecompressed;
                    logV = new log(strDirectoryLog + "/read_" + logV.TimeOut(logV.TimeNow()) + ".log");
                    syn = new gen(strDirectoryCompressed,strDirectoryDecompressed,
                        dblLatMin,dblLatMax,dblLonMin,dblLonMax,intPixelsX,intPixelsY,intBands,
                        intInitialYear,intYears,intMaxTilesPerYear,intMaxGroupSize,intChanceMaximum,
                        dblChancePartialTile,strCompressionRates,blnUnlimitedTime,
                        lngSeed,strChunkingStrategy,intChunkingDeflateLevel,blnChunkingShuffle,logV);
                    syn.ReadMultiple(sDirectoryStats, sDirectoryRAM, sFormat, iTests, iPixelsPerTestX, 
                        iPixelsPerTestY, lSeedRead, intThreads, intThread, intBytesPerBlock, 
                        iFlushBufferSize, lFlushSize,iMaxBufferSize,bCopyToMemory);
                    break;
                }
                else if (strCommand.equals("-CALCULATE_ENVELOPE")) {
                    conv cnv1;
                    String strDirectoryIn,strDirectoryStats;
                    String strTimeStart,strTimeEnd;
                    
                    
                    strDirectoryIn = "/compressed/QUT/NetCDF3/";
                    strDirectoryStats = "/core/QUT/Stats/";
                    dblLatMin = Double.MIN_VALUE;dblLatMax = Double.MAX_VALUE;
                    dblLonMin = Double.MIN_VALUE;dblLonMax = Double.MAX_VALUE;
                    strTimeStart = "1980-01-01_00-00-00.000";
                    strTimeEnd = "2050-01-01_00-00-00.000";
                    for (intArgument = 0; intArgument < args.length; intArgument++) {
                        strArgument = args[intArgument].toUpperCase();
                        aStrParameters = strArgument.split(":");
                        if (aStrParameters.length > 1) {
                            strVal = args[intArgument].substring(aStrParameters[0].length()+1);
                            if (strArgument.startsWith("-DIR_IN:")) 
                                strDirectoryIn = strVal;
                            else if (strArgument.startsWith("-DIR_STATS:")) 
                                strDirectoryStats = strVal;
                            else if (strArgument.startsWith("-LAT_MIN:")) 
                                dblLatMin = Double.parseDouble(strVal);
                            else if (strArgument.startsWith("-LAT_MAX:")) 
                                dblLatMax = Double.parseDouble(strVal);
                            else if (strArgument.startsWith("-LON_MIN:")) 
                                dblLonMin = Double.parseDouble(strVal);
                            else if (strArgument.startsWith("-LON_MAX:")) 
                                dblLonMax = Double.parseDouble(strVal);
                            else if (strArgument.startsWith("-TIME_START:")) 
                                strTimeStart = strVal;
                            else if (strArgument.startsWith("-TIME_END:")) 
                                strTimeEnd = strVal;
                        }
                    }
                    if (strDirectoryLog.length() == 0)
                        strDirectoryLog = strDirectoryStats;
                    logV = new log(strDirectoryLog + "/calculate_envelope_" + logV.TimeOut(logV.TimeNow()) + ".log");
                    cnv1 = new conv(strDirectoryIn,dblLatMin,dblLatMax,dblLonMin,dblLonMax,strTimeStart,strTimeEnd,logV);
                    cnv1.CalculateRange(strDirectoryStats, intPixelsX, intPixelsY, intBands, intThread, intThreads);
                    break;
                }
                else if (strCommand.equals("-CONVERT")) {
                    conv cnv1;
                    String strDirectoryIn,strDirectoryOut;
                    String strTimeStart,strTimeEnd;
                    
                    strDirectoryIn = "/compressed/QUT/NetCDF3/";
                    strDirectoryOut = "/core/Converted/";
                    dblLonMin = 147;dblLonMax = 149;
                    dblLatMin = -38;dblLatMax = -36;
                    strTimeStart = "2004-01-01_00-00-00.000";
                    strTimeEnd = "2008-01-01_00-00-00.000";
                    for (intArgument = 0; intArgument < args.length; intArgument++) {
                        strArgument = args[intArgument].toUpperCase();
                        aStrParameters = strArgument.split(":");
                        if (aStrParameters.length > 1) {
                            strVal = args[intArgument].substring(aStrParameters[0].length()+1);
                            if (strArgument.startsWith("-DIR_IN:")) 
                                strDirectoryIn = strVal;
                            else if (strArgument.startsWith("-DIR_OUT:")) 
                                strDirectoryOut = strVal;
                            else if (strArgument.startsWith("-LAT_MIN:")) 
                                dblLatMin = Double.parseDouble(strVal);
                            else if (strArgument.startsWith("-LAT_MAX:")) 
                                dblLatMax = Double.parseDouble(strVal);
                            else if (strArgument.startsWith("-LON_MIN:")) 
                                dblLonMin = Double.parseDouble(strVal);
                            else if (strArgument.startsWith("-LON_MAX:")) 
                                dblLonMax = Double.parseDouble(strVal);
                            else if (strArgument.startsWith("-TIME_START:")) 
                                strTimeStart = strVal;
                            else if (strArgument.startsWith("-TIME_END:")) 
                                strTimeEnd = strVal;
                        }
                    }
                    if (strDirectoryLog.length() == 0)
                        strDirectoryLog = strDirectoryOut;
                    logV = new log(strDirectoryLog + "/convert_" + logV.TimeOut(logV.TimeNow()) + ".log");
                    cnv1 = new conv(strDirectoryIn,dblLatMin,dblLatMax,dblLonMin,dblLonMax,strTimeStart,strTimeEnd,logV);
                    cnv1.Convert(strDirectoryOut, intPixelsX, intPixelsY, intBands, intThread, intThreads);
                    break;
                }
                else if (strCommand.equals("-AGGREGATE")) {
                    aggregate agr1;
                    String strDirectoryIn,strDirectoryOut;
                    boolean blnOneDegree,blnExportAll;
                    int intConfiguration;
                    
                    strDirectoryIn = "/core/Single/Qtr/";
                    strDirectoryOut = "/core/Aggregated/Qtr/";
                    blnOneDegree = false;
                    blnExportAll = true;
                    intConfiguration = testDim.cIntTestTimeLatLon; // time, lat, lon
                    for (intArgument = 0; intArgument < args.length; intArgument++) {
                        strArgument = args[intArgument].toUpperCase();
                        aStrParameters = strArgument.split(":");
                        if (aStrParameters.length > 1) {
                            strVal = args[intArgument].substring(aStrParameters[0].length()+1);
                            if (strArgument.startsWith("-DIR_IN:")) 
                                strDirectoryIn = strVal;
                            else if (strArgument.startsWith("-DIR_OUT:")) 
                                strDirectoryOut = strVal;
                            else if (strArgument.startsWith("-ONE_DEGREE:")) 
                                blnOneDegree = strVal.toUpperCase().equals("TRUE");
                            else if (strArgument.startsWith("-EXPORT_ALL:")) 
                                blnExportAll = strVal.toUpperCase().equals("TRUE");
                            else if (strArgument.startsWith("-CONFIGURATION:")) 
                                intConfiguration = Integer.parseInt(strVal);     
                        }
                    }
                    if (strDirectoryLog.length() == 0)
                        strDirectoryLog = strDirectoryOut;
                    logV = new log(strDirectoryLog + "/aggregate_" + logV.TimeOut(logV.TimeNow()) + ".log");
                    agr1 = new aggregate(strDirectoryIn,strDirectoryOut,blnOneDegree,logV);
                    agr1.Aggegate(strDirectoryOut, intPixelsX, intPixelsY, intBands, 
                        intThread, intThreads,blnExportAll,intConfiguration);
                    break;
                }
                else if (strCommand.equals("-PERFORMANCE_TESTS")) {
                    test tst1;
                    String strDirectorySingle,strDirectoryAggregated,strDirectoryOut;
                    int intFlushBufferSize,intBandStart,intBandEnd,intRecordsPerTest,intTests;
                    long lngFlushSize;
                    
                    strDirectorySingle = "/core/Single/";
                    strDirectoryAggregated = "/core/Aggregated/";
                    strDirectoryOut = "/core/Performance/";
                    intFlushBufferSize = 134217728; // 128MB  
                    lngFlushSize = 134217728;
                    intRecordsPerTest = 20; // initially test only for what should occur within a year, though for other tests can do any band range
                    intTests = 300; 
                    intPixelsX = 1;
                    intPixelsY = 1;
                    intBandStart = 1;
                    intBandEnd = 1; // initial test only a single band, though for other tests can do any band range
                    lngSeed = System.currentTimeMillis();
                    for (intArgument = 0; intArgument < args.length; intArgument++) {
                        strArgument = args[intArgument].toUpperCase();
                        aStrParameters = strArgument.split(":");
                        if (aStrParameters.length > 1) {
                            strVal = args[intArgument].substring(aStrParameters[0].length()+1);
                            if (strArgument.startsWith("-DIR_SINGLE:")) 
                                strDirectorySingle = strVal;
                            else if (strArgument.startsWith("-DIR_AGGREGATED:")) 
                                strDirectoryAggregated = strVal;
                            else if (strArgument.startsWith("-DIR_OUT:")) 
                                strDirectoryOut = strVal;
                            else if (strArgument.startsWith("-PIXELS_X:")) 
                                intPixelsX = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-PIXELS_Y:")) 
                                intPixelsY = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-BAND_START:")) 
                                intBandStart = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-BAND_END:")) 
                                intBandEnd = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-RECORDS_PER_TEST:")) 
                                intRecordsPerTest = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-TESTS:")) 
                                intTests = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-FLUSH_BUFFER_SIZE:")) 
                                intFlushBufferSize = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-FLUSH_SIZE:")) 
                                lngFlushSize = Long.parseLong(strVal);
                            else if (strArgument.startsWith("-SEED:")) 
                                lngSeed = Long.parseLong(strVal);
                        }
                    }
                    if (strDirectoryLog.length() == 0)
                        strDirectoryLog = strDirectoryOut;
                    logV = new log(strDirectoryLog + "/performance_" + logV.TimeOut(logV.TimeNow()) + ".log");
                    tst1 = new test(strDirectorySingle,strDirectoryAggregated,logV);
                    tst1.execute(strDirectoryOut, intPixelsX, intPixelsY, intBandStart, intBandEnd, 
                        intRecordsPerTest, intTests, lngSeed, intFlushBufferSize, lngFlushSize);
                    break;
                }
                else if (strCommand.equals("-DIMENSION_TESTS")) {
                    testDim tsd1;
                    String strDirectorySingle,strDirectoryTimeLatLon,strDirectoryTimeLatLonBand;
                    String strDirectoryLatLonTime,strDirectoryLatLonTimeBand;
                    String strDirectoryBandTimeLatLon,strDirectoryTimeBandLatLon;
                    String strDirectoryOut;
                    int intFlushBufferSize,intBandStart,intBandEnd,intRecordsPerTest,intTests;
                    long lngFlushSize;
                    
                    strDirectorySingle = "/core/Single/";
                    strDirectoryTimeLatLon = "/core/Aggregated/";
                    strDirectoryTimeLatLonBand = "/core/TimeLatLonBand/";
                    strDirectoryLatLonTime = "/core/LatLonTime/";
                    strDirectoryLatLonTimeBand = "/core/LatLonTimeBand/";
                    strDirectoryBandTimeLatLon = "/core/BandTimeLatLon/";
                    strDirectoryTimeBandLatLon = "/core/TimeBandLatLon/";
                    
                    
                    strDirectoryOut = "/core/Dimension/";
                    intFlushBufferSize = 134217728; // 128MB  
                    lngFlushSize = 134217728;
                    intRecordsPerTest = 20; // initially test only for what should occur within a year, though for other tests can do any band range
                    intTests = 300; 
                    intPixelsX = 1;
                    intPixelsY = 1;
                    intBandStart = 1;
                    intBandEnd = 1; // initial test only a single band, though for other tests can do any band range
                    lngSeed = System.currentTimeMillis();
                    for (intArgument = 0; intArgument < args.length; intArgument++) {
                        strArgument = args[intArgument].toUpperCase();
                        aStrParameters = strArgument.split(":");
                        if (aStrParameters.length > 1) {
                            strVal = args[intArgument].substring(aStrParameters[0].length()+1);
                            if (strArgument.startsWith("-DIR_SINGLE:")) 
                                strDirectorySingle = strVal;
                            else if (strArgument.startsWith("-DIR_TIME_LAT_LON:")) 
                                strDirectoryTimeLatLon = strVal;
                            else if (strArgument.startsWith("-DIR_TIME_LAT_LON_BAND:")) 
                                strDirectoryTimeLatLonBand = strVal;
                            else if (strArgument.startsWith("-DIR_LAT_LON_TIME:")) 
                                strDirectoryLatLonTime = strVal;
                            else if (strArgument.startsWith("-DIR_LAT_LON_TIME_BAND:")) 
                                strDirectoryLatLonTimeBand = strVal;
                            else if (strArgument.startsWith("-DIR_BAND_TIME_LAT_LON:")) 
                                strDirectoryBandTimeLatLon = strVal;
                            else if (strArgument.startsWith("-DIR_TIME_BAND_LAT_LON:")) 
                                strDirectoryTimeBandLatLon = strVal;
                            else if (strArgument.startsWith("-DIR_OUT:")) 
                                strDirectoryOut = strVal;
                            else if (strArgument.startsWith("-PIXELS_X:")) 
                                intPixelsX = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-PIXELS_Y:")) 
                                intPixelsY = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-BAND_START:")) 
                                intBandStart = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-BAND_END:")) 
                                intBandEnd = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-RECORDS_PER_TEST:")) 
                                intRecordsPerTest = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-TESTS:")) 
                                intTests = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-FLUSH_BUFFER_SIZE:")) 
                                intFlushBufferSize = Integer.parseInt(strVal);
                            else if (strArgument.startsWith("-FLUSH_SIZE:")) 
                                lngFlushSize = Long.parseLong(strVal);
                            else if (strArgument.startsWith("-SEED:")) 
                                lngSeed = Long.parseLong(strVal);
                        }
                    }
                    if (strDirectoryLog.length() == 0)
                        strDirectoryLog = strDirectoryOut;
                    logV = new log(strDirectoryLog + "/dim_tests_" + logV.TimeOut(logV.TimeNow()) + ".log");
                    tsd1 = new testDim(strDirectorySingle,strDirectoryTimeLatLon,strDirectoryTimeLatLonBand,
                        strDirectoryLatLonTime,strDirectoryLatLonTimeBand,
                        strDirectoryBandTimeLatLon, strDirectoryTimeBandLatLon,logV);
                    tsd1.execute(strDirectoryOut, intPixelsX, intPixelsY, intBandStart, intBandEnd, 
                        intRecordsPerTest, intTests, lngSeed, intFlushBufferSize, lngFlushSize);
                    break;
                }
            }
            logV.close();
        }
        catch (Exception excError) {
            logV.println("An Error occurred.\r\n" + excError.getMessage() +
                    "\r\n\r\n" + logV.StackTrace(excError.getStackTrace()));
            System.exit(1);
        }
    }
}
