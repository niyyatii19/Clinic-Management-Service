--liquibase formatted sql

--changeset knight:01 context:app,test

CREATE TABLE t_doctor_schedule (
    id SERIAL PRIMARY KEY,
    doc_id BIGINT NOT NULL,
    shift_start_time TIME NOT NULL,
    shift_end_time TIME NOT NULL,
    shift_date DATE NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_doctor
        FOREIGN KEY (doc_id)
        REFERENCES t_doctor(doc_id)
        ON DELETE CASCADE
);
-- rollback drop table if exists t_doctor_schedule

CREATE TABLE t_staff_schedule (
    id SERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL,
    shift_start_time TIME NOT NULL,
    shift_end_time TIME NOT NULL,
    shift_date DATE NOT NULL,
    overtime_minutes BIGINT DEFAULT 0,
    CONSTRAINT fk_staff
        FOREIGN KEY (staff_id)
        REFERENCES t_staff(staff_id)
        ON DELETE CASCADE
);
-- rollback drop table if exists t_staff_schedule

ALTER TABLE t_staff
    DROP COLUMN IF EXISTS shift_start_time,
    DROP COLUMN IF EXISTS shift_end_time,
    DROP COLUMN IF EXISTS overtime_minutes;

ALTER TABLE t_doctor DROP COLUMN IF EXISTS is_available;

--changeset knight:02 context:app,test

ALTER TABLE t_appointment ADD COLUMN updated_by VARCHAR(100);
ALTER TABLE t_appointment ADD COLUMN update_reason TEXT;

--rollback alter table t_appointment drop column if exists updated_by;
--rollback alter table t_appointment drop column if exists update_reason;