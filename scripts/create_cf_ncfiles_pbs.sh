#!/bin/bash

# Purpose: create CF-compliant netCDF files of water extent (for pixel drill)

#PBS -N CreateCF.NC 
#PBS -P u46
#PBS -q express
#PBS -l walltime=24:00:00,ncpus=2,mem=6GB,jobfs=35GB
#PBS -l wd
#PBS -me
#PBS -M lpgs@nci.org.au



EXTENTS_DIR=/g/data/u46/wofs/extents

# EXTENTS_NC=/g/data/u46/wofs/extents2nc

STACKER_PY=/home/547/fxz547/github/scicomput/GeoDataSoft/netCDF4/stack_tiffs2netcdf_CF.py 

#STACKER_PY=/home/547/fxz547/github/scicomput/GeoDataSoft/netCDF4/stack_tiffs2netcdf.py #OLD format NC file

#for acell_dir in `ls -d $EXTENTS_DIR/1[12345]?_???`; do

for acell_dir in `ls -d $EXTENTS_DIR/12*`; do
    acell=`basename $acell_dir`  

    # $acell_dir = /g/data/u46/wofs/extents/149_-036  AND  $acell is like 149_-036
    echo "Got to process $acell_dir $acell"

    #mkdir $EXTENTS_NC/$acell    # If necessary, 

    ncfiles=`ls $acell_dir/LS_WATER*.nc 2> /dev/null`

    if [ -z "$ncfiles" ]; then
        echo "Will be running:  python $STACKER_PY $acell_dir;  to create a LS_WATER_*.nc"

        python $STACKER_PY   $acell_dir
    else
        echo "NetCDF file already exists: $ncfiles. Skip processing ..........."
    fi

    # Proper Target_Ncfile_Name ?
    #  python $STACKER_PY  $EXTENTS_DIR/$acell $EXTENTS_NC/$acell/LS_WATER_${acell}_DTBegin_DTEnd.nc

done
