/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.

http://localhost:8080/geoserver/ows?service=wps&version=1.0.0&request=execute&identifier=gs:PixelDrillWPS&RawDataOutput=result&dataInputs=Latitude=-36.2013;Longitude=149.35303;neighbourp=0
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
public class PixelDrillWPS implements GeoServerProcess {
    
   static final String PATH2_EXTENTS_NETCDF="/g/data/u46/wofs/extentsnc"; //   cellid/LS_WATER_cellid.nc 
   
   @DescribeResult(name="result", description="Output as PlainText JSON/CSV")
   //public String execute(@DescribeParameter(name="Directory", description="The source directory for the WATER product.") String  strDirectory,
   public String execute( @DescribeParameter(name="Latitude", description="The Latitude value of the Pixel") double dblLat,
           @DescribeParameter(name="Longitude", description="The Longitude value of the Pixel") double dblLon,
           @DescribeParameter(name="neighbourp", description="The Neighbours of Pixel") int intNB) throws IOException, ParseException {
       
       // validation for intNB how many layers of neighbour pixels. 0=No neighbour, 1=9pixels,2=25pixels 3=49pixels. Generally (2*intNB+1)^2
       if( intNB > 3){
           System.out.println("intNB must be in [0,1,2,3]");
       }
       
       // Convert input parameters dbLat and dbLon into cell_id and pixel(y,x)
        
       
       // Retrieve the time series for each pixel  and optinally filter/clean them to get only Dry-Wet Observations
       
       // make up a json formatted dataset :{pixel_latlon -> CSV[date, obs]}
       // if only one pixel, then it is only one csv. Generally (2*intNB+1)^2
       
       
        //TODO:  ncfile, intx, inty will be determined from the input parameters
       
       String ncfile="/Softdata/data/water_extents/149_-036/py_stacked_CF.nc";
       int xint=1708;
       int yint=341;
       
       ga.wofs.dao.WofsTimeSeries wts= new ga.wofs.dao.WofsTimeSeries(ncfile);
       
       String mycsv=wts.retrieveTimeSeries(xint, yint);
       
       return mycsv;
                    
       // return "myjson"; //.toString();
   }
}
