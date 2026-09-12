--liquibase formatted sql

--changeset knight:01 context:app,test

-- USERS table
ALTER TABLE t_user ADD COLUMN created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE t_user ADD COLUMN updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE t_user ADD COLUMN number VARCHAR(20);
ALTER TABLE t_user ADD COLUMN user_dob DATE;
ALTER TABLE t_user ADD COLUMN contact_number VARCHAR(20);

-- ALTER TABLE t_user DROP COLUMN contact_number;
-- ALTER TABLE t_user DROP COLUMN user_dob;
-- ALTER TABLE t_user DROP COLUMN number;
-- ALTER TABLE t_user DROP COLUMN updated_on;
-- ALTER TABLE t_user DROP COLUMN created_on;

ALTER TABLE t_doctor ADD COLUMN created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE t_doctor ADD COLUMN updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE t_doctor ADD COLUMN contact_number VARCHAR(20);
ALTER TABLE t_doctor ADD COLUMN doc_dob DATE;
ALTER TABLE t_doctor ADD COLUMN user_id BIGINT UNIQUE;

ALTER TABLE t_doctor ADD CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES t_user(id);

-- ALTER TABLE t_doctor DROP CONSTRAINT fk_doctor_user;
-- ALTER TABLE t_doctor DROP COLUMN user_id;
-- ALTER TABLE t_doctor DROP COLUMN doc_dob;
-- ALTER TABLE t_doctor DROP COLUMN contact_number;
-- ALTER TABLE t_doctor DROP COLUMN updated_on;
-- ALTER TABLE t_doctor DROP COLUMN created_on;

ALTER TABLE t_patient ADD COLUMN created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE t_patient ADD COLUMN updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE t_patient ADD COLUMN contact_number VARCHAR(20);
ALTER TABLE t_patient ADD COLUMN patient_dob DATE;
ALTER TABLE t_patient ADD COLUMN user_id BIGINT UNIQUE;

ALTER TABLE t_patient ADD CONSTRAINT fk_patient_user FOREIGN KEY (user_id) REFERENCES t_user(id);

-- ALTER TABLE t_patient DROP CONSTRAINT fk_patient_user;
-- ALTER TABLE t_patient DROP COLUMN user_id;
-- ALTER TABLE t_patient DROP COLUMN patient_dob;
-- ALTER TABLE t_patient DROP COLUMN contact_number;
-- ALTER TABLE t_patient DROP COLUMN updated_on;
-- ALTER TABLE t_patient DROP COLUMN created_on;

--changeset knight:02 context:app,test

CREATE TABLE t_staff (
    staff_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    staff_dob DATE NOT NULL,
    staff_position VARCHAR(255) NOT NULL,
    contact_number VARCHAR(20),
    overtime_minutes INT DEFAULT 0,
    shift_start_time TIME,
    shift_end_time TIME,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT UNIQUE,
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES t_user(id)
);
-- DROP TABLE t_staff;

--changeset knight:03 context:app,test

ALTER TABLE t_user DROP COLUMN number;
Alter table t_user add column full_name VARCHAR(255) NOT NULL;
--rollback alter table t_user drop column full_name;