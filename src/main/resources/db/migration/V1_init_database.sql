CREATE TABLE "user" (
    id UUID PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    name VARCHAR(150) NOT NULL,
    password_hash VARCHAR(200) NOT NULL,
    level VARCHAR(75) NOT NULL,
    token_version INT,
    alert_threshold INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_token (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES "user"(id),
    token_hash VARCHAR(200) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_api_key (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES "user"(id),
    provider VARCHAR(100) NOT NULL,
    encrypted_key VARCHAR(200) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE analysis_status_list AS ENUM ('PENDING', 'ANALYZING', 'READY');

CREATE TABLE course (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES "user"(id),
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    is_active BOOLEAN NOT NULL,
    analysis_status analysis_status_list DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE material_type AS ENUM ('SUMMARY', 'NOTES', 'PDF');

CREATE TABLE user_material (
    id UUID PRIMARY KEY,
    course_id UUID REFERENCES course(id),
    content TEXT,
    type material_type,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE concept (
    id UUID PRIMARY KEY,
    course_id UUID REFERENCES course(id),
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    order int NOT NULL
);

CREATE TABLE user_concept_mastery (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES "user"(id),
    concept_id UUID REFERENCES concept(id),
    mastery_level int NOT NULL,
    last_reviewed_at TIMESTAMP NOT NULL,
);

CREATE TYPE difficulties AS ENUM ('EASY', 'MEDIUM', 'HARD');

CREATE TABLE quiz_question (
    id UUID PRIMARY KEY,
    concept_id UUID REFERENCES concept(id),
    question_text VARCHAR(250) NOT NULL,
    difficulty difficulties NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE quiz_option (
    id UUID PRIMARY KEY,
    question_id UUID REFERENCES quiz_question(id),
    text VARCHAR(200) NOT NULL,
    is_correct BOOLEAN NOT NULL
);

CREATE TABLE quiz_attempt (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES "user"(id),
    question_id UUID REFERENCES quiz_question(id),
    selected_option_id UUID REFERENCES quiz_option(id),
    is_correct BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE feynman_submission (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES "user"(id),
    concept_id UUID REFERENCES concept(id),
    explanation_text TEXT NOT NULL,
    ai_evaluation jsonb DEFAULT '{}'::jsonb,
    score float NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE chat_session (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES "user"(id),
    course_id UUID REFERENCES course(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE roles AS ENUM ('USER', 'ASSISTANT')

CREATE TABLE chat_message (
    id UUID PRIMARY KEY,
    session_id UUID REFERENCES chat_session(id),
    role roles NOT NULL,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE review_schedule (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES "user"(id),
    concept_id UUID REFERENCES concept(id),
    next_review_at TIMESTAMP,
    interval_days INT
);

CREATE TABLE ai_usage_log (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES "user"(id),
    session_id UUID REFERENCES chat_session(id),
    token_in NUMERIC(10, 4),
    token_out NUMERIC(10, 4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);