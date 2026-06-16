create table clients (
    id varchar(36) primary key,
    last_name varchar(50) not null,
    first_name varchar(50) not null,
    street varchar(60) not null,
    number varchar(12) not null,
    city varchar(60) not null,
    zip_code varchar(12) not null
);
