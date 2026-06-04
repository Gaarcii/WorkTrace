-- =========================================================================
-- Trigger para proteger integridad de time_entries
-- 1. Prohibe DELETE directo en time_entries
-- 2. Registra UPDATE directo en audit_time_entries con action='DB_DIRECT_MODIFY'
-- =========================================================================

-- Paso 1: Actualizar el CHECK en audit_time_entries para permitir 'DB_DIRECT_MODIFY'
ALTER TABLE audit_time_entries
DROP CONSTRAINT audit_time_entries_action_check;

ALTER TABLE audit_time_entries
ADD CONSTRAINT audit_time_entries_action_check
CHECK (action IN ('ADMIN_ADJUST', 'SOFT_DELETE', 'DB_DIRECT_MODIFY'));

-- Paso 2: Crear función que implemente la lógica del trigger
CREATE OR REPLACE FUNCTION prevent_direct_modifications()
RETURNS TRIGGER AS $$
DECLARE
    audit_action TEXT;
    is_app_initiated BOOLEAN;
BEGIN
    -- Obtener la variable de sesión que la app establece ANTES de modificar
    -- Si no existe, significa que el UPDATE es directo en BD (FRAUDE)
    audit_action := current_setting('app.audit_action', TRUE);
    is_app_initiated := audit_action IS NOT NULL AND audit_action != '';

    IF TG_OP = 'DELETE' THEN
        -- ❌ Prohibir DELETE directo en cualquier caso
        RAISE EXCEPTION 'Direct deletion of time_entries is prohibited. Use soft delete instead.';

    ELSIF TG_OP = 'UPDATE' THEN
        IF is_app_initiated THEN
            -- UPDATE desde la app (Java ya registró en audit_time_entries)
            -- El trigger NO registra nada, solo valida
            RETURN NEW;
        ELSE
            -- UPDATE directo en BD (FRAUDE DETECTADO)
            -- El trigger registra el fraude con actor = NULL (modificación sin usuario autenticado)
            INSERT INTO audit_time_entries (
                time_entry_id,
                action,
                actor_user_id,
                reason,
                old_data,
                new_data,
                company_id,
                created_at
            ) VALUES (
                OLD.id,
                'DB_DIRECT_MODIFY',
                NULL,
                'Direct database modification detected (untracked change)',
                row_to_json(OLD),
                row_to_json(NEW),
                OLD.company_id,
                NOW()
            );

            -- Permitir la modificación (pero ya está registrada como fraude)
            RETURN NEW;
        END IF;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Paso 3: Crear el trigger en la tabla time_entries
DROP TRIGGER IF EXISTS time_entries_integrity_protection ON time_entries;

CREATE TRIGGER time_entries_integrity_protection
BEFORE UPDATE OR DELETE ON time_entries
FOR EACH ROW
EXECUTE FUNCTION prevent_direct_modifications();

-- Paso 4: Comentario de documentación
COMMENT ON TRIGGER time_entries_integrity_protection ON time_entries IS
'Trigger de integridad que prohibe DELETE directo y registra UPDATE directo como DB_DIRECT_MODIFY en audit_time_entries.
Esto es crítico para la cadena de hashes de cierre diario.';

COMMENT ON FUNCTION prevent_direct_modifications() IS
'Función que implementa la protección de integridad para time_entries.
- DELETE: PROHIBIDO (lanza excepción)
- UPDATE: Registra en audit_time_entries con action=DB_DIRECT_MODIFY
Esto permite que el sistema de verificación detecte modificaciones no autorizadas.';
