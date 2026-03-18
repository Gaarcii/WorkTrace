-- =============================================================================
-- seed.sql — Datos de prueba para backend Spring Boot
-- Contraseña de todos los usuarios: 123456
--
-- ┌─ FILOSOFÍA DE FECHAS ──────────────────────────────────────────────────────┐
-- │ Todas las fechas son RELATIVAS a now(). Ejecuta este script hoy, dentro   │
-- │ de 8 meses o en 2 años: los datos siempre aparecerán como "recientes".    │
-- │                                                                            │
-- │  Anclas (calculadas en el DO $$):                                         │
-- │    w0 = lunes de hace 2 semanas  → histórico lejano                       │
-- │    w1 = lunes de la semana pasada → histórico reciente                    │
-- │    hoy = CURRENT_DATE             → turno activo (OPEN)                   │
-- └────────────────────────────────────────────────────────────────────────────┘
--
-- ┌─ ANOMALÍAS Y PISTAS DE FRAUDE SEMBRADAS ──────────────────────────────────┐
-- │  A) IP_LOCATION_MISMATCH  — Carlos ficha desde una IP de Madrid siendo    │
-- │     su centro en Pozuelo (w0 Vie y w1 Lun). Auditado 2× por inspector    │
-- │     y 1× por admin (escalado disciplinario). Expediente: EXP-CARLOS-001. │
-- │                                                                            │
-- │  B) GPS_LOW_ACCURACY      — Roberto Mié w0 (250 m) y Sergio Mié w1       │
-- │     (250 m). Revisados post-turno por inspector. 2× ADMIN_ADJUST.         │
-- │                                                                            │
-- │  C) REPEATED_ADMIN_EDIT   — Roberto olvida fichar salida el Jue de w0    │
-- │     Y el Jue de w1 (2 semanas seguidas). Patrón sospechoso documentado    │
-- │     en el audit #6 con alerta REPEATED_PATTERN_DETECTED.                  │
-- │                                                                            │
-- │  D) SOFT_DELETE           — Roberto duplicado (Mar w0) y Laura prueba     │
-- │     fallida (Lun w1). Ambos con audit SOFT_DELETE.                        │
-- │                                                                            │
-- │  E) OUT_OF_HOURS          — Sergio ficha 45 min ANTES de su turno en      │
-- │     Jue w1 (15:15 en vez de 16:00). Auditado por inspector.               │
-- └────────────────────────────────────────────────────────────────────────────┘
--
-- ┌─ RESUMEN DE ENTIDADES ─────────────────────────────────────────────────────┐
-- │  companies           1                                                     │
-- │  job_positions       8                                                     │
-- │  work_sites          4                                                     │
-- │  incidence_types     6                                                     │
-- │  users               6  (1 ADMIN · 1 INSPECTOR · 4 WORKER)  pass: 123456  │
-- │  profiles            6                                                     │
-- │  known_ips           7  (incluye IP sospechosa de Carlos)                  │
-- │  work_schedules     15  (Roberto×5 · Sergio×5 · Laura×2 · Carlos×3)       │
-- │  time_entries       ~34                                                    │
-- │    CLOSED laborales: 20 · CLOSED finde: 4 · CLOSED soft-delete: 2        │
-- │    OPEN: 2 · Flags: GPS_LOW_ACCURACY×2, ADMIN_EDITED×2,                   │
-- │            IP_LOCATION_MISMATCH×2, OUT_OF_HOURS×1                         │
-- │  audit_time_entries 10                                                     │
-- │    SOFT_DELETE ×2 · ADMIN_ADJUST ×8                                       │
-- │  incidents           5  (2 RESOLVED · 1 REJECTED · 2 PENDING)             │
-- └────────────────────────────────────────────────────────────────────────────┘
-- =============================================================================

-- -----------------------------------------------------------------------------
-- BLOQUE 0: LIMPIEZA PREVIA (orden inverso de dependencias)
-- -----------------------------------------------------------------------------
TRUNCATE TABLE audit_time_entries CASCADE;

TRUNCATE TABLE daily_closures CASCADE;

TRUNCATE TABLE incidents CASCADE;

TRUNCATE TABLE time_entries CASCADE;

TRUNCATE TABLE work_schedules CASCADE;

TRUNCATE TABLE known_ips CASCADE;

TRUNCATE TABLE profiles CASCADE;

TRUNCATE TABLE users CASCADE;

TRUNCATE TABLE incidence_types CASCADE;

TRUNCATE TABLE job_positions CASCADE;

TRUNCATE TABLE work_sites CASCADE;

TRUNCATE TABLE companies CASCADE;

-- -----------------------------------------------------------------------------
-- BLOQUE 1: EMPRESA
-- -----------------------------------------------------------------------------
INSERT INTO
    companies (
        id,
        company_name,
        cif,
        logo_url,
        updated_at
    )
VALUES (
        'a0000000-0000-0000-0000-000000000001',
        'Fichajes Online S.L.',
        'B12345678',
        NULL,
        now()
    );

-- -----------------------------------------------------------------------------
-- BLOQUE 2: PUESTOS DE TRABAJO
-- -----------------------------------------------------------------------------
INSERT INTO
    job_positions (
        id,
        title,
        company_id,
        created_at
    )
VALUES (
        'b0000000-0000-0000-0000-000000000001',
        'Conserje',
        'a0000000-0000-0000-0000-000000000001',
        now()
    ),
    (
        'b0000000-0000-0000-0000-000000000002',
        'Portero de Finca',
        'a0000000-0000-0000-0000-000000000001',
        now()
    ),
    (
        'b0000000-0000-0000-0000-000000000003',
        'Socorrista',
        'a0000000-0000-0000-0000-000000000001',
        now()
    ),
    (
        'b0000000-0000-0000-0000-000000000004',
        'Jardinero',
        'a0000000-0000-0000-0000-000000000001',
        now()
    ),
    (
        'b0000000-0000-0000-0000-000000000005',
        'Personal de Limpieza',
        'a0000000-0000-0000-0000-000000000001',
        now()
    ),
    (
        'b0000000-0000-0000-0000-000000000006',
        'Tecnico de Mantenimiento',
        'a0000000-0000-0000-0000-000000000001',
        now()
    ),
    (
        'b0000000-0000-0000-0000-000000000007',
        'Jefe',
        'a0000000-0000-0000-0000-000000000001',
        now()
    ),
    (
        'b0000000-0000-0000-0000-000000000008',
        'Auditor',
        'a0000000-0000-0000-0000-000000000001',
        now()
    );

-- -----------------------------------------------------------------------------
-- BLOQUE 3: CENTROS DE TRABAJO
-- -----------------------------------------------------------------------------
INSERT INTO
    work_sites (
        id,
        name,
        address,
        company_id,
        created_at,
        updated_at
    )
VALUES (
        'c0000000-0000-0000-0000-000000000001',
        'Urbanizacion Las Encinas',
        'Av. Principal 45, Madrid',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    ),
    (
        'c0000000-0000-0000-0000-000000000002',
        'Comunidad Propietarios Los Rosales',
        'C/ de la Rosa 12, Pozuelo',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    ),
    (
        'c0000000-0000-0000-0000-000000000003',
        'Piscina Municipal Distrito 5',
        'Plaza del Deporte s/n',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    ),
    (
        'c0000000-0000-0000-0000-000000000004',
        'Oficina Central (Base)',
        'Poligono Industrial Sur, Nave 3',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    );

-- -----------------------------------------------------------------------------
-- BLOQUE 4: TIPOS DE INCIDENCIA
-- -----------------------------------------------------------------------------
INSERT INTO
    incidence_types (
        id,
        name,
        company_id,
        created_at
    )
VALUES (
        'd0000000-0000-0000-0000-000000000001',
        'Sustitucion Urgente (Baja/Ausencia)',
        'a0000000-0000-0000-0000-000000000001',
        now()
    ),
    (
        'd0000000-0000-0000-0000-000000000002',
        'Falta de Material/Uniforme',
        'a0000000-0000-0000-0000-000000000001',
        now()
    ),
    (
        'd0000000-0000-0000-0000-000000000003',
        'Averia en Instalaciones (Riego/Puertas)',
        'a0000000-0000-0000-0000-000000000001',
        now()
    ),
    (
        'd0000000-0000-0000-0000-000000000004',
        'Incidente con Vecino/Usuario',
        'a0000000-0000-0000-0000-000000000001',
        now()
    ),
    (
        'd0000000-0000-0000-0000-000000000005',
        'Accidente Laboral',
        'a0000000-0000-0000-0000-000000000001',
        now()
    ),
    (
        'd0000000-0000-0000-0000-000000000006',
        'Olvido de Llaves',
        'a0000000-0000-0000-0000-000000000001',
        now()
    );

-- -----------------------------------------------------------------------------
-- BLOQUE 5: USUARIOS + PERFILES
--
-- BCrypt de "123456" (coste 10, compatible con Spring Security BCryptPasswordEncoder):
--   $2a$12$jkF.3spmeNPzG269c0fUn.5pUTDoBO/ZJm/vkln4Kf.G.x8ukSfUC
-- -----------------------------------------------------------------------------

INSERT INTO
    users (
        id,
        email,
        password_hash,
        role,
        is_enabled,
        company_id,
        created_at
    )
VALUES (
        'e0000000-0000-0000-0000-000000000001',
        'admin@test.com',
        '$2a$12$jkF.3spmeNPzG269c0fUn.5pUTDoBO/ZJm/vkln4Kf.G.x8ukSfUC',
        'ADMIN',
        true,
        'a0000000-0000-0000-0000-000000000001',
        now()
    );
-- password: 123456

INSERT INTO
    profiles (
        user_id,
        full_name,
        employee_code,
        is_active,
        phone,
        avatar_url,
        is_first_login,
        weekly_hours,
        position_id,
        updated_at
    )
VALUES (
        'e0000000-0000-0000-0000-000000000001',
        'Paco Garcia',
        'ADMIN-001',
        true,
        '+34 600 000 001',
        NULL,
        false,
        40.0,
        'b0000000-0000-0000-0000-000000000007',
        now()
    );

INSERT INTO
    users (
        id,
        email,
        password_hash,
        role,
        is_enabled,
        company_id,
        created_at
    )
VALUES (
        'e0000000-0000-0000-0000-000000000002',
        'inspector@test.com',
        '$2a$12$jkF.3spmeNPzG269c0fUn.5pUTDoBO/ZJm/vkln4Kf.G.x8ukSfUC',
        'INSPECTOR',
        true,
        'a0000000-0000-0000-0000-000000000001',
        now()
    );
-- password: 123456

INSERT INTO
    profiles (
        user_id,
        full_name,
        employee_code,
        is_active,
        phone,
        avatar_url,
        is_first_login,
        weekly_hours,
        position_id,
        updated_at
    )
VALUES (
        'e0000000-0000-0000-0000-000000000002',
        'Mario Rodriguez',
        'INSP-001',
        true,
        '+34 600 000 002',
        NULL,
        false,
        40.0,
        'b0000000-0000-0000-0000-000000000008',
        now()
    );

INSERT INTO
    users (
        id,
        email,
        password_hash,
        role,
        is_enabled,
        company_id,
        created_at
    )
VALUES (
        'e0000000-0000-0000-0000-000000000003',
        'roberto.conserje@test.com',
        '$2a$12$jkF.3spmeNPzG269c0fUn.5pUTDoBO/ZJm/vkln4Kf.G.x8ukSfUC',
        'WORKER',
        true,
        'a0000000-0000-0000-0000-000000000001',
        now()
    );
-- password: 123456

INSERT INTO
    profiles (
        user_id,
        full_name,
        employee_code,
        is_active,
        phone,
        avatar_url,
        is_first_login,
        weekly_hours,
        position_id,
        updated_at
    )
VALUES (
        'e0000000-0000-0000-0000-000000000003',
        'Roberto Martinez',
        '12345678A',
        true,
        '+34 600 111 222',
        NULL,
        true,
        40.0,
        'b0000000-0000-0000-0000-000000000001',
        now()
    );

INSERT INTO
    users (
        id,
        email,
        password_hash,
        role,
        is_enabled,
        company_id,
        created_at
    )
VALUES (
        'e0000000-0000-0000-0000-000000000004',
        'laura.socorrista@test.com',
        '$2a$12$jkF.3spmeNPzG269c0fUn.5pUTDoBO/ZJm/vkln4Kf.G.x8ukSfUC',
        'WORKER',
        true,
        'a0000000-0000-0000-0000-000000000001',
        now()
    );
-- password: 123456

INSERT INTO
    profiles (
        user_id,
        full_name,
        employee_code,
        is_active,
        phone,
        avatar_url,
        is_first_login,
        weekly_hours,
        position_id,
        updated_at
    )
VALUES (
        'e0000000-0000-0000-0000-000000000004',
        'Laura Gomez',
        '87654321B',
        true,
        '+34 600 333 444',
        NULL,
        true,
        35.0,
        'b0000000-0000-0000-0000-000000000003',
        now()
    );

INSERT INTO
    users (
        id,
        email,
        password_hash,
        role,
        is_enabled,
        company_id,
        created_at
    )
VALUES (
        'e0000000-0000-0000-0000-000000000005',
        'carlos.jardinero@test.com',
        '$2a$12$jkF.3spmeNPzG269c0fUn.5pUTDoBO/ZJm/vkln4Kf.G.x8ukSfUC',
        'WORKER',
        true,
        'a0000000-0000-0000-0000-000000000001',
        now()
    );
-- password: 123456

INSERT INTO
    profiles (
        user_id,
        full_name,
        employee_code,
        is_active,
        phone,
        avatar_url,
        is_first_login,
        weekly_hours,
        position_id,
        updated_at
    )
VALUES (
        'e0000000-0000-0000-0000-000000000005',
        'Carlos Ruiz',
        '11223344C',
        true,
        '+34 600 555 666',
        NULL,
        true,
        40.0,
        'b0000000-0000-0000-0000-000000000004',
        now()
    );

INSERT INTO
    users (
        id,
        email,
        password_hash,
        role,
        is_enabled,
        company_id,
        created_at
    )
VALUES (
        'e0000000-0000-0000-0000-000000000006',
        'sergio.conserje@test.com',
        '$2a$12$jkF.3spmeNPzG269c0fUn.5pUTDoBO/ZJm/vkln4Kf.G.x8ukSfUC',
        'WORKER',
        true,
        'a0000000-0000-0000-0000-000000000001',
        now()
    );
-- password: 123456

INSERT INTO
    profiles (
        user_id,
        full_name,
        employee_code,
        is_active,
        phone,
        avatar_url,
        is_first_login,
        weekly_hours,
        position_id,
        updated_at
    )
VALUES (
        'e0000000-0000-0000-0000-000000000006',
        'Sergio Nieto',
        '99887766D',
        true,
        '+34 600 777 888',
        NULL,
        true,
        40.0,
        'b0000000-0000-0000-0000-000000000001',
        now()
    );

-- -----------------------------------------------------------------------------
-- BLOQUE 6: IPs CONOCIDAS
-- Se incluye la IP sospechosa 77.231.105.42 (Carlos, Madrid) para que el
-- sistema de antifraude la tenga catalogada con risk=medium.
-- -----------------------------------------------------------------------------
INSERT INTO
    known_ips (ip, geoip_data, created_at)
VALUES (
        '192.168.1.10',
        '{"city":"Madrid", "region":"MD","country":"ES","isp":"Movistar","org":"Fibra Optica Casa","type":"residential"}'::jsonb,
        now()
    ),
    (
        '192.168.1.11',
        '{"city":"Madrid", "region":"MD","country":"ES","isp":"Movistar","org":"Fibra Optica Casa","type":"residential"}'::jsonb,
        now()
    ),
    (
        '80.12.34.56',
        '{"city":"Pozuelo","region":"MD","country":"ES","isp":"Vodafone","connection":"Cellular","type":"mobile"}'::jsonb,
        now()
    ),
    (
        '10.10.10.10',
        '{"city":"Pozuelo","region":"MD","country":"ES","isp":"Vodafone","connection":"Cellular","type":"mobile"}'::jsonb,
        now()
    ),
    (
        '10.10.10.20',
        '{"city":"Pozuelo","region":"MD","country":"ES","isp":"Vodafone","connection":"Cellular","type":"mobile"}'::jsonb,
        now()
    ),
    (
        '77.231.105.42',
        '{"city":"Madrid", "region":"MD","country":"ES","isp":"Orange","org":"Orange Espana","type":"residential","risk":"medium"}'::jsonb,
        now()
    ),
    (
        '1.1.1.1',
        '{"city":"Unknown","region":"XX","country":"XX","isp":"Cloudflare","org":"APNIC Research","type":"hosting"}'::jsonb,
        now()
    );

-- -----------------------------------------------------------------------------
-- BLOQUE 7: HORARIOS DE TRABAJO
-- Mapeo: 0=MONDAY…4=FRIDAY, 5=SATURDAY, 6=SUNDAY
-- -----------------------------------------------------------------------------

-- Roberto: Lun-Vie 08:00-16:00 @ Las Encinas
INSERT INTO
    work_schedules (
        id,
        employee_id,
        site_id,
        day_of_week,
        start_time,
        end_time,
        company_id,
        created_at,
        updated_at
    )
VALUES (
        'f0000000-0000-0000-0001-000000000001',
        'e0000000-0000-0000-0000-000000000003',
        'c0000000-0000-0000-0000-000000000001',
        'MONDAY',
        '08:00',
        '16:00',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    ),
    (
        'f0000000-0000-0000-0001-000000000002',
        'e0000000-0000-0000-0000-000000000003',
        'c0000000-0000-0000-0000-000000000001',
        'TUESDAY',
        '08:00',
        '16:00',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    ),
    (
        'f0000000-0000-0000-0001-000000000003',
        'e0000000-0000-0000-0000-000000000003',
        'c0000000-0000-0000-0000-000000000001',
        'WEDNESDAY',
        '08:00',
        '16:00',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    ),
    (
        'f0000000-0000-0000-0001-000000000004',
        'e0000000-0000-0000-0000-000000000003',
        'c0000000-0000-0000-0000-000000000001',
        'THURSDAY',
        '08:00',
        '16:00',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    ),
    (
        'f0000000-0000-0000-0001-000000000005',
        'e0000000-0000-0000-0000-000000000003',
        'c0000000-0000-0000-0000-000000000001',
        'FRIDAY',
        '08:00',
        '16:00',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    );

-- Sergio: Lun-Vie 16:00-23:59 @ Las Encinas
INSERT INTO
    work_schedules (
        id,
        employee_id,
        site_id,
        day_of_week,
        start_time,
        end_time,
        company_id,
        created_at,
        updated_at
    )
VALUES (
        'f0000000-0000-0000-0002-000000000001',
        'e0000000-0000-0000-0000-000000000006',
        'c0000000-0000-0000-0000-000000000001',
        'MONDAY',
        '16:00',
        '23:59',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    ),
    (
        'f0000000-0000-0000-0002-000000000002',
        'e0000000-0000-0000-0000-000000000006',
        'c0000000-0000-0000-0000-000000000001',
        'TUESDAY',
        '16:00',
        '23:59',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    ),
    (
        'f0000000-0000-0000-0002-000000000003',
        'e0000000-0000-0000-0000-000000000006',
        'c0000000-0000-0000-0000-000000000001',
        'WEDNESDAY',
        '16:00',
        '23:59',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    ),
    (
        'f0000000-0000-0000-0002-000000000004',
        'e0000000-0000-0000-0000-000000000006',
        'c0000000-0000-0000-0000-000000000001',
        'THURSDAY',
        '16:00',
        '23:59',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    ),
    (
        'f0000000-0000-0000-0002-000000000005',
        'e0000000-0000-0000-0000-000000000006',
        'c0000000-0000-0000-0000-000000000001',
        'FRIDAY',
        '16:00',
        '23:59',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    );

-- Laura: Sab-Dom 10:00-20:00 @ Piscina
INSERT INTO
    work_schedules (
        id,
        employee_id,
        site_id,
        day_of_week,
        start_time,
        end_time,
        company_id,
        created_at,
        updated_at
    )
VALUES (
        'f0000000-0000-0000-0003-000000000001',
        'e0000000-0000-0000-0000-000000000004',
        'c0000000-0000-0000-0000-000000000003',
        'SATURDAY',
        '10:00',
        '20:00',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    ),
    (
        'f0000000-0000-0000-0003-000000000002',
        'e0000000-0000-0000-0000-000000000004',
        'c0000000-0000-0000-0000-000000000003',
        'SUNDAY',
        '10:00',
        '20:00',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    );

-- Carlos: Lun/Mie/Vie 07:00-15:00 @ Los Rosales
INSERT INTO
    work_schedules (
        id,
        employee_id,
        site_id,
        day_of_week,
        start_time,
        end_time,
        company_id,
        created_at,
        updated_at
    )
VALUES (
        'f0000000-0000-0000-0004-000000000001',
        'e0000000-0000-0000-0000-000000000005',
        'c0000000-0000-0000-0000-000000000002',
        'MONDAY',
        '07:00',
        '15:00',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    ),
    (
        'f0000000-0000-0000-0004-000000000002',
        'e0000000-0000-0000-0000-000000000005',
        'c0000000-0000-0000-0000-000000000002',
        'WEDNESDAY',
        '07:00',
        '15:00',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    ),
    (
        'f0000000-0000-0000-0004-000000000003',
        'e0000000-0000-0000-0000-000000000005',
        'c0000000-0000-0000-0000-000000000002',
        'FRIDAY',
        '07:00',
        '15:00',
        'a0000000-0000-0000-0000-000000000001',
        now(),
        now()
    );

-- =============================================================================
-- BLOQUES 8-10: FICHAJES, AUDITORIA E INCIDENCIAS
-- Todo en un DO $$ para compartir variables y anclas temporales.
-- =============================================================================
DO $$
DECLARE
    -- Anclas de semana: lunes ISO de hace 2 y 1 semana
    w0   date := (date_trunc('week', CURRENT_DATE) - interval '14 days')::date;
    w1   date := (date_trunc('week', CURRENT_DATE) - interval '7 days')::date;

    ua_android  text := 'Mozilla/5.0 (Linux; Android 13; SM-A536B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36';
    ua_iphone   text := 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.3 Mobile/15E148 Safari/604.1';

    geoip_madrid  jsonb := '{"city":"Madrid", "region":"MD","country":"ES","isp":"Movistar","org":"Fibra Optica Casa","type":"residential"}';
    geoip_pozuelo jsonb := '{"city":"Pozuelo","region":"MD","country":"ES","isp":"Vodafone","connection":"Cellular","type":"mobile"}';
    geoip_sospech jsonb := '{"city":"Madrid", "region":"MD","country":"ES","isp":"Orange","org":"Orange Espana","type":"residential","risk":"medium"}';

    lat_encinas numeric(9,6) := 40.417800;   lng_encinas numeric(9,6) := -3.703800;
    lat_piscina numeric(9,6) := 40.436800;   lng_piscina numeric(9,6) := -3.723800;
    lat_rosales numeric(9,6) := 40.406800;   lng_rosales numeric(9,6) := -3.673800;

    id_admin   uuid := 'e0000000-0000-0000-0000-000000000001';
    id_insp    uuid := 'e0000000-0000-0000-0000-000000000002';
    id_roberto uuid := 'e0000000-0000-0000-0000-000000000003';
    id_laura   uuid := 'e0000000-0000-0000-0000-000000000004';
    id_carlos  uuid := 'e0000000-0000-0000-0000-000000000005';
    id_sergio  uuid := 'e0000000-0000-0000-0000-000000000006';
    cid        uuid := 'a0000000-0000-0000-0000-000000000001';

    semana  date;
    d       date;
    off     integer;

BEGIN

-- ===========================================================================
-- BLOQUE 8: FICHAJES HISTORICOS (w0 y w1, Lun-Vie + finde Laura)
--
-- UUIDs fijos para soft-delete (referenciados en BLOQUE 9):
--   b1e7a2e0-1c2d-4e3a-8f6b-1a2b3c4d5e6f  Roberto duplicado   (Mar w0 = w0+1)
--   c2f8b3d1-2e4f-5a6b-7c8d-9e0f1a2b3c4d  Laura prueba fallida (Lun w1 = w1)
-- ===========================================================================

FOREACH semana IN ARRAY ARRAY[w0, w1] LOOP

    -- Dias laborales: 0=Lun … 4=Vie
    FOR off IN 0..4 LOOP
        d := semana + off;

        -- == ROBERTO (Lun-Vie, WiFi Madrid) ==================================
        -- off=2 (Mie): GPS_LOW_ACCURACY (accuracy 250 m)
        -- off=3 (Jue): ADMIN_EDITED    (end_at +30 min, olvido salida)
        INSERT INTO time_entries (
            id, employee_id, work_date, status,
            start_at, end_at,
            start_lat, start_lng, end_lat, end_lng,
            start_accuracy_m, end_accuracy_m,
            start_ip, end_ip, start_user_agent, end_user_agent,
            start_geoip, end_geoip,
            flags, modification_reason,
            created_by, company_id, created_at, updated_at
        ) VALUES (
            gen_random_uuid(), id_roberto, d, 'CLOSED',
            (d + time '07:58:00') AT TIME ZONE 'Europe/Madrid',
            CASE WHEN off = 3
                 THEN (d + time '16:32:00') AT TIME ZONE 'Europe/Madrid'
                 ELSE (d + time '16:02:00') AT TIME ZONE 'Europe/Madrid'
            END,
            lat_encinas, lng_encinas, lat_encinas, lng_encinas,
            CASE WHEN off = 2 THEN 250 ELSE 15 END, 12,
            '192.168.1.10'::inet, '192.168.1.10'::inet, ua_android, ua_android,
            geoip_madrid, geoip_madrid,
            CASE WHEN off = 2 THEN ARRAY['GPS_LOW_ACCURACY']
                 WHEN off = 3 THEN ARRAY['ADMIN_EDITED']
                 ELSE ARRAY[]::text[] END,
            CASE WHEN off = 2 THEN 'Simulacion: Baja precision GPS detectada post-auditoria'
                 WHEN off = 3 THEN 'Empleado olvido fichar salida. Ajuste manual +30 min por admin.'
                 ELSE NULL END,
            id_roberto, cid, now(),
            CASE WHEN off IN (2, 3) THEN now() ELSE NULL END
        );

        -- == SERGIO (Lun-Vie, WiFi Madrid tarde) ==============================
        -- Semana w1, off=2 (Mie): GPS_LOW_ACCURACY (250 m)
        -- Semana w1, off=3 (Jue): OUT_OF_HOURS (entro 15:15 en vez de 16:00)
        INSERT INTO time_entries (
            id, employee_id, work_date, status,
            start_at, end_at,
            start_lat, start_lng, end_lat, end_lng,
            start_accuracy_m, end_accuracy_m,
            start_ip, end_ip, start_user_agent, end_user_agent,
            start_geoip, end_geoip,
            flags, modification_reason,
            created_by, company_id, created_at, updated_at
        ) VALUES (
            gen_random_uuid(), id_sergio, d, 'CLOSED',
            CASE WHEN semana = w1 AND off = 3
                 THEN (d + time '15:15:00') AT TIME ZONE 'Europe/Madrid'
                 ELSE (d + time '15:57:00') AT TIME ZONE 'Europe/Madrid'
            END,
            (d + time '23:59:00') AT TIME ZONE 'Europe/Madrid',
            lat_encinas, lng_encinas, lat_encinas, lng_encinas,
            CASE WHEN semana = w1 AND off = 2 THEN 250 ELSE 10 END, 20,
            '192.168.1.11'::inet, '192.168.1.11'::inet, ua_android, ua_android,
            geoip_madrid, geoip_madrid,
            CASE WHEN semana = w1 AND off = 2 THEN ARRAY['GPS_LOW_ACCURACY']
                 WHEN semana = w1 AND off = 3 THEN ARRAY['OUT_OF_HOURS']
                 ELSE ARRAY[]::text[] END,
            CASE WHEN semana = w1 AND off = 2 THEN 'Simulacion: Baja precision GPS detectada post-auditoria'
                 WHEN semana = w1 AND off = 3 THEN 'Entrada 45 min antes de inicio de turno. Revisado por inspector.'
                 ELSE NULL END,
            id_sergio, cid, now(),
            CASE WHEN semana = w1 AND off IN (2, 3) THEN now() ELSE NULL END
        );

        -- == CARLOS (Lun, Mie, Vie — iPhone 4G Pozuelo) ======================
        -- Anomalia IP_LOCATION_MISMATCH:
        --   w0 Vie (off=4): IP 77.231.105.42 (Madrid en vez de Pozuelo) → audit #8
        --   w1 Lun (off=0): misma IP sospechosa                          → audit #9 y #10
        IF off IN (0, 2, 4) THEN
            INSERT INTO time_entries (
                id, employee_id, work_date, status,
                start_at, end_at,
                start_lat, start_lng, end_lat, end_lng,
                start_accuracy_m, end_accuracy_m,
                start_ip, end_ip, start_user_agent, end_user_agent,
                start_geoip, end_geoip,
                flags, modification_reason,
                created_by, company_id, created_at, updated_at
            ) VALUES (
                gen_random_uuid(), id_carlos, d, 'CLOSED',
                (d + time '07:00:00') AT TIME ZONE 'Europe/Madrid',
                (d + time '15:00:00') AT TIME ZONE 'Europe/Madrid',
                lat_rosales, lng_rosales, lat_rosales, lng_rosales,
                12, 14,
                CASE WHEN (semana = w0 AND off = 4) OR (semana = w1 AND off = 0)
                     THEN '77.231.105.42'::inet ELSE '10.10.10.10'::inet END,
                '10.10.10.10'::inet,
                ua_iphone, ua_iphone,
                CASE WHEN (semana = w0 AND off = 4) OR (semana = w1 AND off = 0)
                     THEN geoip_sospech ELSE geoip_pozuelo END,
                geoip_pozuelo,
                CASE WHEN (semana = w0 AND off = 4) OR (semana = w1 AND off = 0)
                     THEN ARRAY['IP_LOCATION_MISMATCH'] ELSE ARRAY[]::text[] END,
                CASE WHEN (semana = w0 AND off = 4) OR (semana = w1 AND off = 0)
                     THEN 'Entrada desde IP de Madrid. Centro asignado: Pozuelo. Revision pendiente.'
                     ELSE NULL END,
                id_carlos, cid, now(),
                CASE WHEN (semana = w0 AND off = 4) OR (semana = w1 AND off = 0)
                     THEN now() ELSE NULL END
            );
        END IF;

    END LOOP;  -- fin FOR off 0..4

    -- Fin de semana: Laura (Sab off=5, Dom off=6)
    FOR off IN 5..6 LOOP
        d := semana + off;
        INSERT INTO time_entries (
            id, employee_id, work_date, status,
            start_at, end_at,
            start_lat, start_lng, end_lat, end_lng,
            start_accuracy_m, end_accuracy_m,
            start_ip, end_ip, start_user_agent, end_user_agent,
            start_geoip, end_geoip,
            flags, modification_reason,
            created_by, company_id, created_at, updated_at
        ) VALUES (
            gen_random_uuid(), id_laura, d, 'CLOSED',
            (d + time '10:00:00') AT TIME ZONE 'Europe/Madrid',
            (d + time '20:00:00') AT TIME ZONE 'Europe/Madrid',
            lat_piscina, lng_piscina, lat_piscina, lng_piscina,
            8, 9,
            '80.12.34.56'::inet, '80.12.34.56'::inet, ua_android, ua_android,
            geoip_pozuelo, geoip_pozuelo,
            ARRAY[]::text[], NULL,
            id_laura, cid, now(), NULL
        );
    END LOOP;

END LOOP;  -- fin FOREACH semana

-- ── Fichajes soft-delete (UUID fijo para referencia cruzada en auditoria) ────

-- Roberto duplicado — Mar w0
INSERT INTO time_entries (
    id, employee_id, work_date, status,
    start_at, end_at,
    start_lat, start_lng, end_lat, end_lng,
    start_accuracy_m, end_accuracy_m,
    start_ip, end_ip, start_user_agent, end_user_agent,
    start_geoip, end_geoip, flags,
    created_by, deleted_at, deleted_by, delete_reason,
    company_id, created_at, updated_at
) VALUES (
    'b1e7a2e0-1c2d-4e3a-8f6b-1a2b3c4d5e6f',
    id_roberto, (w0 + interval '1 day'), 'CLOSED',
    ((w0 + interval '1 day') + time '08:00:00') AT TIME ZONE 'Europe/Madrid',
    ((w0 + interval '1 day') + time '08:01:00') AT TIME ZONE 'Europe/Madrid',
    lat_encinas, lng_encinas, lat_encinas, lng_encinas,
    10, 10,
    '1.1.1.1'::inet, '1.1.1.1'::inet, ua_android, ua_android,
    geoip_madrid, geoip_madrid, ARRAY[]::text[],
    id_roberto,
    now() - interval '5 minutes', id_admin, 'Registro duplicado por error',
    cid, now() - interval '10 minutes', now() - interval '5 minutes'
);

-- Laura prueba fallida — Lun w1
INSERT INTO time_entries (
    id, employee_id, work_date, status,
    start_at, end_at,
    start_lat, start_lng, end_lat, end_lng,
    start_accuracy_m, end_accuracy_m,
    start_ip, end_ip, start_user_agent, end_user_agent,
    start_geoip, end_geoip, flags,
    created_by, deleted_at, deleted_by, delete_reason,
    company_id, created_at, updated_at
) VALUES (
    'c2f8b3d1-2e4f-5a6b-7c8d-9e0f1a2b3c4d',
    id_laura, w1, 'CLOSED',
    (w1 + time '10:00:00') AT TIME ZONE 'Europe/Madrid',
    (w1 + time '10:05:00') AT TIME ZONE 'Europe/Madrid',
    lat_piscina, lng_piscina, lat_piscina, lng_piscina,
    10, 10,
    '1.1.1.1'::inet, '1.1.1.1'::inet, ua_android, ua_android,
    geoip_pozuelo, geoip_pozuelo, ARRAY[]::text[],
    id_laura,
    now() - interval '5 minutes', id_admin, 'Prueba fallida de sistema',
    cid, now() - interval '10 minutes', now() - interval '5 minutes'
);

-- ── Fichajes OPEN (turno activo hoy) ─────────────────────────────────────────

INSERT INTO time_entries (
    id, employee_id, work_date, status,
    start_at, end_at,
    start_lat, start_lng, end_lat, end_lng,
    start_accuracy_m, end_accuracy_m,
    start_ip, end_ip, start_user_agent, end_user_agent,
    start_geoip, end_geoip, flags,
    created_by, company_id, created_at, updated_at
) VALUES (
    gen_random_uuid(), id_roberto, CURRENT_DATE, 'OPEN',
    now() - interval '4 hours', NULL,
    lat_encinas, lng_encinas, NULL, NULL,
    10, NULL, '192.168.1.10'::inet, NULL, ua_android, NULL,
    geoip_madrid, NULL, ARRAY[]::text[],
    id_roberto, cid, now(), NULL
);

INSERT INTO time_entries (
    id, employee_id, work_date, status,
    start_at, end_at,
    start_lat, start_lng, end_lat, end_lng,
    start_accuracy_m, end_accuracy_m,
    start_ip, end_ip, start_user_agent, end_user_agent,
    start_geoip, end_geoip, flags,
    created_by, company_id, created_at, updated_at
) VALUES (
    gen_random_uuid(), id_carlos, CURRENT_DATE, 'OPEN',
    now() - interval '5 hours', NULL,
    lat_rosales, lng_rosales, NULL, NULL,
    15, NULL, '10.10.10.20'::inet, NULL, ua_iphone, NULL,
    geoip_pozuelo, NULL, ARRAY[]::text[],
    id_carlos, cid, now(), NULL
);


-- ===========================================================================
-- BLOQUE 9: AUDITORIA (audit_time_entries)
--
-- 10 registros que cubren cada anomalia sembrada.
--
-- #1  SOFT_DELETE   Roberto duplicado (Mar w0)               actor: admin
-- #2  SOFT_DELETE   Laura prueba fallida (Lun w1)            actor: admin
-- #3  ADMIN_ADJUST  Roberto GPS_LOW_ACCURACY (Mie w0)        actor: inspector
-- #4  ADMIN_ADJUST  Sergio  GPS_LOW_ACCURACY (Mie w1)        actor: inspector
-- #5  ADMIN_ADJUST  Roberto olvido salida (Jue w0)           actor: admin
-- #6  ADMIN_ADJUST  Roberto olvido salida (Jue w1) REINCI.   actor: admin
-- #7  ADMIN_ADJUST  Sergio  OUT_OF_HOURS (Jue w1)            actor: inspector
-- #8  ADMIN_ADJUST  Carlos  IP_LOCATION_MISMATCH (Vie w0)    actor: inspector
-- #9  ADMIN_ADJUST  Carlos  IP_LOCATION_MISMATCH (Lun w1)    actor: inspector
-- #10 ADMIN_ADJUST  Carlos  escalado disciplinario (Lun w1)  actor: admin
-- ===========================================================================

-- #1 SOFT_DELETE: Roberto duplicado
INSERT INTO audit_time_entries (
    id, time_entry_id, action, actor_user_id, reason,
    old_data, new_data, company_id, created_at
) VALUES (
    gen_random_uuid(),
    'b1e7a2e0-1c2d-4e3a-8f6b-1a2b3c4d5e6f',
    'SOFT_DELETE',
    id_admin,
    'Registro duplicado por error: el trabajador activo dos veces la misma entrada por fallo de app movil.',
    jsonb_build_object(
        'employee_id', id_roberto,
        'work_date',   (w0 + interval '1 day')::text,
        'start_at',    (((w0 + interval '1 day') + time '08:00:00') AT TIME ZONE 'Europe/Madrid')::text,
        'end_at',      (((w0 + interval '1 day') + time '08:01:00') AT TIME ZONE 'Europe/Madrid')::text,
        'start_ip',    '1.1.1.1',
        'status',      'CLOSED',
        'flags',       '[]'::jsonb
    ),
    NULL,  -- new_data=NULL en soft-delete: el registro desaparece de la vista activa
    cid,
    now() - interval '5 minutes'
);

-- #2 SOFT_DELETE: Laura prueba fallida
INSERT INTO audit_time_entries (
    id, time_entry_id, action, actor_user_id, reason,
    old_data, new_data, company_id, created_at
) VALUES (
    gen_random_uuid(),
    'c2f8b3d1-2e4f-5a6b-7c8d-9e0f1a2b3c4d',
    'SOFT_DELETE',
    id_admin,
    'Registro de prueba creado durante validacion de integracion con el sistema de turnos. No corresponde a turno real.',
    jsonb_build_object(
        'employee_id', id_laura,
        'work_date',   w1::text,
        'start_at',    ((w1 + time '10:00:00') AT TIME ZONE 'Europe/Madrid')::text,
        'end_at',      ((w1 + time '10:05:00') AT TIME ZONE 'Europe/Madrid')::text,
        'start_ip',    '1.1.1.1',
        'status',      'CLOSED',
        'flags',       '[]'::jsonb
    ),
    NULL,
    cid,
    now() - interval '4 minutes'
);

-- #3 ADMIN_ADJUST: Roberto GPS_LOW_ACCURACY (Mie w0)
INSERT INTO audit_time_entries (
    id, time_entry_id, action, actor_user_id, reason,
    old_data, new_data, company_id, created_at
)
SELECT
    gen_random_uuid(), te.id, 'ADMIN_ADJUST', id_insp,
    'Revision post-turno: start_accuracy_m=250 m supera el umbral permitido (50 m). '
    || 'Flag GPS_LOW_ACCURACY anadido. Posible uso en interior del edificio sin senal GPS.',
    jsonb_build_object(
        'employee_id',      te.employee_id,
        'work_date',        te.work_date::text,
        'start_accuracy_m', 15,
        'flags',            '[]'::jsonb
    ),
    jsonb_build_object(
        'employee_id',      te.employee_id,
        'work_date',        te.work_date::text,
        'start_accuracy_m', 250,
        'flags',            '["GPS_LOW_ACCURACY"]'::jsonb,
        'reviewed_by',      id_insp
    ),
    cid, now()
FROM time_entries te
WHERE te.employee_id = id_roberto
  AND te.work_date   = (w0 + 2)
  AND te.deleted_at  IS NULL
  AND 'GPS_LOW_ACCURACY' = ANY(te.flags)
LIMIT 1;

-- #4 ADMIN_ADJUST: Sergio GPS_LOW_ACCURACY (Mie w1)
INSERT INTO audit_time_entries (
    id, time_entry_id, action, actor_user_id, reason,
    old_data, new_data, company_id, created_at
)
SELECT
    gen_random_uuid(), te.id, 'ADMIN_ADJUST', id_insp,
    'Segundo caso de GPS_LOW_ACCURACY en 2 semanas (misma zona fisica, Encinas). '
    || 'start_accuracy_m=250 m. Inspector recomienda revisar configuracion de app en este dispositivo. '
    || 'Se informa a RRHH para seguimiento.',
    jsonb_build_object(
        'employee_id',      te.employee_id,
        'work_date',        te.work_date::text,
        'start_accuracy_m', 10,
        'flags',            '[]'::jsonb
    ),
    jsonb_build_object(
        'employee_id',      te.employee_id,
        'work_date',        te.work_date::text,
        'start_accuracy_m', 250,
        'flags',            '["GPS_LOW_ACCURACY"]'::jsonb,
        'reviewed_by',      id_insp,
        'alert',            '"REPEATED_GPS_ISSUE_SAME_ZONE"'
    ),
    cid, now()
FROM time_entries te
WHERE te.employee_id = id_sergio
  AND te.work_date   = (w1 + 2)
  AND te.deleted_at  IS NULL
  AND 'GPS_LOW_ACCURACY' = ANY(te.flags)
LIMIT 1;

-- #5 ADMIN_ADJUST: Roberto olvido salida (Jue w0) — primera vez
INSERT INTO audit_time_entries (
    id, time_entry_id, action, actor_user_id, reason,
    old_data, new_data, company_id, created_at
)
SELECT
    gen_random_uuid(), te.id, 'ADMIN_ADJUST', id_admin,
    'Empleado olvido registrar salida. Fin de turno habitual: 16:02. '
    || 'Ajuste manual +30 min por evidencia de permanencia en instalaciones (camara entrada). '
    || 'Primera vez que ocurre: se notifica verbalmente al trabajador.',
    jsonb_build_object(
        'employee_id', te.employee_id,
        'work_date',   te.work_date::text,
        'end_at',      ((te.work_date + time '16:02:00') AT TIME ZONE 'Europe/Madrid')::text,
        'flags',       '[]'::jsonb
    ),
    jsonb_build_object(
        'employee_id', te.employee_id,
        'work_date',   te.work_date::text,
        'end_at',      ((te.work_date + time '16:32:00') AT TIME ZONE 'Europe/Madrid')::text,
        'flags',       '["ADMIN_EDITED"]'::jsonb
    ),
    cid, now()
FROM time_entries te
WHERE te.employee_id = id_roberto
  AND te.work_date   = (w0 + 3)
  AND te.deleted_at  IS NULL
  AND 'ADMIN_EDITED' = ANY(te.flags)
LIMIT 1;

-- #6 ADMIN_ADJUST: Roberto olvido salida (Jue w1) — REINCIDENCIA sospechosa
-- Mismo dia de la semana, dos semanas consecutivas. El admin lo documenta con alerta.
INSERT INTO audit_time_entries (
    id, time_entry_id, action, actor_user_id, reason,
    old_data, new_data, company_id, created_at
)
SELECT
    gen_random_uuid(), te.id, 'ADMIN_ADJUST', id_admin,
    'REINCIDENCIA: Roberto vuelve a olvidar fichar salida el JUEVES, segunda semana consecutiva. '
    || 'Ajuste manual +30 min. Patron sospechoso: podria estar usando los olvidos para ampliar '
    || 'horas de forma artificial. Inspector notificado para seguimiento. '
    || 'Si se repite una tercera vez se abrira expediente disciplinario.',
    jsonb_build_object(
        'employee_id', te.employee_id,
        'work_date',   te.work_date::text,
        'end_at',      ((te.work_date + time '16:02:00') AT TIME ZONE 'Europe/Madrid')::text,
        'flags',       '[]'::jsonb
    ),
    jsonb_build_object(
        'employee_id', te.employee_id,
        'work_date',   te.work_date::text,
        'end_at',      ((te.work_date + time '16:32:00') AT TIME ZONE 'Europe/Madrid')::text,
        'flags',       '["ADMIN_EDITED"]'::jsonb,
        'alert',       '"REPEATED_PATTERN_DETECTED"',
        'occurrences', 2
    ),
    cid, now()
FROM time_entries te
WHERE te.employee_id = id_roberto
  AND te.work_date   = (w1 + 3)
  AND te.deleted_at  IS NULL
  AND 'ADMIN_EDITED' = ANY(te.flags)
LIMIT 1;

-- #7 ADMIN_ADJUST: Sergio OUT_OF_HOURS (Jue w1)
-- Entro a las 15:15, su turno empieza a las 16:00 (45 min antes).
INSERT INTO audit_time_entries (
    id, time_entry_id, action, actor_user_id, reason,
    old_data, new_data, company_id, created_at
)
SELECT
    gen_random_uuid(), te.id, 'ADMIN_ADJUST', id_insp,
    'Entrada registrada a las 15:15, 45 minutos antes del inicio de turno (16:00). '
    || 'Turno oficial: 16:00-23:59. Trabajador no autorizado para fichar antes de su franja. '
    || 'Posible intento de computar horas extra no autorizadas. '
    || 'Se comunica a RRHH. Se mantiene el fichaje pero se marca como OUT_OF_HOURS.',
    jsonb_build_object(
        'employee_id', te.employee_id,
        'work_date',   te.work_date::text,
        'start_at',    ((te.work_date + time '15:57:00') AT TIME ZONE 'Europe/Madrid')::text,
        'flags',       '[]'::jsonb
    ),
    jsonb_build_object(
        'employee_id',    te.employee_id,
        'work_date',      te.work_date::text,
        'start_at',       ((te.work_date + time '15:15:00') AT TIME ZONE 'Europe/Madrid')::text,
        'flags',          '["OUT_OF_HOURS"]'::jsonb,
        'alert',          '"EARLY_CLOCK_IN"',
        'minutes_early',  45,
        'reviewed_by',    id_insp
    ),
    cid, now()
FROM time_entries te
WHERE te.employee_id = id_sergio
  AND te.work_date   = (w1 + 3)
  AND te.deleted_at  IS NULL
  AND 'OUT_OF_HOURS' = ANY(te.flags)
LIMIT 1;

-- #8 ADMIN_ADJUST: Carlos IP_LOCATION_MISMATCH (Vie w0) — primera incidencia
INSERT INTO audit_time_entries (
    id, time_entry_id, action, actor_user_id, reason,
    old_data, new_data, company_id, created_at
)
SELECT
    gen_random_uuid(), te.id, 'ADMIN_ADJUST', id_insp,
    'Entrada registrada desde IP 77.231.105.42 (Madrid, Orange) siendo el centro asignado '
    || '"Comunidad Propietarios Los Rosales" en Pozuelo. Distancia estimada IP<>centro: ~15 km. '
    || 'Primera incidencia: se avisa al trabajador. Fichaje mantenido como valido.',
    jsonb_build_object(
        'employee_id', te.employee_id,
        'work_date',   te.work_date::text,
        'start_ip',    '10.10.10.10',
        'start_geoip', '{"city":"Pozuelo","region":"MD","country":"ES","isp":"Vodafone","connection":"Cellular","type":"mobile"}'::jsonb,
        'flags',       '[]'::jsonb
    ),
    jsonb_build_object(
        'employee_id', te.employee_id,
        'work_date',   te.work_date::text,
        'start_ip',    '77.231.105.42',
        'start_geoip', '{"city":"Madrid","region":"MD","country":"ES","isp":"Orange","org":"Orange Espana","type":"residential","risk":"medium"}'::jsonb,
        'flags',       '["IP_LOCATION_MISMATCH"]'::jsonb,
        'alert',       '"SUSPICIOUS_IP_ORIGIN"',
        'distance_km', 15
    ),
    cid, now()
FROM time_entries te
WHERE te.employee_id = id_carlos
  AND te.work_date   = (w0 + 4)
  AND te.deleted_at  IS NULL
  AND 'IP_LOCATION_MISMATCH' = ANY(te.flags)
LIMIT 1;

-- #9 ADMIN_ADJUST: Carlos IP_LOCATION_MISMATCH (Lun w1) — REINCIDENCIA
-- El inspector escala a admin tras la segunda incidencia en 5 dias.
INSERT INTO audit_time_entries (
    id, time_entry_id, action, actor_user_id, reason,
    old_data, new_data, company_id, created_at
)
SELECT
    gen_random_uuid(), te.id, 'ADMIN_ADJUST', id_insp,
    'SEGUNDA INCIDENCIA IP_LOCATION_MISMATCH en 5 dias para Carlos Ruiz. '
    || 'IP origen: 77.231.105.42 (Madrid). Centro asignado: Pozuelo (~15 km). '
    || 'Trabajador alega que ficho desde casa antes de desplazarse. '
    || 'Patron sospechoso: posible suplantacion de fichaje sin presencia fisica. '
    || 'Inspector escala a administrador para decision disciplinaria.',
    jsonb_build_object(
        'employee_id', te.employee_id,
        'work_date',   te.work_date::text,
        'start_ip',    '10.10.10.10',
        'flags',       '[]'::jsonb
    ),
    jsonb_build_object(
        'employee_id',  te.employee_id,
        'work_date',    te.work_date::text,
        'start_ip',     '77.231.105.42',
        'flags',        '["IP_LOCATION_MISMATCH"]'::jsonb,
        'alert',        '"REPEATED_SUSPICIOUS_IP"',
        'occurrences',  2,
        'escalated_to', '"ADMIN"'
    ),
    cid, now()
FROM time_entries te
WHERE te.employee_id = id_carlos
  AND te.work_date   = w1
  AND te.deleted_at  IS NULL
  AND 'IP_LOCATION_MISMATCH' = ANY(te.flags)
LIMIT 1;

-- #10 ADMIN_ADJUST: Carlos — decision admin tras escalado (mismo fichaje Lun w1)
-- El admin documenta la resolucion disciplinaria y activa monitorizacion reforzada.
INSERT INTO audit_time_entries (
    id, time_entry_id, action, actor_user_id, reason,
    old_data, new_data, company_id, created_at
)
SELECT
    gen_random_uuid(), te.id, 'ADMIN_ADJUST', id_admin,
    'Admin recibe escalado del inspector (segunda IP_LOCATION_MISMATCH en 5 dias). '
    || 'Carlos Ruiz citado a reunion disciplinaria. '
    || 'Se activa monitorizacion reforzada: proximos 10 fichajes requeriran GPS + foto obligatoria. '
    || 'Fichaje de hoy mantenido como valido bajo reserva hasta resolucion del expediente. '
    || 'Ref. expediente: EXP-CARLOS-001.',
    jsonb_build_object(
        'employee_id',  te.employee_id,
        'work_date',    te.work_date::text,
        'flags',        '["IP_LOCATION_MISMATCH"]'::jsonb,
        'escalated_to', '"ADMIN"'
    ),
    jsonb_build_object(
        'employee_id',          te.employee_id,
        'work_date',            te.work_date::text,
        'flags',                '["IP_LOCATION_MISMATCH","UNDER_INVESTIGATION"]'::jsonb,
        'disciplinary_ref',     'EXP-CARLOS-001',
        'enhanced_monitoring',  true,
        'monitoring_remaining', 10,
        'resolved_by',          id_admin,
        'alert',                '"DISCIPLINARY_ACTION_INITIATED"'
    ),
    cid, now() + interval '1 minute'  -- admin actuo justo despues del inspector
FROM time_entries te
WHERE te.employee_id = id_carlos
  AND te.work_date   = w1
  AND te.deleted_at  IS NULL
  AND 'IP_LOCATION_MISMATCH' = ANY(te.flags)
LIMIT 1;


-- ===========================================================================
-- BLOQUE 10: INCIDENCIAS
--
-- RESOLVED / REJECTED → semanas w0 / w1  (ya gestionadas)
-- PENDING             → semana en curso  (siempre recientes)
-- ===========================================================================

-- Caso 1: Laura — Sustitucion urgente → RESOLVED (viernes w1, notifico el jueves)
INSERT INTO incidents (
    id, user_id, type_id, date, incident_time,
    comment, status, admin_response, resolved_by,
    created_at, updated_at, company_id
) VALUES (
    'ac100001-0000-0000-0000-000000000001',
    id_laura, 'd0000000-0000-0000-0000-000000000001',
    (w1 + interval '4 days'), '09:00:00',
    'Mi hijo se ha puesto enfermo, no podre cubrir el turno de manana.',
    'RESOLVED',
    'Entendido Laura, avisamos a la agencia para el relevo.',
    id_admin,
    ((w1 + 3) + time '08:00:00') AT TIME ZONE 'Europe/Madrid',
    now(), cid
);

-- Caso 2: Carlos — Averia instalaciones → RESOLVED (mie w0)
INSERT INTO incidents (
    id, user_id, type_id, date, incident_time,
    comment, status, admin_response, resolved_by,
    created_at, updated_at, company_id
) VALUES (
    'ac100001-0000-0000-0000-000000000002',
    id_carlos, 'd0000000-0000-0000-0000-000000000003',
    (w0 + interval '2 days'), '08:15:00',
    'La bomba de riego de los rosales hace un ruido extrano y no sale agua.',
    'RESOLVED',
    'Tecnico enviado y reparacion finalizada.',
    id_admin,
    ((w0 + 2) + time '08:30:00') AT TIME ZONE 'Europe/Madrid',
    now(), cid
);

-- Caso 3: Sergio — Olvido de llaves → REJECTED (jue w0)
INSERT INTO incidents (
    id, user_id, type_id, date, incident_time,
    comment, status, admin_response, resolved_by,
    created_at, updated_at, company_id
) VALUES (
    'ac100001-0000-0000-0000-000000000003',
    id_sergio, 'd0000000-0000-0000-0000-000000000006',
    (w0 + interval '3 days'), '16:00:00',
    'Me deje el juego maestro en la oficina central, tuve que volver a por el.',
    'REJECTED',
    'Sergio, es la segunda vez. Esto cuenta como falta leve por negligencia.',
    id_admin,
    ((w0 + 3) + time '17:00:00') AT TIME ZONE 'Europe/Madrid',
    now(), cid
);

-- Caso 4: Roberto — Accidente laboral → PENDING (lunes de esta semana)
INSERT INTO incidents (
    id, user_id, type_id, date, incident_time,
    comment, status, admin_response, resolved_by,
    created_at, updated_at, company_id
) VALUES (
    'ac100001-0000-0000-0000-000000000004',
    id_roberto, 'd0000000-0000-0000-0000-000000000005',
    date_trunc('week', CURRENT_DATE)::date, '12:30:00',
    'Resbalon en la rampa del garaje. Tobillo inflamado. Voy a la mutua.',
    'PENDING', NULL, NULL,
    (date_trunc('week', CURRENT_DATE) + time '13:00:00') AT TIME ZONE 'Europe/Madrid',
    now(), cid
);

-- Caso 5: Carlos — Falta de material → PENDING (martes de esta semana)
INSERT INTO incidents (
    id, user_id, type_id, date, incident_time,
    comment, status, admin_response, resolved_by,
    created_at, updated_at, company_id
) VALUES (
    'ac100001-0000-0000-0000-000000000005',
    id_carlos, 'd0000000-0000-0000-0000-000000000002',
    (date_trunc('week', CURRENT_DATE) + interval '1 day')::date, '10:00:00',
    'Necesito reposicion de guantes de poda y bolsas de basura industriales.',
    'PENDING', NULL, NULL,
    (date_trunc('week', CURRENT_DATE) + interval '1 day' + time '10:05:00') AT TIME ZONE 'Europe/Madrid',
    now(), cid
);

END $$;

-- =============================================================================
-- FIN DEL SEED
-- =============================================================================