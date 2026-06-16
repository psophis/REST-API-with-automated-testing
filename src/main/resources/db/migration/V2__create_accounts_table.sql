create table accounts (
    id varchar(36) primary key,
    client_id varchar(36) not null,
    iban varchar(34) not null unique,
    balance numeric(19, 2) not null,
    account_type varchar(32) not null,
    created_at timestamp with time zone not null
);
