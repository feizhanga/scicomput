#
# This example creates an empty HDF5 file file.h5 using H5Py interfaces
# to the HDF5 library. 
#
import h5py
#
# Use 'w' to remove existing file and create a new one; use 'w-' if
# create operation should fail when the file already exists.
#
f = h5py.File('file.h5','w')
#
# Close the file before exiting
#
f.close()
