// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// Local Storage Keys
const TOKEN_KEY = 'auth_token';
const USER_KEY = 'user_info';

// Authentication Functions
async function register(username, email, password) {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, email, password }),
    });

    if (!response.ok) {
        throw new Error('Registration failed');
    }

    const data = await response.json();
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(USER_KEY, JSON.stringify({
        username: data.username,
        email: data.email
    }));

    return data;
}

async function login(username, password) {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
    });

    if (!response.ok) {
        throw new Error('Login failed');
    }

    const data = await response.json();
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(USER_KEY, JSON.stringify({
        username: data.username,
        email: data.email
    }));

    return data;
}

function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}

function isAuthenticated() {
    return localStorage.getItem(TOKEN_KEY) !== null;
}

function getCurrentUser() {
    const userStr = localStorage.getItem(USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
}

function getAuthHeaders() {
    const token = localStorage.getItem(TOKEN_KEY);
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
}

// League Functions
async function createLeague(name, description, isPublic) {
    const response = await fetch(`${API_BASE_URL}/leagues`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({ name, description, isPublic }),
    });

    if (!response.ok) {
        throw new Error('Failed to create league');
    }

    return await response.json();
}

async function getPublicLeagues() {
    const response = await fetch(`${API_BASE_URL}/leagues/public`);

    if (!response.ok) {
        throw new Error('Failed to fetch public leagues');
    }

    return await response.json();
}

async function getMyLeagues() {
    const response = await fetch(`${API_BASE_URL}/leagues/my`, {
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error('Failed to fetch my leagues');
    }

    return await response.json();
}

async function joinPublicLeague(leagueId) {
    const response = await fetch(`${API_BASE_URL}/leagues/${leagueId}/join`, {
        method: 'POST',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error('Failed to join league');
    }

    return await response.json();
}

async function joinPrivateLeague(inviteCode) {
    const response = await fetch(`${API_BASE_URL}/leagues/join-private`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({ inviteCode }),
    });

    if (!response.ok) {
        throw new Error('Failed to join private league');
    }

    return await response.json();
}

async function leaveLeague(leagueId) {
    const response = await fetch(`${API_BASE_URL}/leagues/${leagueId}/leave`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error('Failed to leave league');
    }
}

async function getLeagueDetails(leagueId) {
    const response = await fetch(`${API_BASE_URL}/leagues/${leagueId}`, {
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error('Failed to fetch league details');
    }

    return await response.json();
}

async function recordMatch(leagueId, winnerId, loserId) {
    const response = await fetch(`${API_BASE_URL}/leagues/${leagueId}/matches`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({ winnerId, loserId }),
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to record match');
    }
}

async function getMatches(leagueId) {
    const response = await fetch(`${API_BASE_URL}/leagues/${leagueId}/matches`, {
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error('Failed to fetch matches');
    }

    return await response.json();
}

// UI Helper Functions
function createLeagueCard(league, showJoinButton = false) {
    const card = document.createElement('div');
    card.className = 'league-card';

    const badge = league.isPublic ?
        '<span class="league-badge badge-public">Public</span>' :
        '<span class="league-badge badge-private">Private</span>';

    const inviteCode = !league.isPublic && league.inviteCode ?
        `<p style="color: var(--text-secondary); font-size: 0.875rem;">
            Invite Code: <strong style="color: var(--text-primary);">${league.inviteCode}</strong>
        </p>` : '';

    const joinButton = showJoinButton && league.isPublic ?
        `<button class="btn btn-primary" onclick="handleJoinLeague(${league.id})" style="width: 100%; margin-top: 1rem;">
            Join League
        </button>` : '';

    card.innerHTML = `
        ${badge}
        <h3>${league.name}</h3>
        <p>${league.description || 'No description'}</p>
        <p style="color: var(--text-secondary); font-size: 0.875rem;">
            Created by ${league.createdByUsername} • ${league.memberCount} member${league.memberCount !== 1 ? 's' : ''}
        </p>
        ${inviteCode}
        ${joinButton}
    `;

    // Make card clickable (except for join button)
    card.style.cursor = 'pointer';
    card.addEventListener('click', (e) => {
        // Don't navigate if clicking the join button
        if (!e.target.classList.contains('btn')) {
            window.location.href = `league.html?id=${league.id}`;
        }
    });

    return card;
}

async function loadPublicLeagues() {
    const container = document.getElementById('leagues-container');

    try {
        const leagues = await getPublicLeagues();

        if (leagues.length === 0) {
            container.innerHTML = '<p style="text-align: center; color: var(--text-secondary);">No public leagues available yet.</p>';
            return;
        }

        container.innerHTML = '';
        const grid = document.createElement('div');
        grid.className = 'leagues-grid';

        leagues.forEach(league => {
            grid.appendChild(createLeagueCard(league, false));
        });

        container.appendChild(grid);
    } catch (error) {
        container.innerHTML = '<p style="text-align: center; color: #f5576c;">Failed to load leagues</p>';
    }
}

async function loadMyLeagues() {
    const container = document.getElementById('my-leagues-container');

    try {
        const leagues = await getMyLeagues();

        if (leagues.length === 0) {
            container.innerHTML = '<p style="text-align: center; color: var(--text-secondary);">You haven\'t joined any leagues yet.</p>';
            return;
        }

        container.innerHTML = '';
        const grid = document.createElement('div');
        grid.className = 'leagues-grid';

        leagues.forEach(league => {
            grid.appendChild(createLeagueCard(league, false));
        });

        container.appendChild(grid);
    } catch (error) {
        container.innerHTML = '<p style="text-align: center; color: #f5576c;">Failed to load your leagues</p>';
    }
}

async function loadPublicLeaguesForDashboard() {
    const container = document.getElementById('public-leagues-container');

    try {
        const leagues = await getPublicLeagues();

        if (leagues.length === 0) {
            container.innerHTML = '<p style="text-align: center; color: var(--text-secondary);">No public leagues available.</p>';
            return;
        }

        container.innerHTML = '';
        const grid = document.createElement('div');
        grid.className = 'leagues-grid';

        leagues.forEach(league => {
            grid.appendChild(createLeagueCard(league, true));
        });

        container.appendChild(grid);
    } catch (error) {
        container.innerHTML = '<p style="text-align: center; color: #f5576c;">Failed to load public leagues</p>';
    }
}

async function handleJoinLeague(leagueId) {
    try {
        await joinPublicLeague(leagueId);
        alert('Successfully joined the league!');
        loadMyLeagues();
        loadPublicLeaguesForDashboard();
    } catch (error) {
        alert('Failed to join league. You may already be a member.');
    }
}
