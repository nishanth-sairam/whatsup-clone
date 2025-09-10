#!/bin/bash

# WhatsApp Clone Docker Management Script
# Author: Generated for WhatsApp Clone Project
# Date: $(date +%Y-%m-%d)

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Project configuration
PROJECT_NAME="whatsupclone"
COMPOSE_FILE="docker-compose.yaml"
LOG_DIR="logs"

# Service names from docker-compose.yaml
SERVICES=("postgres" "keycloak" "whatsup-server" "whatsup-client")

# Create logs directory if it doesn't exist
mkdir -p $LOG_DIR

# Helper functions
print_header() {
    echo -e "${CYAN}========================================${NC}"
    echo -e "${CYAN} WhatsApp Clone Docker Manager${NC}"
    echo -e "${CYAN}========================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        print_error "Docker Compose is not installed or not available"
        exit 1
    fi
}

get_compose_cmd() {
    if docker compose version &> /dev/null; then
        echo "docker compose"
    else
        echo "docker-compose"
    fi
}

# Main functions
show_help() {
    print_header
    echo -e "${YELLOW}Usage: $0 [COMMAND] [OPTIONS]${NC}"
    echo ""
    echo -e "${CYAN}MAIN COMMANDS:${NC}"
    echo "  up                    Start all services"
    echo "  down                  Stop all services"
    echo "  restart               Restart all services"
    echo "  build                 Build all services"
    echo "  rebuild               Rebuild all services (no cache)"
    echo ""
    echo -e "${CYAN}SERVICE-SPECIFIC COMMANDS:${NC}"
    echo "  up <service>          Start specific service"
    echo "  down <service>        Stop specific service"
    echo "  restart <service>     Restart specific service"
    echo "  build <service>       Build specific service"
    echo "  rebuild <service>     Rebuild specific service (no cache)"
    echo ""
    echo -e "${CYAN}LOG COMMANDS:${NC}"
    echo "  logs                  Show logs for all services"
    echo "  logs <service>        Show logs for specific service"
    echo "  logs-live             Follow logs for all services"
    echo "  logs-live <service>   Follow logs for specific service"
    echo "  logs-save             Save logs to files"
    echo "  logs-clear            Clear saved log files"
    echo ""
    echo -e "${CYAN}STATUS & MONITORING:${NC}"
    echo "  status                Show status of all services"
    echo "  ps                    Show running containers"
    echo "  stats                 Show container resource usage"
    echo "  health                Check health of services"
    echo ""
    echo -e "${CYAN}CLEANUP COMMANDS:${NC}"
    echo "  clean                 Remove stopped containers and unused images"
    echo "  clean-all             Remove all project containers, images, and volumes"
    echo "  prune                 Remove all unused Docker resources"
    echo ""
    echo -e "${CYAN}DATABASE COMMANDS:${NC}"
    echo "  db-connect            Connect to PostgreSQL database"
    echo "  db-backup             Backup database"
    echo "  db-restore <file>     Restore database from backup"
    echo ""
    echo -e "${CYAN}DEVELOPMENT COMMANDS:${NC}"
    echo "  dev                   Start in development mode"
    echo "  prod                  Start in production mode"
    echo "  shell <service>       Open shell in service container"
    echo ""
    echo -e "${CYAN}Available services:${NC} ${SERVICES[*]}"
}

validate_service() {
    local service=$1
    if [[ " ${SERVICES[@]} " =~ " ${service} " ]]; then
        return 0
    else
        print_error "Invalid service: $service"
        echo -e "${YELLOW}Available services:${NC} ${SERVICES[*]}"
        exit 1
    fi
}

# Service management functions
start_services() {
    local service=$1
    local compose_cmd=$(get_compose_cmd)
    
    print_info "Starting services..."
    
    if [ -n "$service" ]; then
        validate_service $service
        print_info "Starting service: $service"
        $compose_cmd up -d $service
        print_success "Service $service started"
    else
        print_info "Starting all services"
        $compose_cmd up -d
        print_success "All services started"
    fi
}

stop_services() {
    local service=$1
    local compose_cmd=$(get_compose_cmd)
    
    print_info "Stopping services..."
    
    if [ -n "$service" ]; then
        validate_service $service
        print_info "Stopping service: $service"
        $compose_cmd stop $service
        print_success "Service $service stopped"
    else
        print_info "Stopping all services"
        $compose_cmd down
        print_success "All services stopped"
    fi
}

restart_services() {
    local service=$1
    local compose_cmd=$(get_compose_cmd)
    
    print_info "Restarting services..."
    
    if [ -n "$service" ]; then
        validate_service $service
        print_info "Restarting service: $service"
        $compose_cmd restart $service
        print_success "Service $service restarted"
    else
        print_info "Restarting all services"
        $compose_cmd restart
        print_success "All services restarted"
    fi
}

build_services() {
    local service=$1
    local no_cache=$2
    local compose_cmd=$(get_compose_cmd)
    
    local build_args=""
    if [ "$no_cache" = "true" ]; then
        build_args="--no-cache"
        print_info "Building services (no cache)..."
    else
        print_info "Building services..."
    fi
    
    if [ -n "$service" ] && [ "$service" != "true" ]; then
        validate_service $service
        print_info "Building service: $service"
        $compose_cmd build $build_args $service
        print_success "Service $service built"
    else
        print_info "Building all services"
        $compose_cmd build $build_args
        print_success "All services built"
    fi
}

# Log management functions
show_logs() {
    local service=$1
    local follow=$2
    local compose_cmd=$(get_compose_cmd)
    
    if [ "$follow" = "true" ]; then
        local follow_arg="-f"
        print_info "Following logs (Ctrl+C to stop)..."
    else
        local follow_arg=""
        print_info "Showing logs..."
    fi
    
    if [ -n "$service" ]; then
        validate_service $service
        $compose_cmd logs $follow_arg $service
    else
        $compose_cmd logs $follow_arg
    fi
}

save_logs() {
    local compose_cmd=$(get_compose_cmd)
    local timestamp=$(date +%Y%m%d_%H%M%S)
    
    print_info "Saving logs to $LOG_DIR directory..."
    
    for service in "${SERVICES[@]}"; do
        local log_file="$LOG_DIR/${service}_${timestamp}.log"
        print_info "Saving logs for $service to $log_file"
        $compose_cmd logs $service > $log_file 2>&1
    done
    
    print_success "Logs saved to $LOG_DIR directory"
}

clear_logs() {
    print_warning "This will delete all saved log files in $LOG_DIR directory"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        rm -rf $LOG_DIR/*.log 2>/dev/null || true
        print_success "Log files cleared"
    else
        print_info "Operation cancelled"
    fi
}

# Status and monitoring functions
show_status() {
    local compose_cmd=$(get_compose_cmd)
    
    print_info "Service Status:"
    $compose_cmd ps
    
    echo ""
    print_info "Container Resource Usage:"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"
}

show_health() {
    print_info "Health Check Status:"
    
    # Check if containers are running
    for service in "${SERVICES[@]}"; do
        local container_name="${PROJECT_NAME}_${service}"
        if docker ps --format "{{.Names}}" | grep -q "^${container_name}$\|^whatsupclone_"; then
            print_success "$service: Running"
        else
            print_error "$service: Not running"
        fi
    done
    
    echo ""
    print_info "Port Status:"
    echo "PostgreSQL (5432): $(netstat -an 2>/dev/null | grep :5432 | head -1 || echo 'Not accessible')"
    echo "Keycloak (8080): $(netstat -an 2>/dev/null | grep :8080 | head -1 || echo 'Not accessible')"
    echo "Backend (9090): $(netstat -an 2>/dev/null | grep :9090 | head -1 || echo 'Not accessible')"
    echo "Frontend (5173): $(netstat -an 2>/dev/null | grep :5173 | head -1 || echo 'Not accessible')"
}

# Cleanup functions
clean_docker() {
    print_info "Cleaning up stopped containers and unused images..."
    
    docker container prune -f
    docker image prune -f
    
    print_success "Cleanup completed"
}

clean_all() {
    local compose_cmd=$(get_compose_cmd)
    
    print_warning "This will remove ALL containers, images, and volumes for this project"
    print_warning "This action cannot be undone!"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Stopping and removing all services..."
        $compose_cmd down -v --rmi all --remove-orphans
        print_success "All project resources removed"
    else
        print_info "Operation cancelled"
    fi
}

prune_docker() {
    print_warning "This will remove ALL unused Docker resources (containers, networks, images, volumes)"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Pruning Docker resources..."
        docker system prune -af --volumes
        print_success "Docker resources pruned"
    else
        print_info "Operation cancelled"
    fi
}

# Database functions
db_connect() {
    print_info "Connecting to PostgreSQL database..."
    docker exec -it whatsupclone_db psql -U whatsup_admin -d whatsupclone_db
}

db_backup() {
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_file="$LOG_DIR/db_backup_$timestamp.sql"
    
    print_info "Creating database backup..."
    docker exec whatsupclone_db pg_dump -U whatsup_admin whatsupclone_db > $backup_file
    print_success "Database backup saved to $backup_file"
}

db_restore() {
    local backup_file=$1
    
    if [ -z "$backup_file" ]; then
        print_error "Please specify backup file"
        exit 1
    fi
    
    if [ ! -f "$backup_file" ]; then
        print_error "Backup file not found: $backup_file"
        exit 1
    fi
    
    print_warning "This will restore the database from $backup_file"
    print_warning "Current data will be lost!"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Restoring database from $backup_file..."
        docker exec -i whatsupclone_db psql -U whatsup_admin -d whatsupclone_db < $backup_file
        print_success "Database restored"
    else
        print_info "Operation cancelled"
    fi
}

# Development functions
start_dev() {
    local compose_cmd=$(get_compose_cmd)
    
    print_info "Starting in development mode..."
    export NODE_ENV=development
    $compose_cmd -f $COMPOSE_FILE up -d
    
    print_success "Development environment started"
    print_info "Frontend: http://localhost:5173"
    print_info "Backend API: http://localhost:9090"
    print_info "Keycloak Admin: http://localhost:8080 (admin/admin)"
}

start_prod() {
    local compose_cmd=$(get_compose_cmd)
    
    print_info "Starting in production mode..."
    export NODE_ENV=production
    $compose_cmd -f $COMPOSE_FILE up -d
    
    print_success "Production environment started"
}

open_shell() {
    local service=$1
    
    if [ -z "$service" ]; then
        print_error "Please specify service name"
        exit 1
    fi
    
    validate_service $service
    
    local container_name="whatsupclone_${service}"
    if [ "$service" = "whatsup-server" ]; then
        container_name="whatsupclone_server"
    elif [ "$service" = "whatsup-client" ]; then
        container_name="whatsupclone_client"
    elif [ "$service" = "postgres" ]; then
        container_name="whatsupclone_db"
    elif [ "$service" = "keycloak" ]; then
        container_name="whatsupclone_keycloak"
    fi
    
    print_info "Opening shell in $service container..."
    docker exec -it $container_name /bin/bash 2>/dev/null || docker exec -it $container_name /bin/sh
}

# Main script logic
main() {
    check_docker
    
    case "${1:-help}" in
        "help"|"-h"|"--help"|"")
            show_help
            ;;
        "up")
            start_services $2
            ;;
        "down")
            stop_services $2
            ;;
        "restart")
            restart_services $2
            ;;
        "build")
            build_services $2 false
            ;;
        "rebuild")
            build_services $2 true
            ;;
        "logs")
            if [ "$2" = "live" ] || [ "$2" = "-f" ]; then
                show_logs $3 true
            else
                show_logs $2 false
            fi
            ;;
        "logs-live")
            show_logs $2 true
            ;;
        "logs-save")
            save_logs
            ;;
        "logs-clear")
            clear_logs
            ;;
        "status")
            show_status
            ;;
        "ps")
            docker ps --filter "name=whatsupclone"
            ;;
        "stats")
            docker stats --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"
            ;;
        "health")
            show_health
            ;;
        "clean")
            clean_docker
            ;;
        "clean-all")
            clean_all
            ;;
        "prune")
            prune_docker
            ;;
        "db-connect")
            db_connect
            ;;
        "db-backup")
            db_backup
            ;;
        "db-restore")
            db_restore $2
            ;;
        "dev")
            start_dev
            ;;
        "prod")
            start_prod
            ;;
        "shell")
            open_shell $2
            ;;
        *)
            print_error "Unknown command: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# Execute main function with all arguments
main "$@"
