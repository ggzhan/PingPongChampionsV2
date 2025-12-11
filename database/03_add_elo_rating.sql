-- Add ELO rating column to league_members table
ALTER TABLE league_members ADD COLUMN IF NOT EXISTS elo INTEGER DEFAULT 1000;

-- Update existing members to have default ELO of 1000
UPDATE league_members SET elo = 1000 WHERE elo IS NULL;

-- Add constraint to ensure ELO is always positive
ALTER TABLE league_members ADD CONSTRAINT elo_positive CHECK (elo > 0);

-- Add comment
COMMENT ON COLUMN league_members.elo IS 'Player ELO rating in this league, starts at 1000';
