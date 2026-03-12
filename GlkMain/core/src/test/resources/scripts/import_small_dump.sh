#!/bin/bash

# usage:
#   import_small_dump.sh <ADDITIONAL ARGS PASSED TO MYSQL>
#

## begin configuration

src_username=gleichklang
src_password=gleichklang
src_db=gk_small
src_url=mysql0

dst_username=gleichklang
dst_password=gleichklang
dst_db=gleichklang

## end configuration

echo "DROP DATABASE IF EXISTS ${dst_db}; CREATE DATABASE ${dst_db};" | mysql -u${dst_username} -p${dst_password} $@
mysqldump -u${src_username} -p${src_password} -h${src_url} ${src_db} | mysql -u${dst_username} -p${dst_password} $@ -D${dst_db}

exit 0