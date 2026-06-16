alter table transactions
add constraint fk_transactions_account
foreign key (account_id)
references accounts(id)
on delete cascade;