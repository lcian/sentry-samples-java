.PHONY: request 

# Send a test request to the chat API
request:
	@echo "Sending request to chat API..."
	@curl -X POST http://localhost:8080/api/chat \
		-H "Content-Type: application/json" \
		-d '{"message": "Explain what Spring AI is in simple terms"}' \
		-w "\n"
