//package com.resumeai.controller;
//
//import com.resumeai.model.Resume;
//import com.resumeai.service.ResumeService;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(ResumeController.class)
//@DisplayName("ResumeController Integration Tests")
//class ResumeControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ResumeService resumeService;
//
//    @Test
//    @WithMockUser(roles = "CANDIDATE")
//    @DisplayName("POST /upload — should return 201 CREATED on valid PDF upload")
//    void uploadResume_returns201() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "file", "resume.pdf", "application/pdf",
//                "Fake PDF content".getBytes());
//
//        Resume mockResume = Resume.builder()
//                .id("resume-abc")
//                .candidateEmail("test@example.com")
//                .originalFilename("resume.pdf")
//                .processingStatus(Resume.ProcessingStatus.PENDING)
//                .build();
//
//        when(resumeService.uploadResume(any(), any(), any(), any())).thenReturn(mockResume);
//
//        mockMvc.perform(multipart("/api/v1/resumes/upload")
//                        .file(file)
//                        .param("candidateId", "cand-1")
//                        .param("candidateEmail", "test@example.com")
//                        .param("candidateName", "Test User")
//                        .with(csrf()))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.id").value("resume-abc"))
//                .andExpect(jsonPath("$.data.processingStatus").value("PENDING"));
//    }
//
//    @Test
//    @WithMockUser(roles = "RECRUITER")
//    @DisplayName("GET /api/v1/resumes — should return paginated resume list")
//    void getAllResumes_returnsList() throws Exception {
//        var page = new org.springframework.data.domain.PageImpl<>(List.of(
//                Resume.builder().id("r1").candidateEmail("a@b.com").build()));
//
//        when(resumeService.getAllResumes(any())).thenReturn(page);
//
//        mockMvc.perform(get("/api/v1/resumes")
//                        .param("page", "0").param("size", "10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true));
//    }
//
//    @Test
//    @WithMockUser
//    @DisplayName("GET /{id} — should return 200 with resume body")
//    void getResumeById_returns200() throws Exception {
//        Resume mockResume = Resume.builder().id("resume-abc")
//                .candidateEmail("test@example.com")
//                .processingStatus(Resume.ProcessingStatus.COMPLETED)
//                .build();
//
//        when(resumeService.getResumeById("resume-abc")).thenReturn(mockResume);
//
//        mockMvc.perform(get("/api/v1/resumes/resume-abc"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.id").value("resume-abc"))
//                .andExpect(jsonPath("$.data.processingStatus").value("COMPLETED"));
//    }
//
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    @DisplayName("POST /{id}/reprocess — should return 200 with updated resume")
//    void reprocessResume_returns200() throws Exception {
//        Resume mockResume = Resume.builder().id("resume-abc")
//                .processingStatus(Resume.ProcessingStatus.PENDING).build();
//
//        when(resumeService.reprocessResume("resume-abc")).thenReturn(mockResume);
//
//        mockMvc.perform(post("/api/v1/resumes/resume-abc/reprocess").with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.processingStatus").value("PENDING"));
//    }
//
//    @Test
//    @WithMockUser
//    @DisplayName("DELETE /{id} — should return 200 on soft-delete")
//    void deleteResume_returns200() throws Exception {
//        doNothing().when(resumeService).deleteResume("resume-abc");
//
//        mockMvc.perform(delete("/api/v1/resumes/resume-abc").with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true));
//    }
//}
