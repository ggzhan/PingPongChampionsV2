-- Sample Data for Testing
-- Note: Passwords are BCrypt hashed version of "password123"

-- Insert sample users
INSERT INTO users (username, email, password) VALUES
('john_doe', 'john@example.com', '$2a$12$47B2oR.ipiY0nfu0G07HHevuomgpnJeW81jFLV0ZkUprvQDoBvxMa'),
('jane_smith', 'jane@example.com', '$2a$12$47B2oR.ipiY0nfu0G07HHevuomgpnJeW81jFLV0ZkUprvQDoBvxMa'),
('bob_wilson', 'bob@example.com', '$2a$12$47B2oR.ipiY0nfu0G07HHevuomgpnJeW81jFLV0ZkUprvQDoBvxMa'),
('alice_brown', 'alice@example.com', '$2a$12$47B2oR.ipiY0nfu0G07HHevuomgpnJeW81jFLV0ZkUprvQDoBvxMa');

-- Insert sample leagues
INSERT INTO leagues (name, description, is_public, invite_code, created_by) VALUES
('City Champions', 'Open league for all city players', true, NULL, 1),
('Office League', 'Weekly office ping pong tournament', true, NULL, 2),
('Elite Players', 'Private league for advanced players', false, 'ELITE2024', 1),
('Weekend Warriors', 'Casual weekend games', false, 'WEEKEND99', 3);

-- Insert league memberships
INSERT INTO league_members (user_id, league_id, role, elo) VALUES
-- City Champions members
(1, 1, 'OWNER', 1200),
(2, 1, 'MEMBER', 1100),
(3, 1, 'MEMBER', 950),

-- Office League members
(2, 2, 'OWNER', 1000),
(1, 2, 'MEMBER', 1050),
(4, 2, 'MEMBER', 900),

-- Elite Players members
(1, 3, 'OWNER', 1500),
(4, 3, 'MEMBER', 1400),

-- Weekend Warriors members
(3, 4, 'OWNER', 1000),
(2, 4, 'MEMBER', 1000);

-- Insert sample matches
INSERT INTO matches (league_id, winner_id, loser_id, winner_elo_change, loser_elo_change) VALUES
-- City Champions matches
(1, 1, 2, 15, -15),
(1, 2, 3, 12, -12),
(1, 1, 3, 10, -10),

-- Office League matches
(2, 1, 2, 20, -20),
(2, 2, 4, 15, -15);
