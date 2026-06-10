alter table qr_scan_event
    add column device_type varchar(40),
    add column country_code varchar(2),
    add column country_name varchar(120),
    add column region varchar(160),
    add column city varchar(160),
    add column latitude double precision,
    add column longitude double precision,
    add column timezone varchar(120);

alter table qr_action_click_event
    add column device_type varchar(40),
    add column country_code varchar(2),
    add column country_name varchar(120),
    add column region varchar(160),
    add column city varchar(160),
    add column latitude double precision,
    add column longitude double precision,
    add column timezone varchar(120);

create index idx_qr_scan_event_country_code on qr_scan_event(country_code);
create index idx_qr_action_click_event_country_code on qr_action_click_event(country_code);
