-- Autentificación
create table if not exists users
(
    id            uuid primary key                  default gen_random_uuid(),
    email         text                     not null unique,
    password_hash text                     not null,
    role          text                     not null,
    is_enabled    boolean                  not null default true,
    created_at    timestamp with time zone not null default now(),
    check (role in ('worker', 'admin', 'inspector'))
    );

create table if not exists profiles
(
    user_id        uuid primary key,
    full_name      text                     not null,
    employee_code  text                     null,
    is_active      boolean                  not null default true,
    preferences    jsonb                    null     default '{}'::jsonb,
    phone          text                     null,
    avatar_url     text                     null,
    is_first_login boolean                  not null default true,
    updated_at     timestamp with time zone not null default now(),
    weekly_hours   numeric                  null,
    position_id    uuid                     null,

    constraint fk_profiles_user
    foreign key (user_id)
    references users (id)
    on delete cascade
    );

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
    check (day_of_week between 0 and 6),
    check (start_time < end_time)
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
    check (status in ('open', 'closed'))
    );

create table if not exists daily_closures
(
    work_date     date primary key         not null,
    records_count integer                  not null,
    day_hash      text                     not null,
    prev_day_hash text                     null,
    computed_at   timestamp with time zone not null default now()
    );

create table if not exists audit_log
(
    id            uuid primary key         not null default gen_random_uuid(),
    table_name    text                     not null,
    record_id     uuid                     null,
    action        text                     not null,
    actor_user_id uuid                     null,
    actor_role    text                     null,
    reason        text                     null,
    old_data      jsonb                    null,
    new_data      jsonb                    null,
    created_at    timestamp with time zone not null default now(),
    check (action in ('INSERT', 'UPDATE', 'DELETE', 'SOFT_DELETE'))
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
alter table audit_log
    add constraint fk_audit_actor
        foreign key (actor_user_id)
            references users (id);



