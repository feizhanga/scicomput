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
public class WofsTimeSeries {

    private String netcdf4File;
    //private ArrayList<Integer[]> coordlist = new ArrayList<Integer[]>();
    private ReadNetcdf ncreader = new ReadNetcdf();

    public WofsTimeSeries(String path2ncfile) {
        this.netcdf4File = path2ncfile;
      
    }

    public String retrieveTimeSeries(int xint, int yint) {

        String res="csv";
        // retrieve the time series of the given pixel coordinates

            
        res=ncreader.drill_netcdf_CF(netcdf4File, xint, yint);
        
        return res;

    }
    
     public String retrieveTimeSeries(ArrayList<int[]> latlonList) {

        String res="csv";
        // retrieve the time series of the given pixel coordinates
        Iterator it = latlonList.iterator();

        while (it.hasNext()) {
            int[] coord_point = (int[]) it.next();

            System.out.println( coord_point[0] + "," + coord_point[1]);
            
            res=ncreader.drill_netcdf_CF(netcdf4File, coord_point[0], coord_point[1]);
        }

        
        return res;

    }

    public static void main(String[] args){
        
        //String ncfile=args[0]; // "/g/data/u46/fxz547/wofs/extents/149_-036_Y2015";
        
        String ncfile = null;
        int x_lon = 0;
        int y_lat = 0;

        try {
            ncfile = args[0];
            x_lon = Integer.parseInt(args[1]);
            y_lat = Integer.parseInt(args[2]);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("USAGE: netcdffile, int_lon, int_lat ");
            System.exit(1);
        }
        
        WofsTimeSeries pxts = new WofsTimeSeries(ncfile);
        
        ArrayList pntList= new ArrayList<int[]>();

        pntList.add( new int[]{x_lon,y_lat} );
        //more pixels: pntList.add( new int[]{30,40} );
    
        String result = pxts.retrieveTimeSeries(pntList);
        
        System.out.println( result );
        
    }
}
