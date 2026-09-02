Users & Courses

    users — id, email, name, password_hash, created_at
    courses — id, user_id, title, description, created_at (the course provided by the student to the AI)
    course_materials — id, course_id, content (plain text or extracted text), type (summary/notes/pdf-text), created_at → provides context to the AI

Concept Breakdown

    concepts — id, course_id, name, description, order → the AI breaks the course into trackable subtopics
    user_concept_mastery — id, user_id, concept_id, mastery_level (0–100 or enum), last_reviewed_at → drives adaptive difficulty and the “review before forgetting” logic

Quizzes / Practice

    quiz_questions — id, concept_id, question_text, difficulty (easy/medium/hard), created_at
    quiz_options — id, question_id, text, is_correct
    quiz_attempts — id, user_id, question_id, selected_option_id, is_correct, created_at

Feynman Method

    feynman_submissions — id, user_id, concept_id, explanation_text, ai_evaluation (text feedback), score, created_at

Conversational AI Tutor

    chat_sessions — id, user_id, course_id, created_at
    chat_messages — id, session_id, role (user/assistant), content, created_at

Daily Review

    review_schedule — id, user_id, concept_id, next_review_at, interval_days → simple spaced repetition, such as Leitner or simplified SM-2
