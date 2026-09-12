--liquibase formatted sql

--changeset knight:01 context:app,test
CREATE TABLE t_user (
    id SERIAL PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    user_password VARCHAR(255) NOT NULL,
    user_role VARCHAR(100) NOT NULL
);

--rollback DROP TABLE t_user;

CREATE TABLE t_doctor (
    doc_id SERIAL PRIMARY KEY,
    doc_name VARCHAR(255) NOT NULL,
    doc_specialization VARCHAR(255) NOT NULL,
    doc_appointment_status VARCHAR(100),
    shift_start TIMESTAMP,
    shift_end TIMESTAMP
);

--rollback DROP TABLE t_doctor;

CREATE TABLE t_patient (
    patient_id SERIAL PRIMARY KEY,
    patient_name VARCHAR(255)
);

--rollback DROP TABLE t_patient;

--changeset knight:02 context:app,test
CREATE TABLE t_appointment (
    appointment_id SERIAL PRIMARY KEY,
    appointment_status VARCHAR(100) NOT NULL,
    appointment_start_time TIMESTAMP NOT NULL,
    appointment_end_time TIMESTAMP NOT NULL,
    doc_appointment_id INT,
    patient_appointment_id INT,
    CONSTRAINT fk_doctor_appointment FOREIGN KEY (doc_appointment_id) REFERENCES t_doctor(doc_id),
    CONSTRAINT fk_patient_appointment FOREIGN KEY (patient_appointment_id) REFERENCES t_patient(patient_id)
);
-- rollback DROP TABLE t_appointment;

--changeset knight:03 context:app,test
CREATE TABLE t_live_queue (
    queue_id SERIAL PRIMARY KEY,
    queue_position BOOLEAN NOT NULL,
    queue_status VARCHAR(100) NOT NULL,
    appointment_id INT NOT NULL UNIQUE,
    doctor_id INT NOT NULL,
    queue_patient_id INT NOT NULL,
    appointment_start_time TIMESTAMP NOT NULL,
    appointment_end_time TIMESTAMP NOT NULL,
    CONSTRAINT fk_livequeue_appointment FOREIGN KEY (appointment_id) REFERENCES t_appointment(appointment_id),
    CONSTRAINT fk_livequeue_doctor FOREIGN KEY (doctor_id) REFERENCES t_doctor(doc_id),
    CONSTRAINT fk_livequeue_patient FOREIGN KEY (queue_patient_id) REFERENCES t_patient(patient_id)
);
-- rollback DROP TABLE t_live_queue;
