package com.collaborativeediting.documentediting;

import org.springframework.stereotype.Service;

@Service
public class DocumentExportService {

    public enum ExportFormat {
        TXT, HTML, JSON
    }

    public byte[] exportDocument(Document document, ExportFormat format) {
        switch (format) {
            case TXT:
                return exportToTxt(document);
            case HTML:
                return exportToHtml(document);
            case JSON:
                return exportToJson(document);
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }
    }

    private byte[] exportToTxt(Document document) {
        StringBuilder content = new StringBuilder();
        content.append("Title: ").append(document.getTitle()).append("\n\n");

        if (document.getContent() != null && !document.getContent().trim().isEmpty()) {
            content.append("Content:\n").append(document.getContent());
        } else {
            content.append("Content:\nNo content available.");
        }

        content.append("\n\n--- Document Details ---\n");
        content.append("Owner: ").append(document.getOwner()).append("\n");
        content.append("Created: ").append(document.getCreatedAt()).append("\n");
        content.append("Last Updated: ").append(document.getUpdatedAt());

        return content.toString().getBytes();
    }

    private byte[] exportToHtml(Document document) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<title>").append(escapeHtml(document.getTitle())).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 40px; }\n");
        html.append("h1 { color: #333; }\n");
        html.append(".content { margin-top: 20px; line-height: 1.6; }\n");
        html.append(".metadata { margin-top: 30px; padding: 10px; background-color: #f5f5f5; }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        html.append("<h1>").append(escapeHtml(document.getTitle())).append("</h1>\n");
        html.append("<div class=\"content\">");

        if (document.getContent() != null && !document.getContent().trim().isEmpty()) {
            // Convert line breaks to HTML paragraphs
            String[] paragraphs = document.getContent().split("\n\n");
            for (String paragraph : paragraphs) {
                if (!paragraph.trim().isEmpty()) {
                    html.append("<p>").append(escapeHtml(paragraph.trim())).append("</p>\n");
                }
            }
        } else {
            html.append("<p><em>No content available.</em></p>\n");
        }

        html.append("</div>\n");
        html.append("<div class=\"metadata\">\n");
        html.append("<h3>Document Information</h3>\n");
        html.append("<p><strong>Owner:</strong> ").append(escapeHtml(document.getOwner())).append("</p>\n");
        html.append("<p><strong>Created:</strong> ").append(document.getCreatedAt()).append("</p>\n");
        html.append("<p><strong>Last Updated:</strong> ").append(document.getUpdatedAt()).append("</p>\n");
        html.append("</div>\n");
        html.append("</body>\n</html>");

        return html.toString().getBytes();
    }

    private byte[] exportToJson(Document document) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"id\": ").append(document.getId()).append(",\n");
        json.append("  \"title\": \"").append(escapeJson(document.getTitle())).append("\",\n");
        json.append("  \"content\": \"").append(escapeJson(document.getContent() != null ? document.getContent() : "")).append("\",\n");
        json.append("  \"owner\": \"").append(escapeJson(document.getOwner())).append("\",\n");
        json.append("  \"createdAt\": \"").append(document.getCreatedAt()).append("\",\n");
        json.append("  \"updatedAt\": \"").append(document.getUpdatedAt()).append("\"\n");
        json.append("}");

        return json.toString().getBytes();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    public String getContentType(ExportFormat format) {
        switch (format) {
            case TXT:
                return "text/plain";
            case HTML:
                return "text/html";
            case JSON:
                return "application/json";
            default:
                return "application/octet-stream";
        }
    }

    public String getFileExtension(ExportFormat format) {
        switch (format) {
            case TXT:
                return ".txt";
            case HTML:
                return ".html";
            case JSON:
                return ".json";
            default:
                return "";
        }
    }
}
