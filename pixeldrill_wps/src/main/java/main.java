
import ga.wofs.dao.PixelTimeSeries;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 **
 *
 * @author fxz547
 */
/**
 * The main class to expose command line arguments.
 */
public class main {

    /**
     * Main class to execute a limited number of functions from the command
     * line.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String ncfile = "/g/data/u46/fxz547/wofs/extents/149_-036_Y2015";
        PixelTimeSeries pxts = new PixelTimeSeries(ncfile);

        ArrayList pntList = new ArrayList<int[]>();

        pntList.add(new int[]{10, 20});
        pntList.add(new int[]{30, 40});

        String resultdata = pxts.retrieveTimeSeries(pntList);
        
        System.out.println( resultdata );
    }

}
