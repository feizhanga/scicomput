/*
 * Retrieve the whole time series data for given pixels from a netcdf file
 */
package ga.wofs.dao;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author fxz547
 */
public class PixelTimeSeries {

    private String netcdf4File;
    //private ArrayList<Integer[]> coordlist = new ArrayList<Integer[]>();

    public PixelTimeSeries(String path2ncfile) {
        this.netcdf4File = path2ncfile;

    }

    public String retrieveTimeSeries(ArrayList<int[]> latlonList) {

        // retrieve the time series of the given pixel coordinates
        Iterator it = latlonList.iterator();

        while (it.hasNext()) {
            int[] coord_point = (int[]) it.next();

            System.out.println( coord_point[0] + "," + coord_point[1]);
        }

        return "json for latlon->csv";

    }

    public static void main(String[] args){
        
        String ncfile="/g/data/u46/fxz547/wofs/extents/149_-036_Y2015";
        PixelTimeSeries pxts = new PixelTimeSeries(ncfile);
        
        ArrayList pntList= new ArrayList<int[]>();

        pntList.add( new int[]{10,20} );
        pntList.add( new int[]{30,40} );
    
        String result = pxts.retrieveTimeSeries(pntList);
        
        System.out.println( result );
        
    }
}
