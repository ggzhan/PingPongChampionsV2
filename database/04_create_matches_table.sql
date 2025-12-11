-- Create matches table
CREATE TABLE IF NOT EXISTS matches (
    id BIGSERIAL PRIMARY KEY,
    league_id BIGINT NOT NULL REFERENCES leagues(id),
    winner_id BIGINT NOT NULL REFERENCES users(id),
    loser_id BIGINT NOT NULL REFERENCES users(id),
    winner_elo_change INTEGER NOT NULL,
    loser_elo_change INTEGER NOT NULL,
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add index for faster lookups
CREATE INDEX idx_matches_league_id ON matches(league_id);
CREATE INDEX idx_matches_winner_id ON matches(winner_id);
CREATE INDEX idx_matches_loser_id ON matches(loser_id);
