# ═══════════════════════════════════════════════════════════════
#  AI Resume Screening Platform — Makefile
# ═══════════════════════════════════════════════════════════════

.PHONY: help build test run docker-up docker-down docker-logs clean

help: ## Show available commands
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# ─── Build ──────────────────────────────────────────────────
build: ## Build the Spring Boot JAR (skip tests)
	mvn clean package -DskipTests -B

build-full: ## Build with tests
	mvn clean verify -B

# ─── Test ───────────────────────────────────────────────────
test: ## Run all unit tests
	mvn test -B

test-coverage: ## Run tests and generate Jacoco report
	mvn verify -B
	@echo "Coverage report: target/site/jacoco/index.html"

test-py: ## Run Python NLP service tests
	cd ai-nlp-service && pip install -r requirements.txt && pytest tests/ -v

# ─── Run locally ────────────────────────────────────────────
infra-up: ## Start only infrastructure (Mongo, Redis, ES, Kafka)
	docker-compose up -d mongo redis elasticsearch kafka zookeeper

run: infra-up ## Start infra + Spring Boot app locally
	mvn spring-boot:run

run-nlp: ## Start Python NLP service locally
	cd ai-nlp-service && uvicorn app.main:app --reload --port 5000

# ─── Docker ─────────────────────────────────────────────────
docker-build: ## Build Docker images
	docker-compose build

docker-up: ## Start all services with Docker Compose
	docker-compose up -d

docker-up-build: ## Rebuild and start all services
	docker-compose up -d --build

docker-down: ## Stop all services
	docker-compose down

docker-down-v: ## Stop all services and remove volumes (WIPES DATA)
	docker-compose down -v

docker-logs: ## Tail logs for Spring Boot app
	docker-compose logs -f app

docker-logs-nlp: ## Tail logs for Python NLP service
	docker-compose logs -f ai-nlp

docker-ps: ## Show running containers
	docker-compose ps

# ─── Cleanup ────────────────────────────────────────────────
clean: ## Clean Maven build artifacts
	mvn clean
	rm -rf uploads/resumes/* logs/*

# ─── Utilities ──────────────────────────────────────────────
swagger: ## Open Swagger UI in browser
	open http://localhost:8080/swagger-ui.html

actuator: ## Check health endpoint
	curl -s http://localhost:8080/actuator/health | python3 -m json.tool

mongo-shell: ## Open MongoDB shell
	docker-compose exec mongo mongosh resume_screening_db

redis-cli: ## Open Redis CLI
	docker-compose exec redis redis-cli

kafka-topics: ## List Kafka topics
	docker-compose exec kafka kafka-topics --bootstrap-server kafka:29092 --list
