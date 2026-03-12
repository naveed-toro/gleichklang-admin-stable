#!/bin/bash

# usage:
#   create_small_dump.sh <ADDITIONAL ARGS PASSED TO MYSQL>
#

## begin configuration

username=gleichklang
password=gleichklang

src_db=gleichklang_qa
dst_db=gk_small

## end configuration

db_args="$@"
deactivate_foreign_keys="SET foreign_key_checks = 0;"
activate_foreign_keys="SET foreign_key_checks = 1;"

execute_query()
{
    echo "execute: $1"
    echo "${deactivate_foreign_keys}$1${activate_foreign_keys}" | mysql -u${username} -p${password} ${db_args}
}

execute_insert_query()
{
    if [ $# -gt 1 ]
    then
        execute_query "INSERT INTO ${dst_db}.$1 SELECT * FROM ${src_db}.$1 WHERE $2;"
    else
        execute_query "INSERT INTO ${dst_db}.$1 SELECT * FROM ${src_db}.$1;"
    fi
}

execute_insert_query_consider_ref()
{
    table=$1
    ref_column="user_id"
    ref_table="user_"

    if [ $# -gt 1 ]
    then
        ref_column=$2
    fi

    if [ $# -gt 2 ]
    then
        ref_table=$3
    fi

    execute_query "INSERT INTO ${dst_db}.${table} SELECT * FROM ${src_db}.${table} WHERE ${src_db}.${table}.${ref_column} IS NULL OR EXISTS (SELECT 1 FROM ${dst_db}.${ref_table} WHERE ${dst_db}.${ref_table}.id = ${src_db}.${table}.${ref_column});"
}

execute_query "DROP DATABASE IF EXISTS ${dst_db}; CREATE DATABASE ${dst_db};"
tables=($(echo "SHOW TABLES;" | mysql -u${username} -p${password} $@ ${src_db} | grep -v '^comp' | tail -n+2))
mysqldump -u${username} -p${password} -d $@ ${src_db} ${tables[*]} | mysql -u${username} -p${password} $@ -D${dst_db}

execute_insert_query "schema_version"

execute_insert_query "i18n"

execute_insert_query "choice_group"
execute_insert_query "choice"
execute_insert_query "questionnaire"
execute_insert_query "question_group"
execute_insert_query "question"

execute_insert_query "matching_matrix"
execute_insert_query "matching_matrix_value"
execute_insert_query "activator"
execute_insert_query "activator_choice"
execute_insert_query "questions_mapping"
execute_insert_query "affinity_question"

execute_insert_query "locatable"

execute_insert_query "user_" "member_status = 'REGISTERED' LIMIT 1000"
execute_insert_query "user_" "DTYPE = 'Admin'"
execute_insert_query_consider_ref "address"
execute_insert_query_consider_ref "user_activity_log"
execute_insert_query_consider_ref "after_cancel"
execute_insert_query_consider_ref "recommendation_break"
execute_insert_query_consider_ref "user_payment_settings"
execute_insert_query_consider_ref "user_recommendation_category"
execute_insert_query_consider_ref "user_registration_state"
execute_insert_query_consider_ref "user_settings"
execute_insert_query_consider_ref "answer"
execute_insert_query_consider_ref "proximity_search_request" "answer_id" "answer"
execute_insert_query_consider_ref "region_search_request" "answer_id" "answer"
execute_insert_query_consider_ref "region_search_request_restriction" "region_search_request_id" "region_search_request"
execute_insert_query "admin_roles"

execute_query "INSERT INTO ${dst_db}.message SELECT m.* FROM ${src_db}.message m LEFT JOIN ${src_db}.envelope s ON m.id = s.message_id AND s.DTYPE = 'SenderEnvelope' LEFT JOIN ${src_db}.envelope r ON m.id = r.message_id AND r.DTYPE = 'ReceiverEnvelope' WHERE (s.user_id IS NULL OR EXISTS (SELECT 1 FROM ${dst_db}.user_ u WHERE u.id = s.user_id)) AND (r.user_id IS NULL OR EXISTS (SELECT 1 FROM ${dst_db}.user_ u WHERE u.id = r.user_id));"
execute_insert_query_consider_ref "envelope" "message_id" "message"
execute_insert_query_consider_ref "admin_work_item" "message_id" "message"

execute_query "INSERT INTO ${dst_db}.relationship SELECT r.* FROM ${src_db}.relationship r WHERE EXISTS (SELECT 1 FROM ${dst_db}.user_ u WHERE u.id = r.source_user_id) AND EXISTS (SELECT 1 FROM ${dst_db}.user_ u WHERE u.id = r.target_user_id);"
execute_insert_query_consider_ref "relationship_category" "relationship_id" "relationship"

execute_insert_query "bank_account"
execute_insert_query "product"
execute_insert_query "subscription_offer_category"
execute_insert_query "service_offer_category"
execute_insert_query "upgrade_offer_subscription_offer"
execute_insert_query_consider_ref "subscription"
execute_insert_query_consider_ref "invoice"
execute_insert_query_consider_ref "invoice_item" "invoice_id" "invoice"
execute_insert_query_consider_ref "payment"
execute_insert_query_consider_ref "affiliate_payment_state" "payment_id" "payment"

execute_insert_query "filter"
execute_insert_query "template_context"
execute_insert_query "news"
execute_insert_query_consider_ref "user_news"

execute_query "SET @i = 0;UPDATE ${dst_db}.user_ SET email = CONCAT('valid', @i := @i + 1, '@example.com'), password = '\$2a\$10\$PmRQaI1jLFn9vinSV2KXA.qTWdgHxJ1WbKtx90gh/n5am4XGRyFee', alias = CONCAT('marvin', @i), first_name = CONCAT('Bob', @i), last_name = 'Baumeister' WHERE DTYPE <> 'Admin';"

exit 0