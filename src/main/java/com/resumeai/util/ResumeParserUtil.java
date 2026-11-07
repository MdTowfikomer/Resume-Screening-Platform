package com.resumeai.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * Utility for PDF text extraction using Apache PDFBox.
 * Also contains rule-based fallback skill extraction
 * (used when the AI/NLP microservice is unavailable).
 */
@Component
@Slf4j
public class ResumeParserUtil {

    // ── Known Skill Keywords (expandable) ──────────────────
    private static final Set<String> TECHNICAL_SKILLS = new HashSet<>(Arrays.asList(
            // Languages
            "java", "python", "javascript", "typescript", "kotlin", "scala", "go", "golang",
            "rust", "c++", "c#", "ruby", "php", "swift", "r", "matlab",
            // Frameworks
            "spring boot", "spring", "django", "flask", "fastapi", "react", "angular", "vue",
            "node.js", "nodejs", "express", "nestjs", "hibernate", "jpa",
            // Databases
            "mysql", "postgresql", "mongodb", "elasticsearch", "redis", "cassandra",
            "oracle", "sql server", "dynamodb", "neo4j", "influxdb",
            // Cloud & DevOps
            "aws", "azure", "gcp", "docker", "kubernetes", "k8s", "terraform", "ansible",
            "jenkins", "github actions", "gitlab ci", "circleci", "helm",
            // Messaging
            "kafka", "rabbitmq", "activemq", "sqs", "pubsub",
            // AI/ML
            "machine learning", "deep learning", "tensorflow", "pytorch", "scikit-learn",
            "nlp", "computer vision", "bert", "gpt", "openai", "langchain",
            // Other
            "rest api", "graphql", "grpc", "microservices", "git", "linux", "agile", "scrum"
    ));

    private static final Set<String> SOFT_SKILLS = new HashSet<>(Arrays.asList(
            "leadership", "communication", "teamwork", "problem solving", "critical thinking",
            "adaptability", "time management", "creativity", "collaboration", "mentoring",
            "project management", "stakeholder management", "presentation"
    ));

    private static final Pattern EMAIL_PATTERN   = Pattern.compile(
            "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN   = Pattern.compile(
            "(\\+?\\d{1,3}[\\s\\-]?)?(\\(?\\d{3}\\)?[\\s\\-]?)\\d{3}[\\s\\-]?\\d{4}");
    private static final Pattern LINKEDIN_PATTERN = Pattern.compile(
            "linkedin\\.com/in/[\\w\\-]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern YEARS_EXP_PATTERN = Pattern.compile(
            "(\\d+)\\+?\\s*years?\\s*(?:of\\s*)?experience", Pattern.CASE_INSENSITIVE);

    // ── PDF Text Extraction ────────────────────────────────

    public String extractTextFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            if (document.isEncrypted()) {
                throw new IllegalArgumentException("Encrypted PDF files are not supported");
            }
            var stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    // ── Rule-based Skill Extraction ───────────────────────

    public List<String> extractTechnicalSkills(String text) {
        String lowerText = text.toLowerCase();
        return TECHNICAL_SKILLS.stream()
                .filter(skill -> containsSkill(lowerText, skill))
                .map(s -> formatSkill(s))
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> extractSoftSkills(String text) {
        String lowerText = text.toLowerCase();
        return SOFT_SKILLS.stream()
                .filter(skill -> lowerText.contains(skill))
                .map(s -> toTitleCase(s))
                .sorted()
                .collect(Collectors.toList());
    }

    public String extractEmail(String text) {
        var matcher = EMAIL_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    public String extractPhone(String text) {
        var matcher = PHONE_PATTERN.matcher(text);
        return matcher.find() ? matcher.group().trim() : null;
    }

    public String extractLinkedIn(String text) {
        var matcher = LINKEDIN_PATTERN.matcher(text);
        return matcher.find() ? "https://www." + matcher.group() : null;
    }

    public Double extractExperienceYears(String text) {
        var matcher = YEARS_EXP_PATTERN.matcher(text);
        double maxYears = 0;
        while (matcher.find()) {
            try {
                double years = Double.parseDouble(matcher.group(1));
                maxYears = Math.max(maxYears, years);
            } catch (NumberFormatException ignored) {}
        }
        return maxYears > 0 ? maxYears : null;
    }

    public String extractName(String text) {
        // Heuristic: first non-empty line that looks like a person's name
        return Arrays.stream(text.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty()
                        && line.length() < 60
                        && !line.contains("@")
                        && line.matches("[A-Za-z\\s.'-]+"))
                .findFirst()
                .orElse(null);
    }

    // ── Helpers ────────────────────────────────────────────

    private boolean containsSkill(String text, String skill) {
        // Word-boundary match to avoid partial matches (e.g. "r" matching "ruby")
        if (skill.length() <= 2) {
            return Pattern.compile("\\b" + Pattern.quote(skill) + "\\b")
                    .matcher(text).find();
        }
        return text.contains(skill);
    }

    private String formatSkill(String skill) {
        return Arrays.stream(skill.split("\\s+"))
                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
    }

    private String toTitleCase(String s) {
        return Arrays.stream(s.split("\\s+"))
                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
    }
}
