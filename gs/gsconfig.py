# see http://geoserver.geo-solutions.it/edu/en/rest/python_gsconfig.html

from geoserver.catalog import Catalog
import os

gspwd=os.environ['GS_ADMIN_PASS']

cat = Catalog("http://localhost:8080/geoserver/rest", "admin", gspwd)
layers=cat.get_layers()

print "............. All  layers ........ "
for eachl in layers:
    print eachl.name


print ".............. Workspaces ........ "

worksp=cat.get_workspaces()

for wk in worksp:
    print wk.name

print ".............. Stores ........ "

stores=cat.get_stores()

for st in stores:
    print st.name

print ".............. Styles ........ "

styles=cat.get_styles()

for st in styles:
    print st.name
