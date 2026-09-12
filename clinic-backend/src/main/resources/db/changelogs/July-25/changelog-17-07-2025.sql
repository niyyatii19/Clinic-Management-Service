--liquibase formatted sql

--changeset knight:01 context:app,test

ALTER TABLE t_appointment ADD COLUMN reason_for_visit VARCHAR(255);

--rollback ALTER TABLE t_appointment DROP COLUMN reason_for_visit;

--changeset knight:02 context:app,test

ALTER TABLE t_doctor ADD COLUMN is_available BOOLEAN DEFAULT true not null;

--rollback ALTER TABLE t_doctor DROP COLUMN is_available;

--changeset knight:03 context:app,test

ALTER TABLE t_patient ADD COLUMN emergency_contact_name VARCHAR(100);
ALTER TABLE t_patient ADD COLUMN emergency_contact_number VARCHAR(20);
ALTER TABLE t_patient ADD COLUMN gender VARCHAR(10);

--rollback ALTER TABLE t_patient DROP COLUMN emergency_contact_name;
--rollback ALTER TABLE t_patient DROP COLUMN emergency_contact_number;
--rollback ALTER TABLE t_patient DROP COLUMN gender;