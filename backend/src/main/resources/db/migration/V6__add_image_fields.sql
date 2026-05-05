-- Add image fields to items table
ALTER TABLE items
ADD COLUMN cover_image VARCHAR(500) NULL,
ADD COLUMN images TEXT NULL;
