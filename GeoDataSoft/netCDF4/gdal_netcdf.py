"""
use gdal to handle netcdf files

Ref: 
    http://geoexamples.blogspot.com.au/2013/09/reading-wrf-netcdf-files-with-gdal.html
    http://gis.stackexchange.com/questions/112595/gdal-for-python-extracting-subdomains-from-netcdf-file

"""

import gdal
import os, sys

#######################################
# Usage: 
# python gdal_netcdf.py /short/public/democube/data/LANDSAT_5_TM_149_-36_NBAR_1990-03-02T23-11-04.000000.nc

if __name__ == "__main__":

    datafile =sys.argv[1]

    ds_in = gdal.Open(datafile)  
    metadata = ds_in.GetMetadata()  
    print metadata
