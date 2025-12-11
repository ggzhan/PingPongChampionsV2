-- Sample Data for Testing
-- Note: Passwords are BCrypt hashed version of "password123"

-- Insert sample users
INSERT INTO users (username, email, password) VALUES
('john_doe', 'john@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('jane_smith', 'jane@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('bob_wilson', 'bob@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('alice_brown', 'alice@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

-- Insert sample leagues
INSERT INTO leagues (name, description, is_public, invite_code, created_by) VALUES
('City Champions', 'Open league for all city players', true, NULL, 1),
('Office League', 'Weekly office ping pong tournament', true, NULL, 2),
('Elite Players', 'Private league for advanced players', false, 'ELITE2024', 1),
('Weekend Warriors', 'Casual weekend games', false, 'WEEKEND99', 3);

-- Insert league memberships
INSERT INTO league_members (user_id, league_id, role) VALUES
-- City Champions members
(1, 1, 'OWNER'),
(2, 1, 'MEMBER'),
(3, 1, 'MEMBER'),

-- Office League members
(2, 2, 'OWNER'),
(1, 2, 'MEMBER'),
(4, 2, 'MEMBER'),

-- Elite Players members
(1, 3, 'OWNER'),
(4, 3, 'MEMBER'),

-- Weekend Warriors members
(3, 4, 'OWNER'),
(2, 4, 'MEMBER');
