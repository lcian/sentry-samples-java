
.PHONY: build
build:
	./gradlew fatJar	

.PHONY: run
run: build
	docker compose up -d
	AWS_PAGER="" aws dynamodb create-table --table-name orders_table --attribute-definitions AttributeName=orderId,AttributeType=S --key-schema AttributeName=orderId,KeyType=HASH --billing-mode PAY_PER_REQUEST --endpoint-url http://localhost:8000
	java -javaagent:sentry-opentelemetry-agent-8.2.0.jar -jar build/libs/dynamodb-1.0-SNAPSHOT-standalone.jar
	docker compose down
