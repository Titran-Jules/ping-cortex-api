CREATE TABLE "user" (
    id UUID PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    password_hash VARCHAR(200) NOT NULL,
    level VARCHAR(75) NOT NULL,
    token_version INT NOT NULL DEFAULT 0,
    alert_threshold INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_token (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    token_hash VARCHAR(200) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_api_key (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    provider VARCHAR(100) NOT NULL,
    encrypted_key VARCHAR(200) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE analysis_status_list AS ENUM ('PENDING', 'ANALYZING', 'READY');

CREATE TABLE course (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    analysis_status analysis_status_list NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE material_type AS ENUM ('SUMMARY', 'NOTES', 'PDF');

CREATE TABLE course_material (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL REFERENCES course(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    type material_type NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE coverage_status_list AS ENUM ('NOT_COVERED', 'ANTICIPATED_COVERED', 'COVERED');

CREATE TABLE concept (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL REFERENCES course(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    position INT NOT NULL,
    coverage_status coverage_status_list NOT NULL DEFAULT 'NOT_COVERED'
);

CREATE TABLE user_concept_mastery (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    concept_id UUID NOT NULL REFERENCES concept(id) ON DELETE CASCADE,
    mastery_level INT NOT NULL DEFAULT 0,
    last_reviewed_at TIMESTAMP,
    UNIQUE (user_id, concept_id)
);

CREATE TYPE difficulty_list AS ENUM ('EASY', 'MEDIUM', 'HARD');

CREATE TABLE quiz_question (
    id UUID PRIMARY KEY,
    concept_id UUID NOT NULL REFERENCES concept(id) ON DELETE CASCADE,
    question_text VARCHAR(250) NOT NULL,
    difficulty difficulty_list NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE quiz_option (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL REFERENCES quiz_question(id) ON DELETE CASCADE,
    text VARCHAR(200) NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE quiz_attempt (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES quiz_question(id) ON DELETE CASCADE,
    selected_option_id UUID NOT NULL REFERENCES quiz_option(id) ON DELETE CASCADE,
    is_correct BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE feynman_submission (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    concept_id UUID NOT NULL REFERENCES concept(id) ON DELETE CASCADE,
    explanation_text TEXT NOT NULL,
    ai_evaluation JSONB NOT NULL DEFAULT '{}'::jsonb,
    score FLOAT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE chat_session (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES course(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE chat_role_list AS ENUM ('USER', 'ASSISTANT');

CREATE TABLE chat_message (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES chat_session(id) ON DELETE CASCADE,
    role chat_role_list NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE review_schedule (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    concept_id UUID NOT NULL REFERENCES concept(id) ON DELETE CASCADE,
    next_review_at TIMESTAMP NOT NULL,
    interval_days INT NOT NULL DEFAULT 1,
    UNIQUE (user_id, concept_id)
);

CREATE TABLE ai_usage_log (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    session_id UUID REFERENCES chat_session(id) ON DELETE SET NULL,
    token_in INT NOT NULL,
    token_out INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token(user_id);
CREATE INDEX idx_user_api_key_user_id ON user_api_key(user_id);
CREATE INDEX idx_course_user_id ON course(user_id);
CREATE INDEX idx_course_material_course_id ON course_material(course_id);
CREATE INDEX idx_concept_course_id ON concept(course_id);
CREATE INDEX idx_user_concept_mastery_concept_id ON user_concept_mastery(concept_id);
CREATE INDEX idx_quiz_question_concept_id ON quiz_question(concept_id);
CREATE INDEX idx_quiz_option_question_id ON quiz_option(question_id);
CREATE INDEX idx_quiz_attempt_user_id ON quiz_attempt(user_id);
CREATE INDEX idx_quiz_attempt_question_id ON quiz_attempt(question_id);
CREATE INDEX idx_feynman_submission_user_id ON feynman_submission(user_id);
CREATE INDEX idx_feynman_submission_concept_id ON feynman_submission(concept_id);
CREATE INDEX idx_chat_session_user_id ON chat_session(user_id);
CREATE INDEX idx_chat_session_course_id ON chat_session(course_id);
CREATE INDEX idx_chat_message_session_id ON chat_message(session_id);
CREATE INDEX idx_review_schedule_next_review_at ON review_schedule(next_review_at);
CREATE INDEX idx_ai_usage_log_user_id ON ai_usage_log(user_id);