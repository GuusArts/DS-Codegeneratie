package nl.kik.commons.datastation.mvc;

import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = SerializationController.class)
@AutoConfigureMockMvc
@Slf4j
class SerializationTest {
    @Autowired
    private MockMvc mockMvc;

    // KIK-V ceredenitals

    @Test
    void query() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/kikv/query")) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("query: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/kikv/query").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/kikv/query").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/kikv/query").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    // KIK-V didcomm protocol

    @Test
    void request() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/didcomm/request")) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("request: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(MockMvcRequestBuilders.post("/didcomm/request").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
//        Assertions.assertEquals(request, result.getResponse().getContentAsString());
        SerializationTest.log.info("received 1: {}", new JSONObject(result.getResponse().getContentAsString()).toString(2));

        result = mockMvc.perform(MockMvcRequestBuilders.post("/didcomm/request").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
//        Assertions.assertEquals(request, result.getResponse().getContentAsString());
        SerializationTest.log.info("received 2: {}", new JSONObject(result.getResponse().getContentAsString()).toString(2));

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/didcomm/pass").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
//        Assertions.assertEquals(request, result.getResponse().getContentAsString());
        SerializationTest.log.info("received 3: {}", new JSONObject(result.getResponse().getContentAsString()).toString(2));

        mockMvc.perform(MockMvcRequestBuilders.post("/didcomm/request").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    void error() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/didcomm/error")) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("error: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/didcomm/error").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/didcomm/error").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/didcomm/pass").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/didcomm/error").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    void response() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/didcomm/response")) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("responseASK: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(MockMvcRequestBuilders.post("/didcomm/response").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(MockMvcRequestBuilders.post("/didcomm/response").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/didcomm/pass").content(request).contentType(MediaType.APPLICATION_JSON)) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/didcomm/response").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    // NUTS VC operations

    @Test
    void createVC() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/nuts/createvc")) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("createVC: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/nuts/createvc").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/nuts/createvc").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/nuts/createvc").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    void searchVC() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/nuts/searchvc")) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("searchVC: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/nuts/searchvc").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/nuts/searchvc").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/nuts/searchvc").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    void searchResult() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/nuts/searchresult")) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("searchResult: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(MockMvcRequestBuilders.post("/nuts/searchresult").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(MockMvcRequestBuilders.post("/nuts/searchresult").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/nuts/searchresult").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    void verifyVC() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/nuts/verifyvc")) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("verifyVC: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/nuts/verifyvc").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/nuts/verifyvc").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/nuts/verifyvc").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    void verificationResult() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/nuts/verificationresult")) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("verificationResult: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(MockMvcRequestBuilders.post("/nuts/verificationresult").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(MockMvcRequestBuilders.post("/nuts/verificationresult").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/nuts/verificationresult").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    // NUTS VP metyhods

    @Test
    void createVP() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/nuts/createvp")) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("createVP: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/nuts/createvp").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/nuts/createvp").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/nuts/createvp").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    void verifyVP() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/nuts/verifyvp")) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("verifyVP: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/nuts/verifyvp").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/nuts/verifyvp").content(request).contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/nuts/verifyvp").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    void presentationVerificationResult() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/nuts/presentationverificationresult")) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("presentationVerificationResult: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(MockMvcRequestBuilders.post("/nuts/presentationverificationresult").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(MockMvcRequestBuilders.post("/nuts/presentationverificationresult").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/nuts/presentationverificationresult").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    // NUTS crypto methods

    @Test
    void signResultSet() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/nuts/signresultset")) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("signResultSet: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(MockMvcRequestBuilders.post("/nuts/signresultset").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(MockMvcRequestBuilders.post("/nuts/signresultset").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/nuts/signresultset").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    // NUTS auth methods

    @Test
    void createJwt() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/nuts/createjwt")) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("createJwt: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/nuts/createjwt").content(request).contentType(MediaType.APPLICATION_JSON)) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/nuts/createjwt").content(request).contentType(MediaType.APPLICATION_JSON)) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/nuts/createjwt").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    void grantedJwt() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/nuts/grantedjwt")) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("grantedJwt: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(MockMvcRequestBuilders.post("/nuts/grantedjwt").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(MockMvcRequestBuilders.post("/nuts/grantedjwt").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/nuts/grantedjwt").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    void createToken() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/nuts/createtoken")) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("createToken: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(MockMvcRequestBuilders.post("/nuts/createtoken").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(MockMvcRequestBuilders.post("/nuts/createtoken").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/nuts/createtoken").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    void token() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/nuts/token")) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("token: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/nuts/token").content(request).contentType(MediaType.APPLICATION_JSON)) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/nuts/token").content(request).contentType(MediaType.APPLICATION_JSON)) //
//              .andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/nuts/token").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

}
