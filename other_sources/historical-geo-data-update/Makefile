.PHONY: build_start_common start_local down_common

# Docker build and start the local docker stack
build_start_common:
	docker compose -f docker-compose.yml build --no-cache && docker compose -f docker-compose.yml up --force-recreate  $(c)

start_local:
	docker compose -f docker-compose.yml up --force-recreate  $(c)

# Stop the local docker stack
down_common:
	docker compose -f docker-compose.yml down $(c)
