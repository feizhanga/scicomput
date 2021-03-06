/*
 * Drill into a NetCDF4 file containing water extent time-series: a stack of 1-band raster files over a given spatial-extent (tile).

 * USAGE: java -jar "/path2/dist/netcdf-java.jar" /g/data/u46/fxz547/wofs/extents/149_-036/py_stacked_CF.nc 1708 341 > drill_CF_nc.csv

 *
 * @author fei.zhang@ga.gov.au
 * 
 * TODO 1 filter out redundant (same-day) observations.
 * TODO 2 add the description Wet Dry 
 * TODO 3 Optimize the Drill code
 * TODO 4 make a new web processing service and deploy it. Keep the old one as it is.
 * TODO 5 new javascript-powered graphic info summary page
 */
package ga.datacube.wofs;

import java.io.IOException;
//import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
//import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.util.*;
import java.text.*;

public class ReadNetcdf {

    public static void main(String args[]) throws IOException {
        String path2file = null;
        int x_lon = 0;
        int y_lat = 0;

        try {
            path2file = args[0];
            x_lon = Integer.parseInt(args[1]);
            y_lat = Integer.parseInt(args[2]);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("USAGE: netcdffile, int_lon, int_lat ");
            System.exit(1);
        }

        ReadNetcdf thisInst = new ReadNetcdf();

        //String csv = thisInst.readnetcdf(path2file, x_lon, y_lat);   //readnetcdf(path2file,1684,298);
        String csv = thisInst.drill_netcdf_CF(path2file, x_lon, y_lat); 
        System.out.println(csv);
        //thisInst.test_epoch_datetime_zone(1318386508000L);

    }

    /*
    read a CF1.6-compliant netcdf file the whole time series at a given lat-long -grid point.
    this method a new netcdf format where the Data is uint8 (ubyte) water will be 128 as extent
    The Data(time, lat, lon) 
     */
    public String drill_netcdf_CF(String path2file, int x_lon, int y_lat) {
        
        // Open the netcdf file, get thetime series;
        // return a String of csv = "dtimestamp,results";
        String retCsv = null;

        NetcdfFile dataFile = null;

        // Open the file.
        try {

            dataFile = NetcdfFile.open(path2file, null);

            // Retrieve the variable named "time"
            Variable timeVar = dataFile.findVariable("time");
            if (timeVar == null) {
                System.out.println("Cant find the Variable time");
                return null;
            }

            int[] time_shape = timeVar.getShape();

            // Retrieve the variable named "Data"
            Variable dataVar = dataFile.findVariable("Data");

            if (dataVar == null) {
                System.out.println("Cant find the Variable Data");
                return null;
            } else {
                System.out.println("Number of Time Slices:" + time_shape[0]);
            }

            // Read all the values from the "data" variable into memory.
            int[] shape = dataVar.getShape();
            System.out.println("Number of Dimension=" + shape.length);

            for (int i = 0; i < shape.length; i++) {
                System.out.println("shape[i] =" + shape[i]);
            }

            assert (time_shape[0] == shape[2]);

            int[] origin = new int[3];
            //check 0-initial values?
            System.out.println("Origin0 = " + origin[0]);
            System.out.println("Origin1 = " + origin[1]);
            // the pixel to be drilled 
            origin[1] = y_lat;
            origin[2] = x_lon;
            origin[0] = 0;

            int[] point_shape = new int[3];
            point_shape[1] = 1;
            point_shape[2] = 1;
            point_shape[0] = time_shape[0];  // 1787; //time depth

            //Read() all the data for this Variable and return a memory resident Array.
            ArrayDouble.D1 timeArray = (ArrayDouble.D1) timeVar.read();

            ArrayByte.D3 dataArray;

            dataArray = (ArrayByte.D3) dataVar.read(origin, point_shape);

            for (int i = 0; i < point_shape[0]; i++) {
                //System.out.println(timeArray.get(i) +", "+ dataArray.get(0,0,i) );
                //System.out.println( this.convertEpochSec_Datetime(timeArray.get(i)) +", "+ dataArray.get(0,0,i) );
                retCsv = retCsv + "\n"
                        + this.convertEpochSec_Datetime(timeArray.get(i)) + ", " + dataArray.get(i, 0, 0);
            }
            // The file is closed no matter what by putting inside a try/catch block.
        } catch (java.io.IOException e) {
            e.printStackTrace();

        } catch (InvalidRangeException e) {
            e.printStackTrace();

        } finally {
            if (dataFile != null) {
                try {
                    dataFile.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }

        //System.out.println("*** SUCCESS reading the netcdf file: " + path2file);
        return retCsv;

    }

    /*
    read a netcdf file the whole time series at a given lat-long -grid point.
    this method assume old-drill netcdf format where the Dat is int8 (byte) water will be -128 (not 128)
    The Data (lat, lon, time) 
     */
    public String readnetcdf(String path2file, int x_lon, int y_lat) {
        // Open the netcdf file, get thetime series;
        // return a String of csv

        String retCsv = null;

        NetcdfFile dataFile = null;

        // Open the file.
        try {

            dataFile = NetcdfFile.open(path2file, null);

            // Retrieve the variable named "time"
            Variable timeVar = dataFile.findVariable("time");
            if (timeVar == null) {
                System.out.println("Cant find the Variable time");
                return null;
            }

            int[] time_shape = timeVar.getShape();

            // Retrieve the variable named "Data"
            Variable dataVar = dataFile.findVariable("Data");

            if (dataVar == null) {
                System.out.println("Cant find the Variable Data");
                return null;
            } else {
                System.out.println("Number of Time Slices:" + time_shape[0]);
            }

            // Read all the values from the "data" variable into memory.
            int[] shape = dataVar.getShape();
            System.out.println("Number of Dimension=" + shape.length);

            for (int i = 0; i < shape.length; i++) {
                System.out.println("shape[i] =" + shape[i]);
            }

            assert (time_shape[0] == shape[2]);

            int[] origin = new int[3];
            //check 0-initial values?
            System.out.println("Origin0 = " + origin[0]);
            System.out.println("Origin1 = " + origin[1]);
            origin[0] = y_lat;
            origin[1] = x_lon;
            origin[2] = 0;

            int[] point_shape = new int[3];
            point_shape[0] = 1;
            point_shape[1] = 1;
            point_shape[2] = time_shape[0];  // 1787; //time depth

            ArrayDouble.D1 timeArray = (ArrayDouble.D1) timeVar.read();

            ArrayByte.D3 dataArray;

            dataArray = (ArrayByte.D3) dataVar.read(origin, point_shape);

            for (int i = 0; i < point_shape[2]; i++) {
                //System.out.println(timeArray.get(i) +", "+ dataArray.get(0,0,i) );
                //System.out.println( this.convertEpochSec_Datetime(timeArray.get(i)) +", "+ dataArray.get(0,0,i) );
                retCsv = retCsv + "\n"
                        + this.convertEpochSec_Datetime(timeArray.get(i)) + ", " + dataArray.get(0, 0, i);
            }
            // The file is closed no matter what by putting inside a try/catch block.
        } catch (java.io.IOException e) {
            e.printStackTrace();

        } catch (InvalidRangeException e) {
            e.printStackTrace();

        } finally {
            if (dataFile != null) {
                try {
                    dataFile.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }

        //System.out.println("*** SUCCESS reading the netcdf file: " + path2file);
        return retCsv;

    }

    public String convertEpochSec_Datetime(double epochsec) {
        long msec = (long) (1000 * epochsec);

        //String date = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date (msec));
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(msec));

        return date;
    }

    /*
    Read all things about this netCDF file, mimic ncdump utils.
     */
    private static void readnetcdf_all(String path2file) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        String filename = "/Softdata/data/wofs_extents_149_-036_sub/py_stacked.nc"; //"simple_xy.nc";
        final int NX = 4000;
        final int NY = 4000;
        // This is the array we will read.
        int[][] dataIn = new int[NX][NY];

        // Open the file. The ReadOnly parameter tells netCDF we want
        // read-only access to the file.
        NetcdfFile dataFile = null;

        // Open the file.
        try {

            dataFile = NetcdfFile.open(filename, null);

            // Retrieve the variable named "data"
            Variable dataVar = dataFile.findVariable("Data");

            if (dataVar == null) {
                System.out.println("Cant find Variable data");
                return;
            }

            // Read all the values from the "data" variable into memory.
            int[] shape = dataVar.getShape();
            System.out.println(shape.length);

            for (int i = 0; i < shape.length; i++) {
                System.out.println("shape[i] =" + shape[i]);
            }
            // Check the values.
            assert shape[0] == NX;
            assert shape[1] == NY;

            int[] shape2 = new int[3];
            shape2[0] = 4000;
            shape2[1] = 4000;
            shape2[2] = 2;

            int[] origin = new int[3];
            System.out.println("Origin0 =" + origin[0]);
            System.out.println("Origin1 = " + origin[1]);

            ArrayByte.D3 dataArray;

            dataArray = (ArrayByte.D3) dataVar.read(origin, shape2);

            for (int j = 0; j < shape2[0]; j++) {
                for (int i = 0; i < shape2[1]; i++) {
                    for (int t = 0; t < shape2[2]; t++) {
                        // dataIn[j][i] = dataArray.get(j, i, 0);
                        if (dataArray.get(j, i, t) < 0) {
                            System.out.println("Value= " + dataArray.get(j, i, t));
                        }
                    }
                }
            }

            // The file is closed no matter what by putting inside a try/catch block.
        } catch (java.io.IOException e) {
            e.printStackTrace();

        } catch (InvalidRangeException e) {
            e.printStackTrace();

        } finally {
            if (dataFile != null) {
                try {
                    dataFile.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }

        System.out.println("*** SUCCESS reading the netcdf file: " + filename);

    }

    public void test_epoch_datetime_zone(long millisec) {
        long ms = 1318386508000L; //millisec ;
        java.util.Date date = new java.util.Date(ms);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        String formatted = format.format(date);
        System.out.println(formatted);

        format.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
        formatted = format.format(date);
        System.out.println(formatted);
    }

}
