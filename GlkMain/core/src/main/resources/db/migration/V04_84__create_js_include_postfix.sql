CREATE TABLE IF NOT EXISTS js_include_postfix (
 id bigint(20) not null auto_increment,
 change_date datetime default null,
 create_date datetime default null,
 key_name varchar(12) not null,
 primary key (id)
 );