# -*- coding: utf-8 -*-
"""
@CreateDate: 2015-11-19

@Author: fei.zhang@ga.gov.au
"""

from __future__ import print_function # make sure print behaves the same in 2.7 and 3.x
import netCDF4     # Note: python is case-sensitive!
import numpy as np

import os, sys

class NetcdfCRUD:
    def __init__(self, path2ncfile):
        self.path2ncfile=path2ncfile
    
  
    def create(self, data):
        raise Exception ("NotImplementedException")
        return None
        
    def update(self, newdata):
        raise Exception ("NotImplementedException")
        return None
        
    def delete(self, tobe_deleted_data):
        raise Exception ("NotImplementedException")
        return None
          
        
    def showinfo(self,f, varname):
        """ show info about f varname
        """
        extra_md=f.variables[ varname ]
        print(extra_md)
        print(type(extra_md))
        print(extra_md[:])
        for nitem in range(0,len(extra_md)): 
            print(extra_md[nitem])

     

    def read(self):
        """ read from the self.path2ncfile
        """
        f = netCDF4.Dataset(self.path2ncfile)
        print(f) # similar to ncdump -h
        
# Access a netcdf variables:
#     variable objects stored by name in variables dict.
#     print the variable yields summary info (including all the attributes).
#     no actual data read yet (just have a reference to the variable object with metadata).

        print(f.variables.keys()) # get all variable names
        #band1var = f.variables['band1']  # temperature variable
        band1var = f.variables['time']  # temperature variable
        print(band1var) 
        print(band1var[:])

        self.showinfo(f, 'time')  
        self.showinfo(f, 'longitude')  
        self.showinfo(f, 'latitude')  
        #self.showinfo(f,'extra_metadata')
###############################################################################
# Usage Examples:
# python netcdf_crud.py /datacube/agdcv2_datasets/combined_151_-30.nc
# python netcdf_crud.py /datacube/gdata/LANDSAT_7_ETM_149.0_-35.nc
#[fxz547@raijin3 netCDF4]$
# python gdal_netcdf.py /g/data/fk4/wofs/water_f7q/extents/149_-036/LS_WATER_149_-036_1987-05-22T23-08-20.154_2014-03-28T23-47-03.171.nc
# python netcdf_crud.py /short/public/democube/data/LANDSAT_5_TM_151_-36_PQ_1990-03-02T23-11-04.000000.nc

#--------------------------------------------------------------------    
if __name__=="__main__":
    in_ncfile=sys.argv[1]
    
    ncobj=NetcdfCRUD(in_ncfile)
    ncobj.read()
    
        
                
