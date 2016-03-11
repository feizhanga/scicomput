/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ga.wofs.wps;

import java.io.IOException;
import java.text.ParseException;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geoserver.wps.gs.GeoServerProcess;

/**
 *
 * @author fei.zhang@ga.gov.au
 */


@DescribeProcess(title="wofs_pixel_drill", description="WOfS Pixel Drill wps function to return time series of Wet/Dry observatoin for pixels of interest")
public class PixelDriller implements GeoServerProcess {
    
   static final String PATH2_EXTENTS_NETCDF="/g/data/u46/wofs/extentsnc"; //   cellid/LS_WATER_cellid.nc 
   
   @DescribeResult(name="result", description="Output as PlainText JSON/CSV")
   //public String execute(@DescribeParameter(name="Directory", description="The source directory for the WATER product.") String  strDirectory,
   public String execute( @DescribeParameter(name="Latitude", description="The Latitude value of the Pixel") double dblLat,
           @DescribeParameter(name="Longitude", description="The Longitude value of the Pixel") double dblLon,
           @DescribeParameter(name="neighbourp", description="The Neighbours of Pixel") int intNB) throws IOException, ParseException {
       
       // validation for intNB how many layers of neighbour pixels. 0=No neighbour, 1=9pixels,2=25pixels 3=49pixels. Generally (2*intNB+1)^2
       if( intNB > 3){
           System.out.println("intNB can be 0,1,2,3");
       }
       
       // Convert input parameters dbLat and dbLon into cell_id and pixel(y,x)
        
       
       // Retrieve the time series for each pixel  and optinally filter/clean them to get only Dry-Wet Observations
       
       // make up a json formatted dataset :{pixel_latlon -> CSV[date, obs]}
       // if only one pixel, then it is only one csv. Generally (2*intNB+1)^2
       
        return "myjson"; //.toString();
   }
}
