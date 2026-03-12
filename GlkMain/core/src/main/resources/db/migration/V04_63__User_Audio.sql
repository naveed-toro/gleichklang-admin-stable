CREATE TABLE IF NOT EXISTS audio(
    id int(5) not null auto_increment,
    name varchar(255) unique  not null,
    change_date datetime,
    create_date datetime,
    deleted bit(1),
    avatar bit(1),
    audio_file longblob,
    user_id bigint(20) not null,
    unique key(id)
);

commit;