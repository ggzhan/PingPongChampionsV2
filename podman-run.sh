#!/bin/bash

# Script to build and run the Ping Pong Champions app with Podman
# This script uses Podman instead of Docker

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}=== Building Ping Pong Champions with Podman ===${NC}"

# Check if .env file exists
if [ ! -f .env ]; then
    echo "Warning: .env file not found. Using default values."
fi

# Build the image
echo -e "${YELLOW}Building Docker image...${NC}"
podman build -t pingpong-champions .

echo -e "${GREEN}✓ Build complete!${NC}"
echo ""

# Stop and remove existing container if it exists
if podman ps -a --format "{{.Names}}" | grep -q "^pingpong-app$"; then
    echo -e "${YELLOW}Stopping existing container...${NC}"
    podman stop pingpong-app 2>/dev/null || true
    podman rm pingpong-app 2>/dev/null || true
fi

# Run the container
echo -e "${YELLOW}Starting container...${NC}"
if [ -f .env ]; then
    podman run -d \
        --name pingpong-app \
        -p 8080:8080 \
        --env-file .env \
        pingpong-champions
else
    podman run -d \
        --name pingpong-app \
        -p 8080:8080 \
        pingpong-champions
fi

echo -e "${GREEN}✓ Container started!${NC}"
echo ""
echo "Application is running at: http://localhost:8080"
echo ""
echo "Useful commands:"
echo "  View logs:        podman logs -f pingpong-app"
echo "  Stop container:   podman stop pingpong-app"
echo "  Start container:  podman start pingpong-app"
echo "  Remove container: podman rm -f pingpong-app"
echo ""
echo "To view logs now, run:"
echo "  podman logs -f pingpong-app"
