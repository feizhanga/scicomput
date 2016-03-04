#!/bin/env python
#
# given a pixel (x, y) drill down a netcdf file to find all tme series data for that pixel
#=====================================================
from osgeo import gdal,ogr
import struct
import argparse
import os
from os import listdir, makedirs
from os.path import isfile, join, exists
from datetime import datetime
import re

iso8601 = r"(\d{4})-(\d\d)-(\d\d)T(\d\d)-(\d\d)-(\d\d\.\d*)"

VALUE_NAME = {
    128: 'WET',
    -128: 'WET',
    -127: 'WhatIsThis',
    64:  'CLOUD',
    32:  'CLOUD_SHADOW',
    16:  'HIGH_SLOPE',
    8:   'TERRAIN_SHADOW',
    4:   'SEA_WATER',
    2:   'NO_CONTIGUITY',
    1:   'NO_DATA',
    0:   'DRY'
    }


def getByteValue(filename, x, y) :
    src_ds=gdal.Open(filename) 
    rb=src_ds.GetRasterBand(1)

    structval=rb.ReadRaster(x,y,1,1,buf_type=gdal.GDT_Byte) #Assumes 8 bit int 
    intval = struct.unpack('B' , structval) 

    src_ds = None
    return intval[0]

def getUint16Value(filename, x, y) :
    src_ds=gdal.Open(filename) 
    rb=src_ds.GetRasterBand(1)

    structval=rb.ReadRaster(x,y,1,1,buf_type=gdal.GDT_UInt16) #Assumes 16 bit int aka 'short'
    intval = struct.unpack('H' , structval) #use the 'short' format code (2 bytes) not int (4 bytes)

    src_ds = None
    return intval[0]

def getInt16Value(filename, x, y) :
    src_ds=gdal.Open(filename) 
    result = [None] * 6 
    for b in range(1,7) :
        rb=src_ds.GetRasterBand(b)

        structval=rb.ReadRaster(x,y,1,1,buf_type=gdal.GDT_Int16) #Assumes 16 bit int aka 'short'
        intval = struct.unpack('h' , structval) #use the 'short' format code (2 bytes) not int (4 bytes)

        result[b-1] = intval[0]

    src_ds = None
    return result 

def parseISO8601(aString) :

    m = re.search(iso8601, aString) 
    if m :
        year = int(m.group(1))
        month = int(m.group(2))
        day = int(m.group(3))
        hour = int(m.group(4))
        mins = int(m.group(5))
        sec = float(m.group(6))
        secs = int(sec)
        microsecs = int(1000000 * (sec - secs))

        dt = datetime(year, month, day, hour, mins, secs, microsecs)
        return dt
    return None


def tiffs_drill(cellDirPath, x, y):
    
    fileRE = r"^(.{3})_(.*)_WATER_(\d{3}_[+|-]\d{3})_(.*)\.tif$"
    cnt = 0
    onlyfiles = [ f for f in listdir(cellDirPath) if isfile(join(cellDirPath,f)) ]
    
    for filename in onlyfiles:
        m = re.search(fileRE, filename)
        if m :
            satellite = m.group(1)
            sensor = m.group(2)
            latLong = m.group(3)
            timestamp = m.group(4)

            dt = timestamp   #parseISO8601(timestamp) 

    #        pqPath = join(datacubePath,"%s_%s" % (satellite, sensor), latLong, str(dt.year))
    #        pqFilename = "%s_%s_PQA_%s_%s.tif" % (satellite, sensor, latLong, timestamp) 
            waterExtentFilename = "%s_%s_WATER_%s_%s.tif" % (satellite, sensor, latLong, timestamp) 
            value = getByteValue(join(cellDirPath,filename), x, y)
            #print filename, value, VALUE_NAME[value] 
            print x, y, dt[:19],  VALUE_NAME[value] 
    
    
def netcdf_drill(ncfile, x,y):
    import numpy
    import netCDF4
    
    with netCDF4.Dataset( ncfile ) as ncobj:
        var_time = ncobj['time']
        tnum=var_time.shape
        print tnum
        for i in range(0,tnum[0]):
            print var_time[i],   ncobj['Data'][x,y,i], VALUE_NAME[ ncobj['Data'][x,y,i] ]
            #print var_time[i],  VALUE_NAME[ ncobj['Data'][x,y,i] ]
    
############################################################################################################
# Test Usage: compare different extents and web results: http://eos-test.ga.gov.au/geoserver/www/remote_scripts/WOfS_v1.5.htm 
#   python waterExtentDrill.py -e /g/data/fk4/wofs/water_f7q/extents/ --lat  -35.08531 --lon 149.42719 > Drill_f7q_lon149.42719lat-35.08531
#   python waterExtentDrill.py -e /g/data/fk4/wofs/current/extents/ --lat  -35.08531 --lon 149.42719 > Drill_water_20160203_lon149.42719lat-35.08531
#python waterExtentDrill_netcdf.py -e /g/data/u46/fxz547/wofs/extents/ --lat  -25.08531 --lon 143.42719
#python waterExtentDrill_netcdf.py -e /g/data/u46/fxz547/wofs/extents/149_-036/stacked_tiffs_zlib_true.nc --lat  -35.08531 --lon 149.42719
#---------------------------------------------------------------------------------------------------------
if __name__ =="__main__":
    
    parser = argparse.ArgumentParser("Drill into water extent stack and extract time series for pixel")
    parser.add_argument("-e", "--extentsPath",  type=str, help="path to the water extents", required=True)
    #                         default="/g/data1/v27/water_smr/water_f7m/extents" )
    
    parser.add_argument("--lat", type=float,  help="the latitude of the pixel", required=True)
    parser.add_argument("--lon", type=float,  help="the longitude of the pixel", required=True)
    args = parser.parse_args()

    # get args

    extentsPath = args.extentsPath
    lat = args.lat
    lon = args.lon
    cellId = "%03d_%04d" % (int(lon), int(lat-1))

    # 2D array coordinate?
    x = int((lon - int(lon)) / 0.00025)
    y = int((int(lat)-lat) / 0.00025)
    #y = int((lat - int(lat-1)) / 0.00025)

    print "(%f, %f): cellId=%s (%d, %d)" % (lon, lat, cellId, x, y)

    if os.path.isdir(extentsPath):
        cellDirPath = "%s/%s" % (extentsPath, cellId)
        print "Drilling into a stack of geotiff files in ", cellDirPath
        tiffs_drill(cellDirPath,x,y)
    elif os.path.isfile(extentsPath):  #netcdf file?
        print "Drilling into a netcdf file", extentsPath
        netcdf_drill(extentsPath,y,x)

