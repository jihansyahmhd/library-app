-- src/main/resources/db/migration/V2__seed_books.sql
-- Isi: seed sample books
BEGIN;

-- pastikan extension ada (Postgres)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Insert sample books (id di-generate oleh uuid_generate_v4)
INSERT INTO books (id, isbn, title, author, created_at, updated_at) VALUES
                                                                        (uuid_generate_v4(), '9780140449136', 'Meditations', 'Marcus Aurelius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                        (uuid_generate_v4(), '9780132350884', 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                        (uuid_generate_v4(), '9780201616224', 'The Pragmatic Programmer', 'Andrew Hunt, David Thomas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                        (uuid_generate_v4(), '9780201633610', 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                        (uuid_generate_v4(), '9780262033848', 'Introduction to Algorithms', 'Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                        (uuid_generate_v4(), '9780547928227', 'The Hobbit', 'J.R.R. Tolkien', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
--     ON CONFLICT ON CONSTRAINT unique_isbn_title_author DO NOTHING;

-- COMMIT;
