alter table qr_code
    add column if not exists text_color varchar(7) not null default '#1f2a2e';
