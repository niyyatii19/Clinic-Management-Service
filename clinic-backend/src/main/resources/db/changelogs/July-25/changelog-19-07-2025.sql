--liquibase formatted sql

--changeset knight:01 context:app,test
ALTER TABLE t_appointment ADD COLUMN appointment_date DATE NOT NULL;
ALTER TABLE t_appointment ALTER COLUMN appointment_start_time TYPE TIME USING appointment_start_time::time;
ALTER TABLE t_appointment ALTER COLUMN appointment_end_time TYPE TIME USING appointment_end_time::time;

--rollback Alter table drop column if exists appointment_date;

--changeset knight:02 context:app,test
ALTER TABLE t_doctor DROP COLUMN IF EXISTS doc_appointment_status;
ALTER TABLE t_doctor DROP COLUMN IF EXISTS shift_start;
ALTER TABLE t_doctor DROP COLUMN IF EXISTS shift_end;

