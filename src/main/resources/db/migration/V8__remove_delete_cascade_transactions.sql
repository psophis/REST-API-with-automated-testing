alter table transactions
drop constraint fk_transactions_account;

alter table transactions
add constraint fk_transactions_account
foreign key (account_id)
references accounts(id);