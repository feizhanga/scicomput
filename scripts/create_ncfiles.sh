#!/bin/bash

EXTENTS_DIR=/g/data/u46/wofs/extents

EXTENTS_NC=/g/data/u46/wofs/extents2nc

STACKER_PY=/home/547/fxz547/github/scicomput/GeoDataSoft/netCDF4/stack_tiffs2netcdf_CF.py 

for acell_dir in `ls -d $EXTENTS_DIR/149*`; do
    acell=`basename $acell_dir`  # /g/data/u46/wofs/extents/149_-036
    # value like acell=149_-036
    echo Processing $acell;
    mkdir $EXTENTS_NC/$acell

    echo running  python $STACKER_PY  $EXTENTS_DIR/$acell $EXTENTS_NC/$acell//LS_WATER_${acell}_DTBegin_DTEnd.nc
    python $STACKER_PY  $EXTENTS_DIR/$acell $EXTENTS_NC/$acell/LS_WATER_${acell}_DTBegin_DTEnd.nc

done
