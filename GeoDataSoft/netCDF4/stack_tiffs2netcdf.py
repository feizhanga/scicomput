import os
import re
import sys
import numpy
import netCDF4
import rasterio
from glob import glob
from datetime import datetime
from collections import namedtuple
from osgeo import osr



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
#issue with timechecksize=100? def create_netcdf(filename, tiles, zlib_flag=True, timechunksize0=100):
def create_netcdf(filename, tiles, zlib_flag=True, timechunksize0=100):

    timechunksize = min(timechunksize0, len(tiles))
    
    # open the first datatset to pull out spatial information
    first = rasterio.open(tiles[0].filename)
    crs = osr.SpatialReference(first.crs_wkt.encode('utf8'))
    affine = first.affine
    width, height = first.width, first.height
        
    with netCDF4.Dataset(filename, 'w') as nco:
        nco.date_created = datetime.today().isoformat()  
        nco.Conventions = 'CF-1.6'  

        # crs variable
        crs_var = nco.createVariable('crs', 'i4')
        crs_var.long_name = crs.GetAttrValue('GEOGCS')
        crs_var.grid_mapping_name = 'latitude_longitude'
        crs_var.longitude_of_prime_meridian = 0.0
        crs_var.spatial_ref = crs.ExportToWkt()
        crs_var.semi_major_axis = crs.GetSemiMajor()
        crs_var.semi_minor_axis = crs.GetSemiMinor()
        crs_var.inverse_flattening = crs.GetInvFlattening()
        crs_var.GeoTransform = affine.to_gdal()

        # latitude coordinate
        nco.createDimension('latitude', height)
        lat_coord = nco.createVariable('latitude', 'float64', ['latitude'])
        lat_coord.standard_name = 'latitude'
        lat_coord.long_name = 'latitude'
        lat_coord.axis = 'Y'
        lat_coord.units = 'degrees_north'
        lat_coord[:] = numpy.arange(height) * affine.e + affine.f + affine.e / 2

        # longitude coordinate
        nco.createDimension('longitude', width)
        lon_coord = nco.createVariable('longitude', 'float64', ['longitude'])
        lon_coord.standard_name = 'longitude'
        lon_coord.long_name = 'longitude'
        lon_coord.axis = 'X'
        lon_coord.units = 'degrees_east'
        lon_coord[:] = numpy.arange(width) * affine.a + affine.c + affine.a / 2

        # time coordinate
        nco.createDimension('time', len(tiles))
        time_coord = nco.createVariable('time', 'double', ['time'])
        time_coord.standard_name = 'time'
        time_coord.long_name = 'Time, unix time-stamp'
        time_coord.axis = 'T'
        time_coord.calendar = 'standard'
        time_coord.units = 'seconds since 1970-01-01 00:00:00'
        time_coord[:] = [(tile.datetime-datetime(1970, 1, 1, 0, 0, 0)).total_seconds() for tile in tiles]

        # wofs data variable
        data_var = nco.createVariable('Data',
                                      #'uint8',
                                      'int8',
                                      ['latitude', 'longitude', 'time'],
                                      chunksizes=[100, 100, timechunksize],
                                      zlib=True,
                                      complevel=1)
        data_var.grid_mapping = 'crs'
        #data_var.valid_range = [0, 255]
        #data_var.flag_masks = [1, 2, 4, 8, 16, 32, 64, 128]  #cause gdalinfo seg fault
 	data_var.flag_meanings = "water128 cloud64 cloud_shadow32 high_slope16 terrain_shadow8 over_sea4 no_contiguity2 nodata1 dry0"

        #tmp = numpy.empty(dtype='uint8', shape=(height, width, timechunksize))
        tmp = numpy.empty(dtype='int8', shape=(height, width, timechunksize))
        for start_idx in range(0, len(tiles), timechunksize):
            #read `timechunksize` worth of data into a temporary array
            end_idx = min(start_idx+timechunksize, len(tiles))
            for idx in range(start_idx, end_idx):
                with rasterio.open(tiles[idx].filename) as tile_data:
                    tmp[:,:,idx-start_idx] = tile_data.read(1)
            #write the data into necdffile
            data_var[:,:,start_idx:end_idx] = tmp[:,:,0:end_idx-start_idx]
            sys.stdout.write("\r%d out of %d done\r" % (end_idx, len(tiles)))
            sys.stdout.flush()


def create_netcdf_from_dir(extents_dir, out_ncfile=None):
    zlib_flagv = True 
    #zlib_flagv = False 

    tiles = [make_tileinfo(filename) for filename in glob(os.path.join(extents_dir, '*.tif'))]
    tiles.sort(key=lambda t: t.datetime)

    path2ncfile = out_ncfile
    if  out_ncfile is None:
        #makeup a nc file name like LS_WATER_149_-036_1987-05-22T23-08-20_2014-03-28T23-47-03.nc
        cellid = os.path.basename(os.path.normpath(extents_dir))  #assumed like 149_-036
        begindt =tiles[0].datetime.isoformat().replace(':','-')
        enddt = tiles[-1].datetime.isoformat().replace(':','-')

        ncfile_name = "LS_WATER_%s_%s_%s.nc"%(cellid,begindt,enddt) 
        path2ncfile= os.path.join(extents_dir,ncfile_name)

    create_netcdf(path2ncfile, tiles, zlib_flagv)

def verify_netcdf(extents_dir, out_ncfile):
    """verify the stacked nc file's pixel values agaist the tiff files
    """
    netcdf_old=out_ncfile #'/g/data/fk4/wofs/water_f7q/extents/149_-036/LS_WATER_149_-036_1987-05-22T23-08-20.154_2014-03-28T23-47-03.171.nc'

    tiles = [make_tileinfo(filename) for filename in glob(os.path.join(extents_dir, '*.tif'))]
    tiles.sort(key=lambda t: t.datetime)

    with netCDF4.Dataset(netcdf_old) as nco:
        for i in range(0,len(tiles)):
            print nco['time'][i]
            print tiles[i]
            with rasterio.open(tiles[i].filename) as tile_data:
                print "Any difference? " 
                print numpy.sum(nco['Data'][:,:,i])
                print numpy.sum(tile_data.read(1))

                print type(nco['Data'][:,:,i]), type(tile_data.read(1))
                print nco['Data'][:,:,i].shape, tile_data.read(1).shape
                
                print  numpy.sum(nco['Data'][:,:,i] - tile_data.read(1)[:,:])
                #print  tile_data.read(1)[0:100,0:100] 

                #print (nco['Data'][:,:,i] == tile_data.read(1)).all()
###################################################################
# Usage python thiscript.py /g/data/u46/fxz547/wofs/extents/149_-036 /g/data/u46/fxz547/wofs/extents/149_-036/stacked.nc
if __name__ == "__main__":
    #extents_dir = '/g/data/u46/wofs/extents/149_-036'
    #extents_dir = '/g/data/u46/fxz547/wofs/extents/149_-036'

    extents_dir = sys.argv[1]
    #optional out_ncfile =sys.argv[2]

    create_netcdf_from_dir(extents_dir)
    #create_netcdf_from_dir(extents_dir, out_ncfile)
    #verify_netcdf(extents_dir, out_ncfile)
