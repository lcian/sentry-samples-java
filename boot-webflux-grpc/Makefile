
.PHONY: clean
clean:
	./gradlew clean	

.PHONY: build
build:
	./gradlew build	

.PHONY: run
run: build
	OTEL_TRACES_EXPORTER=none OTEL_LOGS_EXPORTER=none OTEL_METRICS_EXPORTER=none ./gradlew bootRun

.PHONY: request
request:
	curl -X POST -H "Content-Type: application/json" -d '{"deviceId": "test-device", "fileName": "test.txt"}' http://localhost:8080/device/get_upload_url

.PHONY: requests
requests:
	bash test_requests.sh

