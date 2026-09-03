User

    user — id, email, name, password_hash, level (ex : First year in informatic), token_version, alert_threshold, created_at
    refresh_token - id, user_id, token_hash, expires_at, revoked (bool), created_at
    user_api_key - id, user_id, provider, encrypted_key, created_at

Course

    course — id, user_id, title, description, active, analysis_status (pending/analyzing/ready), created_at (the course provided by the student to the AI)
    course_material — id, course_id, content (plain text or extracted text), type (summary/notes/pdf-text), created_at → provides context to the AI

Concept Breakdown

    concept — id, course_id, name, description, order → the AI breaks the course into trackable subtopics
    user_concept_mastery — id, user_id, concept_id, mastery_level (0–100 or enum), last_reviewed_at → drives adaptive difficulty and the “review before forgetting” logic

Quizze / Practice

    quiz_question — id, concept_id, question_text, difficulty (easy/medium/hard), created_at
    quiz_option — id, question_id, text, is_correct
    quiz_attempt — id, user_id, question_id, selected_option_id, is_correct, created_at

Feynman Method

    feynman_submission — id, user_id, concept_id, explanation_text, ai_evaluation (text feedback), score, created_at

Conversational AI Tutor

    chat_session — id, user_id, course_id, created_at
    chat_message — id, session_id, role (user/assistant), content, created_at

Daily Review

    review_schedule — id, user_id, concept_id, next_review_at, interval_days → simple spaced repetition, such as Leitner or simplified SM-2
    ai_usage_log - id, user_id, session_id, token_in, token_out, created_at
