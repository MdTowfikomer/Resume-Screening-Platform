package com.resumeai.constants;

/**
 * Central constants registry — single source of truth for all
 * magic strings, API versions, topic names, and cache keys.
 */
public final class AppConstants {

    private AppConstants() { /* prevent instantiation */ }

    // ── API Versioning ─────────────────────────────────────
    public static final String API_V1        = "/api/v1";
    public static final String API_VERSION   = "v1";

    // ── Controller base paths ──────────────────────────────
    public static final String RESUME_BASE       = API_V1 + "/resumes";
    public static final String JOB_BASE          = API_V1 + "/jobs";
    public static final String MATCH_BASE        = API_V1 + "/matches";
    public static final String RECRUITER_BASE    = API_V1 + "/recruiter";
    public static final String AUTH_BASE         = API_V1 + "/auth";
    public static final String CANDIDATE_BASE    = API_V1 + "/candidates";

    // ── Kafka Topics ───────────────────────────────────────
    public static final String TOPIC_RESUME_UPLOADED  = "resume-uploaded-events";
    public static final String TOPIC_RESUME_PROCESSED = "resume-processed-events";
    public static final String TOPIC_JOB_MATCHED      = "job-matched-events";
    public static final String TOPIC_NOTIFICATION     = "notification-events";

    public static final String CONSUMER_GROUP_MAIN    = "resume-screening-group";

    // ── Redis Cache Keys & TTLs ────────────────────────────
    public static final String CACHE_RESUME          = "resumes";
    public static final String CACHE_JOB             = "jobs";
    public static final String CACHE_MATCH_SCORE     = "match-scores";
    public static final String CACHE_RECRUITER_STATS = "recruiter-stats";
    public static final String CACHE_USER            = "users";

    public static final long CACHE_TTL_MINUTES       = 60L;
    public static final long CACHE_STATS_TTL_MINUTES = 10L;

    // ── Elasticsearch Indices ──────────────────────────────
    public static final String ES_INDEX_RESUME = "resumes";
    public static final String ES_INDEX_JOB    = "jobs";

    // ── File Upload ────────────────────────────────────────
    public static final long   MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L; // 10 MB
    public static final String ALLOWED_CONTENT_TYPE_PDF  = "application/pdf";
    public static final String ALLOWED_CONTENT_TYPE_DOCX =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    // ── Matching ───────────────────────────────────────────
    public static final double DEFAULT_SCORE_THRESHOLD   = 60.0;
    public static final int    MAX_MATCH_RESULTS         = 50;

    // ── Security / JWT ─────────────────────────────────────
    public static final String AUTH_HEADER       = "Authorization";
    public static final String BEARER_PREFIX     = "Bearer ";
    public static final String ROLE_ADMIN        = "ROLE_ADMIN";
    public static final String ROLE_RECRUITER    = "ROLE_RECRUITER";
    public static final String ROLE_CANDIDATE    = "ROLE_CANDIDATE";

    // ── Pagination Defaults ────────────────────────────────
    public static final int DEFAULT_PAGE_NUMBER  = 0;
    public static final int DEFAULT_PAGE_SIZE    = 20;
    public static final int MAX_PAGE_SIZE        = 100;
    public static final String DEFAULT_SORT_BY   = "createdAt";
    public static final String SORT_ASC          = "asc";
    public static final String SORT_DESC         = "desc";

    // ── Audit ──────────────────────────────────────────────
    public static final String CREATED_AT  = "createdAt";
    public static final String UPDATED_AT  = "updatedAt";
    public static final String CREATED_BY  = "createdBy";
    public static final String UPDATED_BY  = "updatedBy";
}
