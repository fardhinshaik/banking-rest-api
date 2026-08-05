package com.bank.banking_api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BankingApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String suffix;

    @BeforeEach
    void setUp() {
        suffix = UUID.randomUUID().toString();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void createsUserAccountDepositWithdrawTransferAndHistory() throws Exception {
        long userOneId = createUser("Alice Tester", "alice-" + suffix + "@test.com");
        long userTwoId = createUser("Bob Tester", "bob-" + suffix + "@test.com");

        String accountOne = createAccount(userOneId, "1000");
        String accountTwo = createAccount(userTwoId, "500");

        mockMvc.perform(post("/api/v1/accounts/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountNumber":"%s","amount":250}
                                """.formatted(accountOne)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1250.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(post("/api/v1/accounts/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountNumber":"%s","amount":100}
                                """.formatted(accountOne)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1150.0));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountNumber":"%s","toAccountNumber":"%s","amount":300}
                                """.formatted(accountOne, accountTwo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mockMvc.perform(get("/api/v1/accounts/{accountNumber}", accountOne))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(850.0));

        mockMvc.perform(get("/api/v1/accounts/{accountNumber}", accountTwo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(800.0));

        mockMvc.perform(get("/api/v1/transactions/{accountNumber}", accountOne))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].type").value("TRANSFER"))
                .andExpect(jsonPath("$[1].type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$[2].type").value("DEPOSIT"));
    }

    @Test
    void rejectsInvalidUserAndDuplicateEmailWithBadRequest() throws Exception {
        String email = "duplicate-" + suffix + "@test.com";
        createUser("First User", email);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"","email":"not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Duplicate User","email":"%s"}
                                """.formatted(email)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request violates a database constraint"));
    }

    @Test
    void rejectsMissingResourcesAndInsufficientBalance() throws Exception {
        long userId = createUser("Funds Tester", "funds-" + suffix + "@test.com");
        String accountNumber = createAccount(userId, "100");

        mockMvc.perform(get("/api/v1/users/{id}", 999999))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/accounts/{accountNumber}", "NO_SUCH_ACCOUNT"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/accounts/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountNumber":"%s","amount":999999}
                                """.formatted(accountNumber)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient funds for withdrawal"));
    }

    @Test
    void inactiveAccountsBlockDepositWithdrawAndTransfer() throws Exception {
        long userOneId = createUser("Frozen Source", "frozen-source-" + suffix + "@test.com");
        long userTwoId = createUser("Active Destination", "active-destination-" + suffix + "@test.com");
        String sourceAccount = createAccount(userOneId, "1000");
        String destinationAccount = createAccount(userTwoId, "500");

        mockMvc.perform(patch("/api/v1/accounts/{accountNumber}/status", sourceAccount)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"FROZEN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"));

        mockMvc.perform(post("/api/v1/accounts/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountNumber":"%s","amount":10}
                                """.formatted(sourceAccount)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account is not ACTIVE"));

        mockMvc.perform(post("/api/v1/accounts/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountNumber":"%s","amount":10}
                                """.formatted(sourceAccount)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account is not ACTIVE"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountNumber":"%s","toAccountNumber":"%s","amount":10}
                                """.formatted(sourceAccount, destinationAccount)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account is not ACTIVE"));
    }

    @Test
    void rejectsInvalidStatusEnumWithBadRequest() throws Exception {
        long userId = createUser("Enum Tester", "enum-" + suffix + "@test.com");
        String accountNumber = createAccount(userId, "100");

        mockMvc.perform(patch("/api/v1/accounts/{accountNumber}/status", accountNumber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"SUSPENDED"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("invalid enum")));
    }

    private long createUser(String fullName, String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"%s","email":"%s"}
                                """.formatted(fullName, email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }

    private String createAccount(long userId, String initialDeposit) throws Exception {
        String response = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":%d,"initialDeposit":%s}
                                """.formatted(userId, initialDeposit)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("accountNumber").asText();
    }
}
