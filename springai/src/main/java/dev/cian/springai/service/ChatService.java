package dev.cian.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String getChatResponse(String message) {
        return chatClient.prompt()
                .user(message)
                .tools(new CalculatorTools())
                .call()
                .content();
    }

    public static class CalculatorTools {
        
        @Tool(description = "Performs basic arithmetic operations like add, subtract, multiply, divide")
        public String calculate(double a, double b, String operation) {
            double result;
            try {
                switch (operation.toLowerCase()) {
                    case "add":
                        result = a + b;
                        break;
                    case "subtract":
                        result = a - b;
                        break;
                    case "multiply":
                        result = a * b;
                        break;
                    case "divide":
                        if (b == 0) {
                            return "Error: Cannot divide by zero";
                        }
                        result = a / b;
                        break;
                    default:
                        return "Error: Unknown operation: " + operation + ". Supported operations: add, subtract, multiply, divide";
                }
                return "The result of " + a + " " + operation + " " + b + " is " + result;
            } catch (Exception e) {
                return "Error performing calculation: " + e.getMessage();
            }
        }

        @Tool(description = "Get the current date and time")
        public String getCurrentTime() {
            LocalDateTime now = LocalDateTime.now();
            String formattedTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String dayOfWeek = now.getDayOfWeek().toString();
            return "Current time: " + formattedTime + ", Day: " + dayOfWeek;
        }
    }
} 