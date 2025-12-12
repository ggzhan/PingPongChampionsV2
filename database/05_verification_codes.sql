-- Verification Codes Table for Email Verification and Password Reset
-- Run this migration to add the verification_codes table

CREATE TABLE IF NOT EXISTS verification_codes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    code VARCHAR(6) NOT NULL,
    type VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index for faster lookups
CREATE INDEX IF NOT EXISTS idx_verification_codes_user_id ON verification_codes(user_id);
CREATE INDEX IF NOT EXISTS idx_verification_codes_code ON verification_codes(code);
CREATE INDEX IF NOT EXISTS idx_verification_codes_type ON verification_codes(type);

-- Add email_verified column to users table if it doesn't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE;

COMMENT ON TABLE verification_codes IS 'Stores verification codes for email verification and password reset';
COMMENT ON COLUMN verification_codes.type IS 'Type of code: EMAIL_VERIFY or PASSWORD_RESET';
COMMENT ON COLUMN verification_codes.expires_at IS 'Codes expire 15 minutes after creation';
