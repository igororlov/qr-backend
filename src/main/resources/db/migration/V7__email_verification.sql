alter table app_user
    add column email_verified_at timestamptz,
    add column email_verification_token_hash varchar(128),
    add column email_verification_expires_at timestamptz;

update app_user
set email_verified_at = created_at
where enabled = true;

create unique index uq_app_user_email_verification_token_hash
    on app_user(email_verification_token_hash)
    where email_verification_token_hash is not null;
