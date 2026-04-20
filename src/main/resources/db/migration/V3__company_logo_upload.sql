alter table company
    add column logo_content_type varchar(120),
    add column logo_bytes bytea,
    add column logo_uploaded_at timestamptz;
