package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {
    @MockBean
    HelpRequestRepository helpRequestRepository;

    @MockBean
    UserRepository userRepository;

    // test for GET
    
    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/HelpRequests/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }


    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/HelpRequests/all"))
                            .andExpect(status().is(200)); // logged
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_HelpRequests() throws Exception {

            // arrange

            LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
            HelpRequest helpRequest1 = HelpRequest.builder()
                            .requesterEmail("student@ucsb.edu")
                            .teamId("teamId")
                            .tableOrBreakoutRoom("table")
                            .requestTime(LocalDateTime.parse("2021-10-01T00:00:00"))
                            .explanation("explanation")
                            .solved(false)
                            .build();

            LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");
            HelpRequest helpRequest2 = HelpRequest.builder()
                            .requesterEmail("student2@ucsb.edu")
                            .teamId("teamId2")
                            .tableOrBreakoutRoom("breakoutRoom")
                            .requestTime(LocalDateTime.parse("2021-10-01T00:00:00"))
                            .explanation("explanation2")
                            .solved(false)
                            .build();
                            
            ArrayList<HelpRequest> expectedHelpRequests = new ArrayList<>();
            expectedHelpRequests.addAll(Arrays.asList(helpRequest1, helpRequest2));

            when(helpRequestRepository.findAll()).thenReturn(expectedHelpRequests);
            // act
            MvcResult response = mockMvc.perform(get("/api/HelpRequests/all"))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(helpRequestRepository, times(1)).findAll();
            String expectedJson = mapper.writeValueAsString(expectedHelpRequests);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
            
    }

    // Tests for POST /api/HelpRequests/post...

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/HelpRequests/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/HelpRequests/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_HelpRequest() throws Exception {
            // arrange

            HelpRequest helpRequest1 = HelpRequest.builder()
                            .requesterEmail("student@ucsb.edu")
                            .teamId("teamId")
                            .tableOrBreakoutRoom("table")
                            .requestTime(LocalDateTime.parse("2021-10-01T00:00:00"))
                            .explanation("explanation")
                            .solved(false)
                            .build();
            when(helpRequestRepository.save(eq(helpRequest1))).thenReturn(helpRequest1);
            
            // act

            MvcResult response = mockMvc.perform(
                            post("/api/HelpRequests/post?requesterEmail=student@ucsb.edu&teamId=teamId&tableOrBreakoutRoom=table&requestTime=2021-10-01T00:00:00&explanation=explanation&solved=false")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();
            // assert

            verify(helpRequestRepository, times(1)).save(helpRequest1);
            String expectedJson = mapper.writeValueAsString(helpRequest1);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
            }



}