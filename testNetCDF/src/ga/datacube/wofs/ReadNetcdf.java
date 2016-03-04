/*
   Read a NetCDF4 file containing water extente time-series, a stack of 1 band raster 
   over a given spatial (tile).

   This is an example which reads a NetCDF4 file written by simple_xy_wr.java. 

   Full documentation of the netCDF Java API can be found at:
   http://www.unidata.ucar.edu/software/netcdf-java/
*/

/**
 *
 * @author fei.zhang@ga.gov.au
 */


package ga.datacube.wofs;

import java.io.IOException;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayByte;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;


public class ReadNetcdf {
 

  public static void main(String args[]) throws IOException {

    final int NX = 4000;
    final int NY = 4000;
    // This is the array we will read.
    int[][] dataIn = new int[NX][NY];

    // Open the file. The ReadOnly parameter tells netCDF we want
    // read-only access to the file.
    NetcdfFile dataFile = null;
    String filename = "/g/data/u46/fxz547/wofs/extents/149_-036/py_stacked.nc"; //"simple_xy.nc";
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
      for (int i=0; i<shape.length; i++){
          System.out.println("shape[i] =" + shape[i] );
      }
      
      int[] shape2= new int[3];
      shape2[0]=4000;
      shape2[1]=4000;
      shape2[2]=20;
              
            
        // Check the values.
      assert shape[0] == NX;
      assert shape[1] == NY;
      
      int[] origin = new int[3];
      System.out.println("Origin0 =" + origin[0]);
      System.out.println("Origin1= " + origin[1]);

      ArrayByte.D3 dataArray;

      dataArray = (ArrayByte.D3) dataVar.read(origin, shape2);



      for (int j = 0; j < shape2[0]; j++) {
        for (int i = 0; i < shape2[1]; i++) {
            for (int t=0; t< shape2[2]; t++){
          // dataIn[j][i] = dataArray.get(j, i, 0);
          if (dataArray.get(j, i, t) <0) {
             System.out.println("Value= " + dataArray.get(j, i, t) );
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
      if (dataFile != null)
        try {
          dataFile.close();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
    }

    System.out.println("*** SUCCESS reading the netcdf file: " + filename);

  }

}