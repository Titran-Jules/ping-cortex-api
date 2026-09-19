ALTER TABLE concept
    ADD COLUMN suggested_coverage BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN suggested_from_material_id UUID REFERENCES course_material(id) ON DELETE SET NULL;