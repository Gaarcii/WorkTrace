create extension if not exists pgcrypto;

-- Tabla de empresas (La raíz del SaaS)
create table if not exists companies (
    id uuid primary key default gen_random_uuid (),
    company_name text unique not null,
    cif text unique not null,
    logo_url text null,
    updated_at timestamp with time zone not null default now()
);

-- Autentificación
create table if not exists users (
    id uuid primary key default gen_random_uuid (),
    email text not null unique,
    password_hash text not null,
    role text not null,
    is_enabled boolean not null default true,
    created_at timestamp with time zone not null default now(),
    company_id uuid not null,
    check (
        role in (
            'WORKER',
            'ADMIN',
            'INSPECTOR'
        )
    )
);

create table if not exists profiles (
    user_id uuid primary key,
    full_name text not null,
    employee_code text unique not null,
    is_active boolean not null default true,
    phone text not null,
    avatar_url text null,
    is_first_login boolean not null default true,
    updated_at timestamp with time zone not null default now(),
    weekly_hours numeric null,
    position_id uuid null
);

create table if not exists job_positions (
    id uuid primary key default gen_random_uuid (),
    title text not null,
    created_at timestamp with time zone not null default now(),
    company_id uuid not null,
    unique (title, company_id) -- Único por empresa
);

create table if not exists work_sites (
    id uuid primary key default gen_random_uuid (),
    name text not null,
    address text not null,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),
    company_id uuid not null
);

create table if not exists work_schedules (
    id uuid primary key default gen_random_uuid (),
    employee_id uuid not null,
    site_id uuid not null,
    day_of_week varchar not null,
    start_time time without time zone not null,
    end_time time without time zone not null,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),
    company_id uuid not null,
    CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'))
);

--Sistema de fichajes
create table if not exists time_entries (
    id uuid primary key not null default gen_random_uuid (),
    employee_id uuid not null,
    work_date date not null,
    start_at timestamp with time zone not null,
    end_at timestamp with time zone null,
    start_lat numeric(9, 6) not null,
    start_lng numeric(9, 6) not null,
    end_lat numeric(9, 6) null,
    end_lng numeric(9, 6) null,
    start_accuracy_m integer not null,
    end_accuracy_m integer null,
    start_ip inet not null,
    end_ip inet null,
    start_user_agent text not null,
    end_user_agent text null,
    start_geoip jsonb null,
    end_geoip jsonb null,
    flags text [] not null default '{}'::text [],
    status text not null,
    deleted_at timestamp with time zone null,
    deleted_by uuid null,
    delete_reason text null,
    created_at timestamp with time zone not null default now(),
    created_by uuid not null,
    updated_at timestamp with time zone null,
    modification_reason text null,
    company_id uuid not null,
    check (status in ('OPEN', 'CLOSED')),
    check (
        (
            deleted_at is null
            and deleted_by is null
            and delete_reason is null
        )
        or (
            deleted_at is not null
            and deleted_by is not null
            and delete_reason is not null
        )
    )
);

create table if not exists daily_closures (
    work_date date primary key not null,
    records_count integer not null,
    day_hash text not null,
    prev_day_hash text null,
    company_id uuid not null,
    computed_at timestamp with time zone not null default now()
);

create table if not exists audit_time_entries (
    id uuid primary key default gen_random_uuid (),
    time_entry_id uuid not null,
    action text not null,
    actor_user_id uuid not null,
    reason text not null,
    old_data jsonb,
    new_data jsonb,
    created_at timestamp with time zone not null default now(),
    company_id uuid not null,
    check (
        action in ('ADMIN_ADJUST', 'SOFT_DELETE')
    )
);

--Incidencias y antifraude
create table if not exists incidences (
    id uuid primary key not null default gen_random_uuid (),
    user_id uuid not null,
    date date not null,
    comment text not null,
    status text not null default 'PENDING'::text,
    admin_response text null,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),
    resolved_by uuid null,
    type_id uuid not null,
    incidence_time time without time zone not null,
    company_id uuid not null,
    check (
        status in (
            'PENDING',
            'RESOLVED',
            'REJECTED'
        )
    )
);

create table if not exists incidence_types (
    id uuid primary key default gen_random_uuid (),
    name text not null,
    created_at timestamp with time zone not null default now(),
    company_id uuid not null,
    unique (name, company_id)
);

create table if not exists known_ips (
    ip inet primary key,
    geoip_data jsonb not null,
    created_at timestamp with time zone not null default now()
);

alter table users
add constraint fk_users_company foreign key (company_id) references companies (id);

alter table job_positions
add constraint fk_positions_company foreign key (company_id) references companies (id);

alter table work_sites
add constraint fk_sites_company foreign key (company_id) references companies (id);

alter table audit_time_entries
add constraint fk_audit_company foreign key (company_id) references companies (id);

alter table work_schedules
add constraint fk_schedules_company foreign key (company_id) references companies (id);

alter table time_entries
add constraint fk_entries_company foreign key (company_id) references companies (id);

alter table incidences
add constraint fk_incidences_company foreign key (company_id) references companies (id);

alter table incidence_types
add constraint fk_inc_types_company foreign key (company_id) references companies (id);

alter table daily_closures
add constraint fk_closures_company foreign key (company_id) references companies (id);


alter table profiles
add constraint fk_profiles_user foreign key (user_id) references users (id) on delete cascade;

alter table profiles
add constraint fk_profiles_position foreign key (position_id) references job_positions (id);

alter table work_schedules
add constraint fk_work_schedules_employee foreign key (employee_id) references profiles (user_id) on delete cascade;

alter table work_schedules
add constraint fk_work_schedules_site foreign key (site_id) references work_sites (id);

alter table time_entries
add constraint fk_time_entries_employee foreign key (employee_id) references profiles (user_id);

alter table time_entries
add constraint fk_time_entries_created_by foreign key (created_by) references users (id);

alter table time_entries
add constraint fk_time_entries_deleted_by foreign key (deleted_by) references users (id);

alter table audit_time_entries
add constraint fk_audit_actor foreign key (actor_user_id) references users (id);

alter table incidences
add constraint fk_incidences_type foreign key (type_id) references incidence_types (id);

alter table incidences
add constraint fk_incidences_profile foreign key (user_id) references profiles (user_id);

alter table incidences
add constraint fk_incidences_resolved_by foreign key (resolved_by) references users (id);