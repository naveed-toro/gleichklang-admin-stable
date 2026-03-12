alter table client_information
add column last_login BIT(1) default 0;
commit;