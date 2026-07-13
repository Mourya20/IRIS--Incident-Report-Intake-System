CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

CREATE TABLE incident (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    severity_band VARCHAR(20) NOT NULL,
    severity_score INTEGER NOT NULL DEFAULT 0,
    reporter_id BIGINT NOT NULL,
    assignee_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_incident_reporter FOREIGN KEY (reporter_id) REFERENCES app_user(id),
    CONSTRAINT fk_incident_assignee FOREIGN KEY (assignee_id) REFERENCES app_user(id)
);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT,
    actor_user_id BIGINT NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    details_json TEXT NOT NULL,
    prev_hash VARCHAR(64) NOT NULL,
    entry_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_incident FOREIGN KEY (incident_id) REFERENCES incident(id),
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES app_user(id)
);

CREATE TABLE severity_rule (
    id BIGSERIAL PRIMARY KEY,
    rule_key VARCHAR(100) NOT NULL UNIQUE,
    weight INTEGER NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    metadata_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attack_technique (
    technique_id VARCHAR(20) PRIMARY KEY,
    tactic VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT
);

CREATE TABLE incident_technique (
    incident_id BIGINT NOT NULL,
    technique_id VARCHAR(20) NOT NULL,
    PRIMARY KEY (incident_id, technique_id),
    CONSTRAINT fk_incident_technique_incident FOREIGN KEY (incident_id) REFERENCES incident(id) ON DELETE CASCADE,
    CONSTRAINT fk_incident_technique_technique FOREIGN KEY (technique_id) REFERENCES attack_technique(technique_id)
);

CREATE TABLE ioc (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    type VARCHAR(100) NOT NULL,
    ioc_value VARCHAR(500) NOT NULL,
    normalized_score INTEGER,
    source_provider VARCHAR(100),
    raw_response_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ioc_incident FOREIGN KEY (incident_id) REFERENCES incident(id) ON DELETE CASCADE,
    CONSTRAINT uq_ioc_cache UNIQUE (incident_id, type, ioc_value)
);

CREATE INDEX idx_incident_reporter ON incident(reporter_id);
CREATE INDEX idx_incident_assignee ON incident(assignee_id);
CREATE INDEX idx_incident_status ON incident(status);
CREATE INDEX idx_incident_severity ON incident(severity_band);
CREATE INDEX idx_audit_incident_created ON audit_log(incident_id, created_at);
