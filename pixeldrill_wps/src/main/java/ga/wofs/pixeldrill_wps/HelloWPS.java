/*
 * A simple Hello WPS  http://docs.geoserver.org/stable/en/developer/programming-guide/wps-services/implementing.html

How2Test:
 *  http://localhost:8080/geoserver/ows?service=wps&version=1.0.0&request=execute&identifier=gs:HelloWPS&RawDataOutput=result&dataInputs=name=John
 */
package ga.wofs.pixeldrill_wps;

/**
 *
 * @author fei.zhang@ga.gov.au
 */

import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geoserver.wps.gs.GeoServerProcess;

@DescribeProcess(title="helloWPS", description="Hello WPS Sample")
public class HelloWPS implements GeoServerProcess {

   @DescribeResult(name="result", description="output result")
   public String execute(@DescribeParameter(name="name", description="name to return") String name) {
        return "<HTML> <H1> Hello </H1>, " + name + " You have successfully accessed a WPS! </HTML>";
        
        // return a HTML doc with link to javascript source graphics.
   }
}