/*
 * A simple Hello WPS  http://docs.geoserver.org/stable/en/developer/programming-guide/wps-services/implementing.html

How2Test:
 *  http://localhost:8080/geoserver/ows?service=wps&version=1.0.0&request=execute&identifier=gs:HelloWPS&RawDataOutput=result&dataInputs=name=John
 */
package ga.wofs.wps;

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
       
       String HTTP_HEADER="HTTP/1.1 200 OK\n" +
        "Date: Mon, 14 March 2016 10:38:34 GMT\n" +
        "Content-Type: text/html; charset=UTF-8\n" +
        "Content-Encoding: UTF-8\n" +
        //"Content-Length: 138\n" +
        "Connection: close\n\n";
       
        return HTTP_HEADER +  "<HTML> <H1> Hello </H1>, " + name + " You have successfully accessed a WPS! </HTML>";
        
        // return a HTML doc with link to javascript source graphics.
        // see http-header https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol
   }
}
