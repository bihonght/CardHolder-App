package com.cardpayment;

import com.cardpayment.db.Database;
import com.cardpayment.model.CardRecord;
import com.cardpayment.repo.CardRepository;
import com.cardpayment.security.Crypto;
import com.cardpayment.util.Masking;

import static spark.Spark.*;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class App {
    public static void main(String[] args) {
        port(getPort());
        staticFiles.location("/public"); // serves index.html

        // Init DB + schema
        Database.init();
        try (Connection c = Database.get()) {
            Database.ensureSchema(c);
        } catch (Exception e) {
            System.err.println("DB init error: " + e.getMessage());
            System.exit(1);
        }

        CardRepository repo = new CardRepository();

        // Health
        get("/health", (req, res) -> "OK");

        // Add card (form POST)
        post("/cards", (req, res) -> {
            String name = trimOrNull(req.queryParams("name"));
            String pan = trimOrNull(req.queryParams("pan"));

            if (name == null || name.isEmpty() || pan == null || pan.isEmpty()) {
                res.status(400);
                return "Name and PAN are required.";
            }

            // Validate PAN basic (digits 12-19 typical)
            String panDigits = pan.replaceAll("\\s+", "");
            if (!panDigits.matches("\\d{12,19}")) {
                res.status(400);
                return "PAN must be 12-19 digits.";
            }

            String last4 = panDigits.substring(panDigits.length() - 4);

            // Encrypt
            Crypto.Encrypted enc = Crypto.encrypt(panDigits.getBytes(StandardCharsets.UTF_8));

            // Persist (never log plaintext!)
            try {
                repo.insertCard(name, enc.ciphertext(), enc.iv(), panDigits);
            } catch (Exception e) {
                res.status(500);
                return "Failed to save card.";
            }

            // Redirect back to home with a success message
            res.redirect("/?saved=1&last4=" + last4);
            return null;
        });

        // Search by last4 (HTML)
        get("/cards", (req, res) -> {
            String last4 = trimOrNull(req.queryParams("last4"));
            if (last4 == null || !last4.matches("\\d{4}")) {
                res.status(400);
                return "Query param last4=#### required.";
            }
            List<CardRecord> found = repo.findByLast4(last4);
            res.type("text/html");
            return renderResultsHtml(last4, found);
        });

        // JSON search (optional)
        get("/api/cards", (req, res) -> {
            String last4 = trimOrNull(req.queryParams("last4"));
            if (last4 == null || !last4.matches("\\d{4}")) {
                res.status(400);
                return "{\"error\":\"last4 required\"}";
            }
            List<CardRecord> found = repo.findByLast4(last4);
            res.type("application/json");
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i=0; i<found.size(); i++) {
                CardRecord r = found.get(i);
                sb.append("{")
                        .append("\"name\":\"").append(escapeJson(r.name())).append("\",")
                        .append("\"maskedPan\":\"").append(Masking.maskLast4(r.last4())).append("\",")
                        .append("\"createdAt\":\"").append(r.createdAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)).append("\"")
                        .append("}");
                if (i < found.size() - 1) sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        });
    }

    private static String renderResultsHtml(String last4, List<CardRecord> found) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html><html><head><meta charset='utf-8'><title>Results</title>")
                .append("<link rel='stylesheet' href='/styles.css'></head><body>")
                .append("<div class='container'>")
                .append("<h1>Search results for **** ").append(last4).append("</h1>")
                .append("<a href='/'>&larr; Back</a>")
                .append("<table><thead><tr><th>Name</th><th>Masked PAN</th><th>Created</th></tr></thead><tbody>");
        for (CardRecord r : found) {
            sb.append("<tr>")
                    .append("<td>").append(escapeHtml(r.name())).append("</td>")
                    .append("<td>").append(Masking.maskLast4(r.last4())).append("</td>")
                    .append("<td>").append(r.createdAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)).append("</td>")
                    .append("</tr>");
        }
        if (found.isEmpty()) {
            sb.append("<tr><td colspan='3' style='text-align:center;color:#777'>No matches.</td></tr>");
        }
        sb.append("</tbody></table></div></body></html>");
        return sb.toString();
    }

    private static int getPort() {
        String p = System.getenv("PORT");
        return (p != null) ? Integer.parseInt(p) : 8080;
    }

    private static String trimOrNull(String s) {
        return (s == null) ? null : s.trim();
    }

    // Very small HTML/JSON escaping helpers to avoid bringing a templating engine
    private static String escapeHtml(String s) {
        return Objects.toString(s, "")
                .replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }
    private static String escapeJson(String s) {
        return Objects.toString(s, "").replace("\\","\\\\").replace("\"","\\\"");
    }
}
