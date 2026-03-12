#!/bin/bash

# usage:
#   export_db.sh <ADDITIONAL ARGS PASSED TO MYSQL>
#
# Example to export db from the host "theia"
#
#   export_dh.sh -h theia

## begin configuration
username=gleichklang
password=gleichklang
db_name=gleichklang

import_sql=${db_name}.sql.gz
## end configuration

tables=($(echo "SHOW TABLES;" | mysql -u${username} -p${password} $@ ${db_name} | grep -v '^comp' | tail -n+2))
mysqldump -u${username} -p${password} $@ ${db_name} ${tables[*]} | gzip > ${import_sql}

exit 0
