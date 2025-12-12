-- Ping Pong Champions Database Schema
-- PostgreSQL Create Scripts

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS matches CASCADE;
DROP TABLE IF EXISTS league_members CASCADE;
DROP TABLE IF EXISTS leagues CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Leagues Table
CREATE TABLE leagues (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT true,
    invite_code VARCHAR(20) UNIQUE,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- League Members Table (Junction Table)
CREATE TABLE league_members (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    league_id BIGINT NOT NULL,
    role VARCHAR(20) DEFAULT 'MEMBER',
    elo INTEGER DEFAULT 1000 CHECK (elo > 0),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (league_id) REFERENCES leagues(id) ON DELETE CASCADE,
    UNIQUE(user_id, league_id)
);

-- Matches Table
CREATE TABLE matches (
    id BIGSERIAL PRIMARY KEY,
    league_id BIGINT NOT NULL,
    winner_id BIGINT NOT NULL,
    loser_id BIGINT NOT NULL,
    winner_elo_change INTEGER NOT NULL,
    loser_elo_change INTEGER NOT NULL,
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (league_id) REFERENCES leagues(id) ON DELETE CASCADE,
    FOREIGN KEY (winner_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (loser_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for better query performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_leagues_is_public ON leagues(is_public);
CREATE INDEX idx_leagues_invite_code ON leagues(invite_code);
CREATE INDEX idx_leagues_created_by ON leagues(created_by);
CREATE INDEX idx_league_members_user_id ON league_members(user_id);
CREATE INDEX idx_league_members_league_id ON league_members(league_id);
CREATE INDEX idx_matches_league_id ON matches(league_id);
CREATE INDEX idx_matches_winner_id ON matches(winner_id);
CREATE INDEX idx_matches_loser_id ON matches(loser_id);
CREATE INDEX idx_matches_played_at ON matches(played_at);

-- Comments for documentation
COMMENT ON TABLE users IS 'Stores user account information';
COMMENT ON TABLE leagues IS 'Stores league information (public and private)';
COMMENT ON TABLE league_members IS 'Junction table for user-league membership';
COMMENT ON TABLE matches IS 'Stores match history and ELO changes';
COMMENT ON COLUMN leagues.invite_code IS 'Unique code for joining private leagues';
COMMENT ON COLUMN league_members.role IS 'User role in league: OWNER, ADMIN, or MEMBER';
COMMENT ON COLUMN league_members.elo IS 'Player ELO rating in this league, starts at 1000';
