ALTER TABLE review_schedule
    ADD COLUMN easiness_factor FLOAT NOT NULL DEFAULT 2.5,
    ADD COLUMN repetition_count INT NOT NULL DEFAULT 0;