--liquibase formatted sql

--changeset knight:01 context:app,test
CREATE TABLE IF NOT EXISTS t_database_changelog (
    id VARCHAR(255) PRIMARY KEY,
    author VARCHAR(255),
    filename VARCHAR(255),
    dateexecuted TIMESTAMP,
    orderexecuted INT,
    exec_type VARCHAR(10),
    md5sum VARCHAR(35),
    description VARCHAR(255),
    comments VARCHAR(255),
    tag VARCHAR(255),
    liquibase VARCHAR(20),
    contexts VARCHAR(255),
    labels VARCHAR(255),
    deployment_id VARCHAR(10)
);

--rollback drop table t_database_changelog
CREATE TABLE IF NOT EXISTS t_database_changelog_lock (
    id INT PRIMARY KEY,
    locked BOOLEAN,
    lockgranted TIMESTAMP,
    lockedby VARCHAR(255)
);
--rollback drop table t_database_changelog_lock

INSERT INTO t_database_changelog_lock (id, locked) VALUES (1, false)
    ON CONFLICT (id) DO NOTHING;
