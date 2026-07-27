#!/bin/bash

# Docker Build Script for Exchange Modules
# Usage: ./build-docker.sh <module-name> [image-tag]
# Example: ./build-docker.sh ce latest
#          ./build-docker.sh gateway v1.0

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Valid modules
VALID_MODULES=("ce" "gateway" "oms" "me" "wallet" "dp" "emailmanager" "profile" "mm")

# Function to print usage
print_usage() {
    echo "Usage: $0 <module-name> [port] [image-tag]"
    echo ""
    echo "Valid modules:"
    printf '%s\n' "${VALID_MODULES[@]}" | sed 's/^/  - /'
    echo ""
    echo "Examples:"
    echo "  $0 ce 8080 latest"
    echo "  $0 gateway 8080 v1.0"
    echo "  $0 oms 8080"
    echo "  $0 oms //default port 8080"
}

# Function to print colored messages
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Validate inputs
if [ $# -lt 1 ]; then
    print_error "Module name is required"
    echo ""
    print_usage
    exit 1
fi

MODULE=$1
PORT=${2:-8080}
TAG=${3:-latest}

# Validate module
if [[ ! " ${VALID_MODULES[@]} " =~ " ${MODULE} " ]]; then
    print_error "Invalid module: $MODULE"
    echo ""
    print_usage
    exit 1
fi

# Check if Dockerfile exists
if [ ! -f "$MODULE/Dockerfile" ]; then
    print_error "Dockerfile not found at $MODULE/Dockerfile"
    exit 1
fi

# Get the script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

print_info "Building Docker image for module: $MODULE"
print_info "Image tag: $TAG"
print_info "Working directory: $SCRIPT_DIR"
echo ""

# Build the Docker image
IMAGE_NAME="exchange-${MODULE}:${TAG}"
print_info "Building image: $IMAGE_NAME"
echo ""

if docker build \
    -f "$SCRIPT_DIR/$MODULE/Dockerfile" \
    -t "$IMAGE_NAME" \
    "$SCRIPT_DIR"; then

    print_info "Docker image built successfully!"
    print_info "Image name: $IMAGE_NAME"
    echo ""

    # Display image info
    echo "Image details:"
    docker images "$IMAGE_NAME"
    echo ""

    print_info "Running container:"
    docker-compose rm -f $MODULE
    docker-compose up -d $MODULE

else
    print_error "Failed to build Docker image"
    exit 1
fi

# List all built exchange images
# echo ""
# print_info "All exchange images:"
# docker images 'exchange-*' --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.Created}}"