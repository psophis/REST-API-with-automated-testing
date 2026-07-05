alter table accounts
drop constraint fk_accounts_client;

alter table accounts
add constraint fk_accounts_client
foreign key (client_id)
references clients(id);