package sweng.penelope.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import sweng.penelope.repositories.DataManagerRepository;
import sweng.penelope.services.StorageService;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class KeyPairControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageService storageService;

    @MockBean
    private DataManagerRepository dataManagerRepository;

    @Value("${penelope.api-key}")
    private String keyHeader;

    @Test
    @WithMockUser
    public void canGetMethodName() throws Exception {
        mockMvc.perform(get("/key").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().exists(keyHeader))
                // Expect Base64
                .andExpect(header().string(keyHeader, matchesPattern("^[A-Za-z0-9+/]+[=]{0,2}$")));
    }
}
