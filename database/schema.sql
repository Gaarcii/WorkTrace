create extension if not exists pgcrypto;

-- Autentificación
create table if not exists users
(
    id            uuid primary key                  default gen_random_uuid(),
    email         text                     not null unique,
    password_hash text                     not null,
    role          text                     not null,
    is_enabled    boolean                  not null default true,
    created_at    timestamp with time zone not null default now(),
    check (role in ('WORKER', 'ADMIN', 'INSPECTOR'))
);

create table if not exists profiles
(
    user_id        uuid primary key,
    full_name      text                     not null,
    employee_code  text                     not null,
    is_active      boolean                  not null default true,
    preferences    jsonb                    null     default '{}'::jsonb,
    phone          text                     null,
    avatar_url     text                     null,
    is_first_login boolean                  not null default true,
    updated_at     timestamp with time zone not null default now(),
    weekly_hours   numeric                  null,
    position_id    uuid                     null
);
--Relación perfiles-usuarios
alter table profiles
    add constraint fk_profiles_user
        foreign key (user_id)
            references users (id)
            on delete cascade;

--Organización de la empresa
create table if not exists company_settings
(
    id           uuid primary key                  default gen_random_uuid(),
    company_name text                     null,
    cif          text                     null,
    logo_url     text                     null,
    updated_at   timestamp with time zone not null default now(),
    is_singleton boolean                  not null default true
);

create unique index ux_company_settings_singleton
    on company_settings (is_singleton)
    where is_singleton = true;

create table if not exists job_positions
(
    id         uuid primary key                  default gen_random_uuid(),
    title      text                     not null,
    created_at timestamp with time zone not null default now()
);

create table if not exists work_sites
(
    id         uuid primary key                  default gen_random_uuid(),
    name       text                     not null,
    address    text                     not null,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

create table if not exists work_schedules
(
    id          uuid primary key                  default gen_random_uuid(),
    employee_id uuid                     not null,
    site_id     uuid                     not null,
    day_of_week integer                  not null,
    start_time  time without time zone   not null,
    end_time    time without time zone   not null,
    created_at  timestamp with time zone not null default now(),
    updated_at  timestamp with time zone not null default now(),
    check (day_of_week between 0 and 6)
);

--Relación perfiles-puesto de trabajo
alter table profiles
    add constraint fk_profiles_position
        foreign key (position_id)
            references job_positions (id);

--Relación horario-perfil
alter table work_schedules
    add constraint fk_work_schedules_employee
        foreign key (employee_id)
            references profiles (user_id)
            on delete cascade;

--Relación horario-ubicación trabajo
alter table work_schedules
    add constraint fk_work_schedules_site
        foreign key (site_id)
            references work_sites (id);

--Sistema de fichajes
create table if not exists time_entries
(
    id                  uuid primary key         not null default gen_random_uuid(),
    employee_id         uuid                     not null,
    work_date           date                     not null,
    start_at            timestamp with time zone not null,
    end_at              timestamp with time zone null,
    start_lat           numeric(9, 6)            not null,
    start_lng           numeric(9, 6)            not null,
    end_lat             numeric(9, 6)            null,
    end_lng             numeric(9, 6)            null,
    start_accuracy_m    integer                  not null,
    end_accuracy_m      integer                  null,
    start_ip            inet                     not null,
    end_ip              inet                     null,
    start_user_agent    text                     not null,
    end_user_agent      text                     null,
    start_geoip         jsonb                    null,
    end_geoip           jsonb                    null,
    flags               text[]                   not null default '{}'::text[],
    status              text                     not null,
    deleted_at          timestamp with time zone null,
    deleted_by          uuid                     null,
    delete_reason       text                     null,
    created_at          timestamp with time zone not null default now(),
    created_by          uuid                     not null,
    modification_reason text                     null,
    check (status in ('OPEN', 'CLOSED')),
    check (
        (deleted_at is null and deleted_by is null and delete_reason is null)
            or
        (deleted_at is not null and deleted_by is not null and delete_reason is not null)
        )
);

create table if not exists daily_closures
(
    work_date     date primary key         not null,
    records_count integer                  not null,
    day_hash      text                     not null,
    prev_day_hash text                     null,
    computed_at   timestamp with time zone not null default now()
);

create table if not exists audit_time_entries
(
    id            uuid primary key                  default gen_random_uuid(),
    time_entry_id uuid                     not null,
    action        text                     not null,
    actor_user_id uuid                     not null,
    reason        text                     not null,
    old_data      jsonb,
    new_data      jsonb,
    created_at    timestamp with time zone not null default now(),

    check (action in ('ADMIN_ADJUST', 'SOFT_DELETE'))
);

--Relación fichajes-perfiles
alter table time_entries
    add constraint fk_time_entries_employee
        foreign key (employee_id)
            references profiles (user_id);

--Relación fichajes-usuarios (created_by)
alter table time_entries
    add constraint fk_time_entries_created_by
        foreign key (created_by)
            references users (id);

--Relación fichajes-usuarios (deleted_by)
alter table time_entries
    add constraint fk_time_entries_deleted_by
        foreign key (deleted_by)
            references users (id);

--Relación registro auditoría-usuario
alter table audit_time_entries
    add constraint fk_audit_actor
        foreign key (actor_user_id)
            references users (id);

--Incidencais y antifraude
create table if not exists incidents
(
    id             uuid primary key         not null default gen_random_uuid(),
    user_id        uuid                     not null,
    date           date                     not null,
    comment        text                     not null,
    status         text                     not null default 'Pendiente'::text,
    admin_response text                     null,
    created_at     timestamp with time zone not null default now(),
    updated_at     timestamp with time zone not null default now(),
    resolved_by    uuid                     null,
    type_id        uuid                     not null,
    incident_time  time without time zone   not null,
    check (status in ('PENDING', 'RESOLVED', 'REJECTED'))
);

create table if not exists incidence_types
(
    id         uuid primary key                  default gen_random_uuid(),
    name       text                     not null unique,
    created_at timestamp with time zone not null default now()
);

create table if not exists known_ips
(
    ip         inet primary key,
    geoip_data jsonb                    not null,
    created_at timestamp with time zone not null default now()
);

--Relación incidencias-tipo de incidencia
alter table incidents
    add constraint fk_incidents_type
        foreign key (type_id)
            references incidence_types (id);

--Relación incidencias-perfil
alter table incidents
    add constraint fk_incidents_profile
        foreign key (user_id)
            references profiles (user_id);

--Relación incidencias-usuario
alter table incidents
    add constraint fk_incidents_resolved_by
        foreign key (resolved_by)
            references users (id);