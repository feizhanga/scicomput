#Ref: http://nbviewer.ipython.org/github/Unidata/netcdf4-python/blob/master/examples/writing_netCDF.ipynb
"""
https://github.com/Unidata/netcdf4-python|http://nbviewer.ipython.org/github/Unidata/netcdf4-python/blob/master/examples/writing_netCDF.ipynb|http://nbviewer.ipython.org/github/Unidata/netcdf4-python/blob/master/examples/reading_netCDF.ipynb#Each-dimension-typically-has-a-variable-associated-with-it-(called-a-coordinate-variable).|http://unidata.github.io/netcdf4-python/|http://www.unidata.ucar.edu/software/netcdf/examples/programs/|https://www.hdfgroup.org/HDF5/Tutor/tools.html|https://www.hdfgroup.org/products/java/release/download.html|https://www.google.com.au/search?q=gdal+netcdf4&ie=utf-8&oe=utf-8&gws_rd=cr&ei=5UROVom9M-OomgWHqaiYCA#q=gdal+netcdf+driver|http://www.gdal.org/frmt_netcdf.html|https://stash.csiro.au/projects/CMAR_RS/repos/netcdf-tools/browse|https://docs.google.com/document/d/1LF1sEYZYPF5XoLKLfXWLx1voSYnNcH7I1ZrQZourMPU/edit|http://cfconventions.org/Data/cf-standard-names/29/build/cf-standard-name-table.html|http://www.auscover.org.au/node/158|http://nco.sourceforge.net/nco.html#nces

"""

from __future__ import print_function # make sure print behaves the same in 2.7 and 3.x
import netCDF4     # Note: python is case-sensitive!
import numpy as np

try: 
    ncfile.close()  # just to be safe, make sure dataset is not already open.
except: 
    pass

ncfile = netCDF4.Dataset('/datacube/new1.nc',mode='w') 
#ncfile = netCDF4.Dataset('/datacube/new1.nc',mode='w',format='NETCDF4_CLASSIC') 
print(ncfile)

# create dimensions
print("******** Create Dimensions *********")
lat_dim = ncfile.createDimension('lat', 73)     # latitude axis
print (lat_dim)
lon_dim = ncfile.createDimension('lon', 144)    # longitude axis
time_dim = ncfile.createDimension('time', None) # unlimited axis (can be appended to).
for dim in ncfile.dimensions.items():
    print(dim)


# Create Attributes
print("******** Create Attributes *********")
ncfile.title='My model data'
print(ncfile.title)



print ("************** Creating variables **************")
# Creating variables
# Now let's add some variables and store some data in them.

#   A variable has a name, a type, a shape, and some data values.
#   The shape of a variable is specified by a tuple of dimension names.
#   A variable should also have some named attributes, such as 'units', that describe the data.

# Define two variables with the same names as dimensions,
# a conventional way to define "coordinate variables".
lat = ncfile.createVariable('lat', np.float32, ('lat',))
lat.units = 'degrees_north'
lat.long_name = 'latitude'
lon = ncfile.createVariable('lon', np.float32, ('lon',))
lon.units = 'degrees_east'
lon.long_name = 'longitude'
time = ncfile.createVariable('time', np.float64, ('time',))
time.units = 'hours since 1800-01-01'
time.long_name = 'time'
# Define a 3D variable to hold the data
temp = ncfile.createVariable('temp',np.float64,('time','lat','lon')) # note: unlimited dimension is leftmost
temp.units = 'K' # degrees Kelvin
temp.standard_name = 'air_temperature' # this is a CF standard name
print(temp)



#Pre-defined variable attributes (read only)

#The netCDF4 module provides some useful pre-defined Python attributes for netCDF variables, such as dimensions, shape, dtype, ndim.


print("-- Some pre-defined attributes for variable temp:")
print("temp.dimensions:", temp.dimensions)
print("temp.shape:", temp.shape)
print("temp.dtype:", temp.dtype)
print("temp.ndim:", temp.ndim)


nlats = len(lat_dim); nlons = len(lon_dim); ntimes = 3
# Write latitudes, longitudes.
# Note: the ":" is necessary in these "write" statements
lat[:] = -90. + (180./nlats)*np.arange(nlats) # south pole to north pole
lon[:] = (180./nlats)*np.arange(nlons) # Greenwich meridian eastward
# create a 3D array of random numbers
data_arr = np.random.uniform(low=280,high=330,size=(ntimes,nlats,nlons))
# Write the data.  This writes the whole 3D netCDF variable all at once.
temp[:,:,:] = data_arr  # Appends data along unlimited dimension
print("-- Wrote data, temp.shape is now ", temp.shape)
# read data back from variable (by slicing it), print min and max
print("-- Min/Max values:", temp[:,:,:].min(), temp[:,:,:].max())




# Let us add another time slice....
# create a 2D array of random numbers
data_slice = np.random.uniform(low=280,high=330,size=(nlats,nlons))
temp[3,:,:] = data_slice   # Appends the 4th time slice
print("-- Wrote more data, temp.shape is now ", temp.shape)



# first print the Dataset object to see what we've got
print(ncfile)
# close the Dataset.
ncfile.close(); print('Dataset is closed!')


def datetime_fun():
    from datetime import datetime
    from netCDF4 import date2num,num2date
    # 1st 4 days of October.
    dates = [datetime(2014,10,1,0),datetime(2014,10,2,0),datetime(2014,10,3,0),datetime(2014,10,4,0)]
    print(dates)
    times = date2num(dates, time.units)
    print(times, time.units) # numeric values
    time[:] = times
    # read time data back, convert to datetime instances, check values.
    print(num2date(time[:],time.units))


if __name__== "__main__":

    datetime_fun()
