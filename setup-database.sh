#!/bin/bash

# Script to run PostgreSQL setup files
# This script reads database credentials from .env file and runs the SQL scripts

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== PostgreSQL Setup Script ===${NC}"

# Check if .env file exists
if [ ! -f .env ]; then
    echo -e "${RED}Error: .env file not found!${NC}"
    echo "Please create a .env file with your database credentials."
    exit 1
fi

# Load environment variables from .env file
export $(cat .env | grep -v '^#' | xargs)

# Check if required variables are set
if [ -z "$POSTGRESQL_ADDON_HOST" ] || [ -z "$POSTGRESQL_ADDON_PORT" ] || \
   [ -z "$POSTGRESQL_ADDON_DB" ] || [ -z "$POSTGRESQL_ADDON_USER" ] || \
   [ -z "$POSTGRESQL_ADDON_PASSWORD" ]; then
    echo -e "${RED}Error: Missing required environment variables!${NC}"
    echo "Please ensure .env file contains:"
    echo "  - POSTGRESQL_ADDON_HOST"
    echo "  - POSTGRESQL_ADDON_PORT"
    echo "  - POSTGRESQL_ADDON_DB"
    echo "  - POSTGRESQL_ADDON_USER"
    echo "  - POSTGRESQL_ADDON_PASSWORD"
    exit 1
fi

# Construct PostgreSQL connection string
export PGPASSWORD="$POSTGRESQL_ADDON_PASSWORD"
DB_URL="postgresql://$POSTGRESQL_ADDON_USER@$POSTGRESQL_ADDON_HOST:$POSTGRESQL_ADDON_PORT/$POSTGRESQL_ADDON_DB"

echo -e "${YELLOW}Connecting to database:${NC}"
echo "  Host: $POSTGRESQL_ADDON_HOST"
echo "  Port: $POSTGRESQL_ADDON_PORT"
echo "  Database: $POSTGRESQL_ADDON_DB"
echo "  User: $POSTGRESQL_ADDON_USER"
echo ""

# Check if psql is installed
if ! command -v psql &> /dev/null; then
    echo -e "${RED}Error: psql is not installed!${NC}"
    echo "Please install PostgreSQL client:"
    echo "  Ubuntu/Debian: sudo apt-get install postgresql-client"
    echo "  macOS: brew install postgresql"
    echo "  Arch: sudo pacman -S postgresql"
    exit 1
fi

# Test database connection
echo -e "${YELLOW}Testing database connection...${NC}"
if psql "$DB_URL" -c "SELECT version();" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Connection successful!${NC}"
else
    echo -e "${RED}✗ Connection failed!${NC}"
    echo "Please check your database credentials in .env file."
    exit 1
fi

# Run schema creation script
if [ -f "database/01_create_tables.sql" ]; then
    echo -e "${YELLOW}Running schema creation script...${NC}"
    psql "$DB_URL" -f database/01_create_tables.sql
    echo -e "${GREEN}✓ Schema created successfully!${NC}"
else
    echo -e "${RED}Warning: database/01_create_tables.sql not found${NC}"
fi

# Ask if user wants to load sample data
echo ""
read -p "Do you want to load sample data? (y/n) " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    if [ -f "database/02_sample_data.sql" ]; then
        echo -e "${YELLOW}Loading sample data...${NC}"
        psql "$DB_URL" -f database/02_sample_data.sql
        echo -e "${GREEN}✓ Sample data loaded successfully!${NC}"
    else
        echo -e "${RED}Warning: database/02_sample_data.sql not found${NC}"
    fi
else
    echo -e "${YELLOW}Skipping sample data.${NC}"
fi

# Run ELO rating script
if [ -f "database/03_add_elo_rating.sql" ]; then
    echo -e "${YELLOW}Running ELO rating script...${NC}"
    psql "$DB_URL" -f database/03_add_elo_rating.sql
    echo -e "${GREEN}✓ ELO rating script executed successfully!${NC}"
else
    echo -e "${RED}Warning: database/03_add_elo_rating.sql not found${NC}"
fi

 if [ -f "database/04_create_matches_table.sql" ]; then
        echo -e "${YELLOW}Creating matches table...${NC}"
        psql "$DB_URL" -f database/04_create_matches_table.sql
        echo -e "${GREEN}✓ Matches table created successfully!${NC}"
    else
        echo -e "${RED}Warning: database/04_create_matches_table.sql not found${NC}"
    fi

# Verify tables were created
echo ""
echo -e "${YELLOW}Verifying tables...${NC}"
psql "$DB_URL" -c "\dt" 2>/dev/null || true

echo ""
echo -e "${GREEN}=== Setup Complete! ===${NC}"
echo ""
echo "You can now run the Spring Boot application:"
echo "  mvn spring-boot:run"
echo "  or"
echo "  docker-compose up"

# Unset password
unset PGPASSWORD
