#!/bin/bash

#PBS -N CreateNCF 
#PBS -P u46
#PBS -q normal 
#PBS -l walltime=20:10:00,ncpus=8,mem=16GB,jobfs=35GB
#PBS -l wd
#PBS -me
#PBS -M lpgs@nci.org.au



EXTENTS_DIR=/g/data/fk4/wofs/water_20160203/extents

#EXTENTS_NC=/g/data/u46/wofs/extents2nc

#STACKER_PY=/home/547/fxz547/github/scicomput/GeoDataSoft/netCDF4/stack_tiffs2netcdf_CF.py 
STACKER_PY=/home/547/fxz547/github/scicomput/GeoDataSoft/netCDF4/stack_tiffs2netcdf.py #OLD format NC file

for acell_dir in `ls -d $EXTENTS_DIR/1*`; do
    acell=`basename $acell_dir`  # /g/data/u46/wofs/extents/149_-036
    # value like acell=149_-036
    echo Processing $acell_dir $acell;
    #mkdir $EXTENTS_NC/$acell

    echo running  python $STACKER_PY $acell_dir 

    python $STACKER_PY   $acell_dir

    # python $STACKER_PY  $EXTENTS_DIR/$acell $EXTENTS_NC/$acell/LS_WATER_${acell}_DTBegin_DTEnd.nc

done
