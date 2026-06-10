create table qr_scan_event (
    id uuid primary key,
    qr_code_id uuid not null references qr_code(id) on delete cascade,
    visitor_id varchar(100),
    unique_visitor boolean not null default false,
    ip_address varchar(45),
    user_agent text,
    scanned_at timestamptz not null
);

create table qr_action_click_event (
    id uuid primary key,
    qr_code_id uuid not null references qr_code(id) on delete cascade,
    qr_action_id uuid not null references qr_action(id) on delete cascade,
    visitor_id varchar(100),
    ip_address varchar(45),
    user_agent text,
    clicked_at timestamptz not null
);

create index idx_qr_scan_event_qr_code_id on qr_scan_event(qr_code_id);
create index idx_qr_scan_event_qr_code_visitor on qr_scan_event(qr_code_id, visitor_id);
create index idx_qr_scan_event_scanned_at on qr_scan_event(scanned_at);
create index idx_qr_action_click_event_qr_code_id on qr_action_click_event(qr_code_id);
create index idx_qr_action_click_event_action_id on qr_action_click_event(qr_action_id);
create index idx_qr_action_click_event_visitor on qr_action_click_event(qr_code_id, visitor_id);
create index idx_qr_action_click_event_clicked_at on qr_action_click_event(clicked_at);
