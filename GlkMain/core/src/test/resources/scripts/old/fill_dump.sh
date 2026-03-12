#!/usr/bin/env bash

HOST=127.0.0.1

read -p "Are you sure you want to destroy the database on $HOST? [y/N] " -n 1 -r
echo # move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
    START=$(date +%s)
    echo "Recreating Database"
    mysql -ugleichklang -pgleichklang -h$HOST< recreate_database.sql

    if [ ! -z $1 ]
    then
       DUMP=$1
    else
       DUMP=gk_small.sql
    fi
    echo "Filling Data with $DUMP"
    mysql -ugleichklang -pgleichklang -h$HOST gleichklang < $DUMP

    echo "Сreating test user with credentials: valid@example.com:secret"
    mysql -ugleichklang -pgleichklang -h$HOST gleichklang < create_test_users.sql

    END=$(date +%s)
    DIFF=$(( $END - $START ))
    echo "It took $DIFF seconds"
fi
