#!/bin/bash

# usage:
#   anonymizer.sh <ADDITIONAL ARGS PASSED TO MYSQL>
#

## begin configuration

username=gleichklang
password=gleichklang

db=gleichklang_qa

## end configuration

db_args="$@"
deactivate_foreign_keys="SET foreign_key_checks = 0;"
activate_foreign_keys="SET foreign_key_checks = 1;"

execute_query()
{
    echo "execute: $1"
    echo "${deactivate_foreign_keys}$1${activate_foreign_keys}" | mysql -u${username} -p${password} ${db_args}
}

execute_anonymize()
{
    table=$1
    column=$2
    signs=10
    tail=""
    where=""

    if [ $# -gt 2 ]
    then
        signs=$3
    fi

    if [ $# -gt 3 ]
    then
        tail=$4
    fi

    if [ $# -gt 4 ]
    then
        where=$5
    fi

    if [ -n "$tail" ]
    then
       tail=", ${tail}"
    fi

    if [ -n "$where" ]
    then
       where=" AND ${where}"
    fi

    execute_query "UPDATE ${db}.${table} SET ${column} = LOWER(CONCAT(RIGHT(password(${column}), ${signs})${tail})) WHERE ${column} IS NOT NULL AND ${column} != ''${where};"
}

execute_anonymize "user_" "alias" 10 "" "DTYPE <> 'Admin'"
execute_anonymize "user_" "email" 10 "@example.com" "DTYPE <> 'Admin' AND email IS NOT NULL"
execute_anonymize "address" "streetWithNumber" 8
execute_query "SET @i = 0;UPDATE ${db}.user_ SET email = CONCAT('valid', @i := @i + 1, '@example.com'), password = '\$2a\$10\$PmRQaI1jLFn9vinSV2KXA.qTWdgHxJ1WbKtx90gh/n5am4XGRyFee', alias = CONCAT('marvin', @i), first_name = CONCAT('Bob', @i), last_name = 'Baumeister' WHERE DTYPE <> 'Admin';"

exit 0