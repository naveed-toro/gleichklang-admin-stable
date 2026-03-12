#!/usr/bin/env bash

DATABASE_NAME=$1
# creates table for all countries
mysql -ugleichklang -pgleichklang -h127.0.0.1 ${DATABASE_NAME} < create_all_countries.sql

COUNTRIES_DIR=countries

rm -rf $COUNTRIES_DIR
mkdir $COUNTRIES_DIR

ZIP_FILENAMES=countries_zip/*.zip

# unzips zip files from zip directory and for each country put content to the database table
for f in $ZIP_FILENAMES
do
    ZIP_FILENAME=$f
    DIRNAME=${ZIP_FILENAME##*/}
    unzip -o "$f" -d countries

    DATAFILENAME=countries/${DIRNAME%%.*}.txt
    echo processing country $DATAFILENAME ...
    QUERY="load data local infile '"$DATAFILENAME"' into table all_countries CHARACTER SET UTF8 fields TERMINATED BY '\t' LINES TERMINATED BY '\n'"
    mysql -e "$QUERY" -ugleichklang -pgleichklang -h127.0.0.1 ${DATABASE_NAME} --local-infile
done


echo "Inserting values into zip_table"
mysql -ugleichklang -pgleichklang -h127.0.0.1 ${DATABASE_NAME} < create_zip_table.sql

mysql -ugleichklang -pgleichklang -h127.0.0.1 ${DATABASE_NAME} --execute "SELECT * FROM zip_table" > zip_table.csv
mv -f zip_table.csv ../../../main/resources/db/migration/data/