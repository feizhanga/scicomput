from netCDF4 import Dataset
import pylab as pl

root_grp = Dataset('test.nc')

temp = root_grp.variables['temp']

print "length of temp", len(temp)

for i in range(len(temp)):
    pl.clf()
    pl.contourf(temp[i])
    pl.show()
    raw_input('Press enter.')
