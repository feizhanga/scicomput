import os
import re
import sys
import numpy
import netCDF4
import rasterio
from glob import glob
from datetime import datetime
from collections import namedtuple  #immutable beans
from osgeo import osr

#water extent pixel value -> disctionary 
VALUE_DICT = {
            128: 'WET',
            -128: 'WET',
            64:  'CLOUD',
            32:  'CLOUD_SHADOW',
            16:  'HIGH_SLOPE',
            8:   'TERRAIN_SHADOW',
            4:   'SEA_WATER',
            2:   'NO_CONTIGUITY',
            1:   'NO_DATA',
            0:   'DRY'
            }


TileInfo = namedtuple('TileInfo', ['filename', 'datetime'])

def parse_filename(filename):
    fields = re.match(
        (
            r"(?P<vehicle>LS[578])"
            r"_(?P<instrument>OLI_TIRS|OLI|TIRS|TM|ETM)"
            r"_(?P<type>WATER)"
            r"_(?P<longitude>[0-9]{3})"
            r"_(?P<latitude>-[0-9]{3})"
            r"_(?P<date>.*)"
            "\.tif$"
        ),
        filename).groupdict()
        
        
    return fields
    
def make_tileinfo(filename):
    basename = os.path.basename(filename)
    fields = parse_filename(os.path.basename(basename))
    dt = datetime.strptime(fields['date'][:19], '%Y-%m-%dT%H-%M-%S')
    return TileInfo(filename, datetime=dt)


###############################
def create_netcdf(ncfilename, tiles, zlib_flag=True, timechunksize0=100):
    """create a netCDF-4 ncfilename with Data(time,lat,lon) and CF1.6 metadata convention.
    """

    timechunksize = min(timechunksize0, len(tiles))
    
    # open the first datatset to pull out spatial information
    first = rasterio.open(tiles[0].filename)
    crs = osr.SpatialReference(first.crs_wkt.encode('utf8'))
    affine = first.affine
    width, height = first.width, first.height
        
    with netCDF4.Dataset(ncfilename, 'w') as ncobj:
        ncobj.date_created = datetime.today().isoformat()  
        ncobj.Conventions = 'CF-1.6'  

        # crs variable
        crs_var = ncobj.createVariable('crs', 'i4')
        crs_var.long_name = crs.GetAttrValue('GEOGCS')
        crs_var.grid_mapping_name = 'latitude_longitude'
        crs_var.longitude_of_prime_meridian = 0.0
        crs_var.spatial_ref = crs.ExportToWkt()
        crs_var.semi_major_axis = crs.GetSemiMajor()
        crs_var.semi_minor_axis = crs.GetSemiMinor()
        crs_var.inverse_flattening = crs.GetInvFlattening()
        crs_var.GeoTransform = affine.to_gdal()

        # latitude coordinate
        ncobj.createDimension('latitude', height)
        lat_coord = ncobj.createVariable('latitude', 'float64', ['latitude'])
        lat_coord.standard_name = 'latitude'
        lat_coord.long_name = 'latitude'
        lat_coord.axis = 'Y'
        lat_coord.units = 'degrees_north'
        lat_coord[:] = numpy.arange(height) * affine.e + affine.f + affine.e / 2

        # longitude coordinate
        ncobj.createDimension('longitude', width)
        lon_coord = ncobj.createVariable('longitude', 'float64', ['longitude'])
        lon_coord.standard_name = 'longitude'
        lon_coord.long_name = 'longitude'
        lon_coord.axis = 'X'
        lon_coord.units = 'degrees_east'
        lon_coord[:] = numpy.arange(width) * affine.a + affine.c + affine.a / 2

        # time coordinate
        ncobj.createDimension('time', len(tiles))
        time_coord = ncobj.createVariable('time', 'double', ['time'])
        time_coord.standard_name = 'epochtime'
        time_coord.long_name = 'Time, unix time-stamp'
        time_coord.axis = 'T'
        time_coord.calendar = 'standard'
        time_coord.units = 'seconds since 1970-01-01 00:00:00'
        time_coord[:] = [(tile.datetime-datetime(1970, 1, 1, 0, 0, 0)).total_seconds() for tile in tiles]

        # wofs data variable
        data_var = ncobj.createVariable('Data',
                                      'uint8',
                                      #'int8',
                                      ['time', 'latitude', 'longitude'],
                                      chunksizes=[timechunksize, 100, 100 ],
                                      zlib=zlib_flag,
                                      complevel=1) # 1 lest compression, 9 most compression
        data_var.grid_mapping = 'crs'
        data_var.value_range = [0, 255]
        data_var.values = [0, 2, 4, 8, 16, 32, 64, 128];
        data_var.flag_meanings = "water128 cloud64 cloud_shadow32 high_slope16 terrain_shadow8 over_sea4 no_contiguity2 o_data1 dry0"
        data_var.dictionary=str(VALUE_DICT)


        tmp = numpy.empty(dtype='uint8', shape=(timechunksize, height, width )) #chunck of data to be compressed
        #tmp = numpy.empty(dtype='int8', shape=(timechunksize, height, width ))
        for start_idx in range(0, len(tiles), timechunksize):
            #read `timechunksize` worth of data into a temporary array
            end_idx = min(start_idx+timechunksize, len(tiles))
            for idx in range(start_idx, end_idx):
                with rasterio.open(tiles[idx].filename) as tile_data:
                    tmp[idx-start_idx, :,:] = tile_data.read(1)
           
           #write the data into necdffile
            data_var[start_idx:end_idx, :,:] = tmp[0:end_idx-start_idx, :,:]
            
            print("\r%d out of %d done" % (end_idx, len(tiles)))
        


def create_netcdf_from_dir(extents_dir, out_ncfile):
    #zlib_flagv = False 
    zlib_flagv = True 

    tiles = [make_tileinfo(filename) for filename in glob(os.path.join(extents_dir, '*.tif'))]
    tiles.sort(key=lambda t: t.datetime)


    create_netcdf(out_ncfile, tiles, zlib_flagv)

def verify_netcdf(extents_dir, ncfile):
    """verify the stacked nc file's pixel values agaist the extents_dir's tiff files
    """
    
    tiles = [make_tileinfo(filename) for filename in glob(os.path.join(extents_dir, '*.tif'))]
    tiles.sort(key=lambda t: t.datetime)

    with netCDF4.Dataset( ncfile ) as ncobj:
        for i in range(0,len(tiles)):
            ncdata = ncobj['Data'][i, :,:]
            print ncobj['time'][i]
            print tiles[i]
            with rasterio.open(tiles[i].filename) as tile_data:
                tiledata1= tile_data.read(1)[:,:]
            
                #print "Data Types? ", type(ncdata) ,type( tiledata1 )
                #print "shapes?", ncdata.shape, tiledata1.shape
                
                diff_sum = numpy.sum(ncdata - tiledata1)
                
                if (diff_sum != 0):
                    print ("Found a paie of different images: diff_sum= " , diff_sum )
                
                #print  tile_data.read(1)[0:100,0:100] 

                #print (ncobj['Data'][:,:,i] == tile_data.read(1)).all()
###################################################################
# Usage python thiscript.py /g/data/u46/fxz547/wofs/extents/149_-036 /g/data/u46/fxz547/wofs/extents/149_-036/stacked.nc
if __name__ == "__main__":
    #extents_dir = '/g/data/u46/wofs/extents/149_-036'
    #extents_dir = '/g/data/u46/fxz547/wofs/extents/149_-036'

    extents_dir = sys.argv[1]
    out_ncfile =sys.argv[2]

    create_netcdf_from_dir(extents_dir, out_ncfile)
    
    print "Completed the NC file creation. Now verifying?......"
    
    #verify_netcdf(extents_dir, out_ncfile)
