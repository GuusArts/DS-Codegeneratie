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
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(MockMvcRequestBuilders.post("/didcomm/request").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
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
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        final String request = result.getResponse().getContentAsString();
        SerializationTest.log.info("presentationVerificationResult: {}", new JSONObject(request).toString(2));

        result = mockMvc.perform(MockMvcRequestBuilders.post("/nuts/presentationverificationresult").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        result = mockMvc.perform(MockMvcRequestBuilders.post("/nuts/presentationverificationresult").content(request)
                .contentType(MediaType.APPLICATION_JSON)) //
//				.andDo(print()) //
                .andExpect(MockMvcResultMatchers.status().isOk()) //
                .andReturn();
        Assertions.assertEquals(request, result.getResponse().getContentAsString());

        mockMvc.perform(MockMvcRequestBuilders.post("/nuts/presentationverificationresult").content("AAAA" + request)
                .contentType(MediaType.APPLICATION_JSON)) //
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

//	@Test
//	void ask() throws Exception {
//		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/ask")) //
////				.andDo(print()) //
//				.andExpect(MockMvcResultMatchers.status().isOk()) //
//				.andReturn();
//		final String request = result.getResponse().getContentAsString();
//		SerializationTest.log.info("Ask: {}", request);
//
//		result = mockMvc.perform(MockMvcRequestBuilders.post("/ask").content(request)) //
////				.andDo(print()) //
//				.andExpect(MockMvcResultMatchers.status().isOk()) //
//				.andReturn();
//		Assertions.assertEquals(request, result.getResponse().getContentAsString());
//
//		result = mockMvc.perform(MockMvcRequestBuilders.post("/response").content(request)) //
////				.andDo(print()) //
//				.andExpect(MockMvcResultMatchers.status().isOk()) //
//				.andReturn();
//		Assertions.assertEquals(request, result.getResponse().getContentAsString());
//
//		mockMvc.perform(MockMvcRequestBuilders.post("/ask").content("AAAA" + request)) //
//				.andExpect(MockMvcResultMatchers.status().is(400));
//	}

//	@Test
//	void construct() throws Exception {
//		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/construct")) //
////				.andDo(print()) //
//				.andExpect(MockMvcResultMatchers.status().isOk()) //
//				.andReturn();
//		final String request = result.getResponse().getContentAsString();
//		SerializationTest.log.info("Construct: {}", request);
//
//		result = mockMvc.perform(MockMvcRequestBuilders.post("/construct").content(request)) //
////				.andDo(print()) //
//				.andExpect(MockMvcResultMatchers.status().isOk()) //
//				.andReturn();
//		Assertions.assertEquals(request, result.getResponse().getContentAsString());
//
//		result = mockMvc.perform(MockMvcRequestBuilders.post("/response").content(request)) //
////				.andDo(print()) //
//				.andExpect(MockMvcResultMatchers.status().isOk()) //
//				.andReturn();
//		Assertions.assertEquals(request, result.getResponse().getContentAsString());
//
//		mockMvc.perform(MockMvcRequestBuilders.post("/construct").content("AAAA" + request)) //
//				.andExpect(MockMvcResultMatchers.status().is(400));
//	}
//
//	@Test
//	void error() throws Exception {
//		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/error")) //
////				.andDo(print()) //
//				.andExpect(MockMvcResultMatchers.status().isOk()) //
//				.andReturn();
//		final String request = result.getResponse().getContentAsString();
//		SerializationTest.log.info("Error: {}", request);
//
//		result = mockMvc.perform(MockMvcRequestBuilders.post("/error").content(request)) //
////				.andDo(print()) //
//				.andExpect(MockMvcResultMatchers.status().isOk()) //
//				.andReturn();
//		Assertions.assertEquals(request, result.getResponse().getContentAsString());
//
//		result = mockMvc.perform(MockMvcRequestBuilders.post("/response").content(request)) //
////				.andDo(print()) //
//				.andExpect(MockMvcResultMatchers.status().isOk()) //
//				.andReturn();
//		Assertions.assertEquals(request, result.getResponse().getContentAsString());
//
//		mockMvc.perform(MockMvcRequestBuilders.post("/error").content("AAAA" + request)) //
//				.andExpect(MockMvcResultMatchers.status().is(400));
//	}
//
//	@Test
//	void request() throws Exception {
//		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/request")) //
////				.andDo(print()) //
//				.andExpect(MockMvcResultMatchers.status().isOk()) //
//				.andReturn();
//		final String request = result.getResponse().getContentAsString();
//		SerializationTest.log.info("Request: {}", request);
//
//		result = mockMvc.perform(MockMvcRequestBuilders.post("/request").content(request)) //
////				.andDo(print()) //
//				.andExpect(MockMvcResultMatchers.status().isOk()) //
//				.andReturn();
//		Assertions.assertEquals(request, result.getResponse().getContentAsString());
//
//		mockMvc.perform(MockMvcRequestBuilders.post("/request").content("AAAA" + request)) //
//				.andExpect(MockMvcResultMatchers.status().is(400));
//	}
//
//	@Test
//	void select() throws Exception {
//		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/select")) //
////				.andDo(print()) //
//				.andExpect(MockMvcResultMatchers.status().isOk()) //
//				.andReturn();
//		final String request = result.getResponse().getContentAsString();
//		SerializationTest.log.info("Select: {}", request);
//
//		result = mockMvc.perform(MockMvcRequestBuilders.post("/select").content(request)) //
////				.andDo(print()) //
//				.andExpect(MockMvcResultMatchers.status().isOk()) //
//				.andReturn();
//		Assertions.assertEquals(request, result.getResponse().getContentAsString());
//
//		result = mockMvc.perform(MockMvcRequestBuilders.post("/response").content(request)) //
////				.andDo(print()) //
//				.andExpect(MockMvcResultMatchers.status().isOk()) //
//				.andReturn();
//		Assertions.assertEquals(request, result.getResponse().getContentAsString());
//
//		mockMvc.perform(MockMvcRequestBuilders.post("/select").content("AAAA" + request)) //
//				.andExpect(MockMvcResultMatchers.status().is(400));
//	}

}
