-- Create extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create borrowers table
CREATE TABLE borrowers (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           name VARCHAR(255) NOT NULL,
                           email VARCHAR(255) NOT NULL UNIQUE,
                           created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create books table
CREATE TABLE books (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       isbn VARCHAR(20) NOT NULL,
                       title VARCHAR(500) NOT NULL,
                       author VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP

    -- Constraint to ensure books with same ISBN have same title and author
--                        CONSTRAINT unique_isbn_title_author UNIQUE (isbn, title, author)
);

-- Create borrowing_records table to track who borrows which book and for how long
CREATE TABLE borrowing_records (
                                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   borrower_id UUID NOT NULL REFERENCES borrowers(id) ON DELETE CASCADE,
                                   book_id UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
                                   borrowed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                   returned_at TIMESTAMP WITH TIME ZONE NULL,
                                   due_date TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP + INTERVAL '14 days'),
                                   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Ensure one book can only be borrowed by one person at a time (if not returned)
                                   CONSTRAINT unique_active_book_borrowing
                                       EXCLUDE (book_id WITH =) WHERE (returned_at IS NULL)
);

-- Create indexes for better performance
CREATE INDEX idx_borrowers_email ON borrowers(email);
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_borrowing_records_borrower_id ON borrowing_records(borrower_id);
CREATE INDEX idx_borrowing_records_book_id ON borrowing_records(book_id);
CREATE INDEX idx_borrowing_records_borrowed_at ON borrowing_records(borrowed_at);
CREATE INDEX idx_borrowing_records_returned_at ON borrowing_records(returned_at);
CREATE INDEX idx_borrowing_records_active ON borrowing_records(book_id) WHERE returned_at IS NULL;

-- Create function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers to automatically update updated_at
CREATE TRIGGER update_borrowers_updated_at
    BEFORE UPDATE ON borrowers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_books_updated_at
    BEFORE UPDATE ON books
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_borrowing_records_updated_at
    BEFORE UPDATE ON borrowing_records
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();