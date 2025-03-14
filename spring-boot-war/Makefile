
.PHONY: clean
clean:
	./gradlew clean	

.PHONY: build
build:
	./gradlew build	

.PHONY: run
run: build
	docker compose up --build

.PHONY: shutdown
shutdown: 
	docker compose down

