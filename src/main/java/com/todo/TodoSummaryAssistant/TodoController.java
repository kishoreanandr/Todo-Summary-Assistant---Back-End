package com.todo.TodoSummaryAssistant;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;

@RestController
@CrossOrigin
public class TodoController {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    @Autowired
    private TodoRepository repo;

    @GetMapping("/todos")
    public List<Todo> getAll() {
        return repo.findAll();
    }

    @PostMapping("/todos")
    public Todo add(@RequestBody Todo todo) {
        return repo.save(todo);
    }

    @PutMapping("/todos/{id}")
    public Todo update(@PathVariable Long id, @RequestBody Todo updatedTodo) {
        Todo todo = repo.findById(id).orElse(null);
        if (todo != null) {
            todo.setTitle(updatedTodo.getTitle());
            return repo.save(todo);
        }
        return null;
    }

    @DeleteMapping("/todos/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }

    @PostMapping("/summarize")
    public ResponseEntity<Map<String, Object>> summarize() throws IOException, InterruptedException {
        Map<String, Object> response = new LinkedHashMap<>();
        List<Todo> todos = repo.findAll();

        if (todos.isEmpty()) {
            response.put("status", "error");
            response.put("message", "No tasks to summarize");
            return ResponseEntity.badRequest().body(response);
        }

        String todoText = todos.stream()
                .map(Todo::getTitle)
                .collect(Collectors.joining("\n• "));

        try {
            // Prepare Groq API request
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "llama3-70b-8192");

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", "Create a concise bullet point summary of these tasks:\n• " + todoText);
            messages.put(message);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.5);
            requestBody.put("max_tokens", 150);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(groqApiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+groqApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> apiResponse = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject jsonResponse = new JSONObject(apiResponse.body());
            String summary = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim();

            // Send to Slack
            JSONObject slackPayload = new JSONObject();
            slackPayload.put("text", "Todo Summary:\n" + summary);
            slackPayload.put("mrkdwn", true);

            HttpRequest slackRequest = HttpRequest.newBuilder()
                    .uri(URI.create(slackWebhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(slackPayload.toString()))
                    .build();

            HttpResponse<String> slackResponse = HttpClient.newHttpClient()
                    .send(slackRequest, HttpResponse.BodyHandlers.ofString());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("summary", summary);
            data.put("tokensUsed", jsonResponse.getJSONObject("usage").getInt("total_tokens"));
            data.put("slackStatus", slackResponse.statusCode() == 200 ? "success" : "failed");

            response.put("status", "success");
            response.put("data", data);

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error generating summary: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}