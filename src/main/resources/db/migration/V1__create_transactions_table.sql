create table transactions (
    id varchar(36) primary key,
    account_id varchar(36) not null,
    sender_iban varchar(34) not null,
    recipient_iban varchar(34) not null,
    amount numeric(19, 2) not null,
    transaction_type varchar(32) not null,
    created_at timestamp with time zone not null
);
