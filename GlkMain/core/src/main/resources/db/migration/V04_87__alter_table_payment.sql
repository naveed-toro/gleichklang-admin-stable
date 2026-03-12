alter table payment
add column revocation_date date default null,
add column revocation_amount decimal(10,2) default null;
