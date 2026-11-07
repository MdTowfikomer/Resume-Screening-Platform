//package com.resumeai.service;
//
//import com.resumeai.exception.DuplicateResourceException;
//import com.resumeai.exception.ResourceNotFoundException;
//import com.resumeai.kafka.producer.ResumeEventProducer;
//import com.resumeai.model.Resume;
//import com.resumeai.repository.elasticsearch.ResumeSearchRepository;
//import com.resumeai.repository.mongo.ResumeRepository;
//import com.resumeai.service.impl.ResumeServiceImpl;
//import com.resumeai.util.FileStorageUtil;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.*;
//import org.springframework.mock.web.MockMultipartFile;
//
//import java.util.*;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("ResumeService Unit Tests")
//class ResumeServiceTest {
//
//    @Mock private ResumeRepository      resumeRepository;
//    @Mock private ResumeSearchRepository resumeSearchRepository;
//    @Mock private FileStorageUtil        fileStorageUtil;
//    @Mock private ResumeEventProducer    eventProducer;
//
//    @InjectMocks
//    private ResumeServiceImpl resumeService;
//
//    private static final String RESUME_ID     = "resume-123";
//    private static final String CANDIDATE_ID  = "candidate-456";
//    private static final String CANDIDATE_EMAIL = "john.doe@example.com";
//
//    // ── Upload Tests ───────────────────────────────────────
//
//    @Test
//    @DisplayName("uploadResume — should save resume and publish Kafka event")
//    void uploadResume_success() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "file", "resume.pdf", "application/pdf", "PDF content".getBytes());
//
//        when(resumeRepository.existsByCandidateEmailAndIsActiveTrue(CANDIDATE_EMAIL))
//                .thenReturn(false);
//        when(fileStorageUtil.storeFile(file)).thenReturn("stored-uuid.pdf");
//        when(resumeRepository.save(any(Resume.class))).thenAnswer(inv -> {
//            Resume r = inv.getArgument(0);
//            r.setId(RESUME_ID);
//            return r;
//        });
//
//        Resume result = resumeService.uploadResume(file, CANDIDATE_ID, CANDIDATE_EMAIL, "John Doe");
//
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(RESUME_ID);
//        assertThat(result.getCandidateEmail()).isEqualTo(CANDIDATE_EMAIL);
//        assertThat(result.getProcessingStatus()).isEqualTo(Resume.ProcessingStatus.PENDING);
//
//        verify(resumeRepository).save(any(Resume.class));
//        verify(eventProducer).publishResumeUploaded(any());
//    }
//
//    @Test
//    @DisplayName("uploadResume — should throw DuplicateResourceException if email already exists")
//    void uploadResume_duplicate_throwsException() {
//        MockMultipartFile file = new MockMultipartFile(
//                "file", "resume.pdf", "application/pdf", "PDF content".getBytes());
//
//        when(resumeRepository.existsByCandidateEmailAndIsActiveTrue(CANDIDATE_EMAIL))
//                .thenReturn(true);
//
//        assertThatThrownBy(() ->
//                resumeService.uploadResume(file, CANDIDATE_ID, CANDIDATE_EMAIL, "John Doe"))
//                .isInstanceOf(DuplicateResourceException.class)
//                .hasMessageContaining(CANDIDATE_EMAIL);
//
//        verify(resumeRepository, never()).save(any());
//        verify(eventProducer, never()).publishResumeUploaded(any());
//    }
//
//    // ── Get Tests ──────────────────────────────────────────
//
//    @Test
//    @DisplayName("getResumeById — should return resume when found")
//    void getResumeById_found() {
//        Resume resume = Resume.builder().id(RESUME_ID).candidateEmail(CANDIDATE_EMAIL).build();
//        when(resumeRepository.findById(RESUME_ID)).thenReturn(Optional.of(resume));
//
//        Resume result = resumeService.getResumeById(RESUME_ID);
//
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(RESUME_ID);
//    }
//
//    @Test
//    @DisplayName("getResumeById — should throw ResourceNotFoundException when not found")
//    void getResumeById_notFound_throwsException() {
//        when(resumeRepository.findById(RESUME_ID)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> resumeService.getResumeById(RESUME_ID))
//                .isInstanceOf(ResourceNotFoundException.class);
//    }
//
//    // ── Search Tests ───────────────────────────────────────
//
//    @Test
//    @DisplayName("searchBySkills — should return matching resumes")
//    void searchBySkills_returnsResults() {
//        List<String> skills = List.of("Java", "Spring Boot");
//        List<Resume> expected = List.of(
//                Resume.builder().id("r1").technicalSkills(skills).build(),
//                Resume.builder().id("r2").technicalSkills(skills).build());
//
//        when(resumeRepository.findByTechnicalSkillsIn(skills)).thenReturn(expected);
//
//        List<Resume> result = resumeService.searchBySkills(skills);
//
//        assertThat(result).hasSize(2);
//        assertThat(result).extracting(Resume::getId).containsExactly("r1", "r2");
//    }
//
//    // ── Delete Tests ───────────────────────────────────────
//
//    @Test
//    @DisplayName("deleteResume — should soft-delete and remove from Elasticsearch")
//    void deleteResume_softDeletes() {
//        Resume resume = Resume.builder().id(RESUME_ID).isActive(true).build();
//        when(resumeRepository.findById(RESUME_ID)).thenReturn(Optional.of(resume));
//        when(resumeRepository.save(any())).thenReturn(resume);
//
//        resumeService.deleteResume(RESUME_ID);
//
//        assertThat(resume.getIsActive()).isFalse();
//        verify(resumeRepository).save(resume);
//        verify(resumeSearchRepository).deleteById(RESUME_ID);
//    }
//
//    // ── Reprocess Tests ────────────────────────────────────
//
//    @Test
//    @DisplayName("reprocessResume — should reset status to PENDING and re-publish event")
//    void reprocessResume_resetsToPending() {
//        Resume resume = Resume.builder()
//                .id(RESUME_ID)
//                .processingStatus(Resume.ProcessingStatus.FAILED)
//                .processingError("Some error")
//                .storedFilename("uuid.pdf")
//                .contentType("application/pdf")
//                .build();
//
//        when(resumeRepository.findById(RESUME_ID)).thenReturn(Optional.of(resume));
//        when(resumeRepository.save(any())).thenReturn(resume);
//
//        Resume result = resumeService.reprocessResume(RESUME_ID);
//
//        assertThat(result.getProcessingStatus()).isEqualTo(Resume.ProcessingStatus.PENDING);
//        assertThat(result.getProcessingError()).isNull();
//        verify(eventProducer).publishResumeUploaded(any());
//    }
//}
