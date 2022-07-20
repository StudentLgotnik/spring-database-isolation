drop table if exists MESSAGE;

create table MESSAGE(
    id varchar not null unique,
    message varchar not null
);