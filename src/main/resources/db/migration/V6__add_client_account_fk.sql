alter table accounts
add constraint fk_accounts_client
foreign key (client_id)
references clients(id)
on delete cascade;