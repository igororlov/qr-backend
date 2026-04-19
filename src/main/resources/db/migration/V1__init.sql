create table app_user (
    id uuid primary key,
    email varchar(320) not null unique,
    password_hash varchar(255) not null,
    full_name varchar(160) not null,
    role varchar(40) not null,
    enabled boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table company (
    id uuid primary key,
    name varchar(160) not null,
    slug varchar(120) not null unique,
    logo_url text,
    owner_user_id uuid not null references app_user(id),
    active boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table qr_code (
    id uuid primary key,
    company_id uuid not null references company(id),
    slug varchar(120) not null unique,
    title varchar(160) not null,
    subtitle varchar(240),
    label varchar(120),
    logo_url text,
    active boolean not null default true,
    scan_count bigint not null default 0,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table qr_action (
    id uuid primary key,
    qr_code_id uuid not null references qr_code(id) on delete cascade,
    position integer not null,
    label varchar(120) not null,
    type varchar(40) not null,
    value text not null,
    active boolean not null default true,
    click_count bigint not null default 0,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_qr_action_position unique (qr_code_id, position),
    constraint chk_qr_action_position check (position between 1 and 10)
);

create table qr_form_submission (
    id uuid primary key,
    qr_code_id uuid not null references qr_code(id) on delete cascade,
    sender_name varchar(160),
    sender_email varchar(320),
    sender_phone varchar(60),
    message text not null,
    created_at timestamptz not null
);

create index idx_company_owner_user_id on company(owner_user_id);
create index idx_qr_code_company_id on qr_code(company_id);
create index idx_qr_action_qr_code_id on qr_action(qr_code_id);
create index idx_qr_form_submission_qr_code_id on qr_form_submission(qr_code_id);
