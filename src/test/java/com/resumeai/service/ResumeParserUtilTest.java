//package com.resumeai.service;
//
//import com.resumeai.util.ResumeParserUtil;
//import org.junit.jupiter.api.*;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.*;
//
//@DisplayName("ResumeParserUtil Unit Tests")
//class ResumeParserUtilTest {
//
//    private ResumeParserUtil parserUtil;
//
//    @BeforeEach
//    void setUp() {
//        parserUtil = new ResumeParserUtil();
//    }
//
//    private static final String SAMPLE_RESUME = """
//            John Doe
//            john.doe@example.com | +1-555-123-4567
//            linkedin.com/in/johndoe | San Francisco, CA
//
//            PROFESSIONAL SUMMARY
//            Senior Software Engineer with 8 years of experience in building scalable
//            distributed systems using Java, Spring Boot, Kubernetes, and AWS.
//
//            TECHNICAL SKILLS
//            Languages: Java, Python, TypeScript
//            Frameworks: Spring Boot, React, Node.js
//            Databases: MongoDB, PostgreSQL, Redis, Elasticsearch
//            Cloud & DevOps: AWS, Docker, Kubernetes, Jenkins, Kafka
//
//            WORK EXPERIENCE
//            Senior Software Engineer at Acme Technologies Inc (2019 – Present)
//            - Led migration of monolith to microservices using Spring Boot and Kafka
//            - Designed Redis caching layer reducing API latency by 60%
//
//            Software Engineer at Beta Solutions Ltd (2016 – 2019)
//            - Built REST APIs in Java and deployed to AWS
//
//            EDUCATION
//            Bachelor of Engineering in Computer Science
//            University of California, Berkeley (2012 – 2016)
//
//            CERTIFICATIONS
//            AWS Certified Solutions Architect
//            Certified Kubernetes Administrator (CKA)
//            """;
//
//    @Test
//    @DisplayName("extractTechnicalSkills — should find Java, Spring Boot, Kubernetes, AWS etc.")
//    void extractTechnicalSkills_findsExpectedSkills() {
//        List<String> skills = parserUtil.extractTechnicalSkills(SAMPLE_RESUME);
//
//        assertThat(skills).isNotEmpty();
//        assertThat(skills).anyMatch(s -> s.equalsIgnoreCase("Java"));
//        assertThat(skills).anyMatch(s -> s.equalsIgnoreCase("Spring Boot"));
//        assertThat(skills).anyMatch(s -> s.equalsIgnoreCase("Kubernetes"));
//        assertThat(skills).anyMatch(s -> s.equalsIgnoreCase("AWS"));
//        assertThat(skills).anyMatch(s -> s.equalsIgnoreCase("Docker"));
//        assertThat(skills).anyMatch(s -> s.equalsIgnoreCase("Redis"));
//        assertThat(skills).anyMatch(s -> s.equalsIgnoreCase("Kafka"));
//        assertThat(skills).anyMatch(s -> s.equalsIgnoreCase("Mongodb"));
//    }
//
//    @Test
//    @DisplayName("extractSoftSkills — should identify leadership-related terms")
//    void extractSoftSkills_findsTerms() {
//        String text = "Demonstrated strong leadership and excellent communication skills. "
//                + "Expert in project management and team collaboration.";
//        List<String> softSkills = parserUtil.extractSoftSkills(text);
//
//        assertThat(softSkills).isNotEmpty();
//        assertThat(softSkills).anyMatch(s -> s.equalsIgnoreCase("Leadership"));
//        assertThat(softSkills).anyMatch(s -> s.equalsIgnoreCase("Communication"));
//    }
//
//    @Test
//    @DisplayName("extractEmail — should parse valid email from resume text")
//    void extractEmail_returnsCorrectEmail() {
//        String email = parserUtil.extractEmail(SAMPLE_RESUME);
//        assertThat(email).isEqualTo("john.doe@example.com");
//    }
//
//    @Test
//    @DisplayName("extractEmail — should return null when no email present")
//    void extractEmail_noEmail_returnsNull() {
//        assertThat(parserUtil.extractEmail("No contact info here")).isNull();
//    }
//
//    @Test
//    @DisplayName("extractPhone — should find phone number in resume")
//    void extractPhone_returnsPhone() {
//        String phone = parserUtil.extractPhone(SAMPLE_RESUME);
//        assertThat(phone).isNotBlank();
//        assertThat(phone).contains("555");
//    }
//
//    @Test
//    @DisplayName("extractLinkedIn — should parse LinkedIn URL")
//    void extractLinkedIn_returnsUrl() {
//        String linkedin = parserUtil.extractLinkedIn(SAMPLE_RESUME);
//        assertThat(linkedin).isNotNull();
//        assertThat(linkedin).contains("linkedin.com/in/johndoe");
//    }
//
//    @Test
//    @DisplayName("extractExperienceYears — should return 8 from '8 years of experience'")
//    void extractExperienceYears_returns8() {
//        Double years = parserUtil.extractExperienceYears(SAMPLE_RESUME);
//        assertThat(years).isEqualTo(8.0);
//    }
//
//    @Test
//    @DisplayName("extractExperienceYears — should return null when no mention found")
//    void extractExperienceYears_noMention_returnsNull() {
//        Double years = parserUtil.extractExperienceYears("No experience section here");
//        assertThat(years).isNull();
//    }
//
//    @Test
//    @DisplayName("extractName — should return first non-empty name-like line")
//    void extractName_returnsFirstLine() {
//        String name = parserUtil.extractName(SAMPLE_RESUME);
//        assertThat(name).isNotBlank();
//        assertThat(name).contains("John");
//    }
//
//    @Test
//    @DisplayName("extractTechnicalSkills — no skills found returns empty list, not null")
//    void extractTechnicalSkills_emptyInput_returnsEmptyList() {
//        List<String> skills = parserUtil.extractTechnicalSkills("This resume has no recognizable skills listed.");
//        assertThat(skills).isNotNull();
//    }
//
//    @Test
//    @DisplayName("extractCertifications via extractTechnicalSkills — AWS cert keywords present")
//    void certificationKeywordsPresent() {
//        // The resume has "AWS Certified Solutions Architect" — at minimum AWS should be detected
//        List<String> skills = parserUtil.extractTechnicalSkills(SAMPLE_RESUME);
//        assertThat(skills).anyMatch(s -> s.equalsIgnoreCase("AWS"));
//    }
//}
