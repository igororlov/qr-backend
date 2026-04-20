alter table qr_code
    add column qr_foreground_color varchar(7) not null default '#111111',
    add column qr_background_color varchar(7) not null default '#ffffff',
    add column qr_logo_enabled boolean not null default true,
    add column qr_image_png bytea,
    add column qr_image_generated_at timestamptz;
