-- V2__insert_sample_borrowers.sql
-- Make sure uuid_generate_v4() is available; create extension if needed.
-- If you don't have permission to create extensions, remove this or use gen_random_uuid() from pgcrypto.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO borrowers (id, name, email, created_at, updated_at) VALUES
                                                                    (uuid_generate_v4(), 'Aisha Rahman',    'aisha.rahman@example.com',    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (uuid_generate_v4(), 'Daniel Tan',      'daniel.tan@example.com',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (uuid_generate_v4(), 'Siti Nur',        'siti.nur@example.com',        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (uuid_generate_v4(), 'John Smith',      'john.smith@example.com',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                    (uuid_generate_v4(), 'Maria Gonzales',  'maria.gonzales@example.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (email) DO NOTHING;
