This is a service to fill zip table
1. Download country zip files from: http://download.geonames.org/export/zip/
2. Put them to **countries_zip** directory
3. Run _import_zips.sh DATABASE_NAME_ 


The script unzips the files and inserts the content from the files into **all_countries** table.

Additionally it creates a table with regions, with the same names like in locatable table.

After that using **all_countries** and **regions** table **zip_table** is created. This table is exported to migration data directory.