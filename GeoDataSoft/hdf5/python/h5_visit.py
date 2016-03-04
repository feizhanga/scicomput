import h5py
f = h5py.File('GATMO-SATMS-npp.h5', 'r+')
f.keys()
f.values()
members = []
f.visit(members.append)
for i in range(len(members)):
    print members[i]

