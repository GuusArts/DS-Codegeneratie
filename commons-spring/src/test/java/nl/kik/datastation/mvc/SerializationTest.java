package nl.kik.datastation.mvc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = SerializationController.class)
@AutoConfigureMockMvc
@Slf4j
public class SerializationTest {
	@Autowired
	private MockMvc mockMvc;

	@Test
	public void ask() throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/ask")) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		final String request = result.getResponse().getContentAsString();
		SerializationTest.log.info("Ask: {}", request);

		result = mockMvc.perform(MockMvcRequestBuilders.post("/ask").content(request)) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		Assertions.assertEquals(request, result.getResponse().getContentAsString());

		result = mockMvc.perform(MockMvcRequestBuilders.post("/response").content(request)) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		Assertions.assertEquals(request, result.getResponse().getContentAsString());

		mockMvc.perform(MockMvcRequestBuilders.post("/ask").content("AAAA" + request)) //
				.andExpect(MockMvcResultMatchers.status().is(400));
	}

	@Test
	public void construct() throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/construct")) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		final String request = result.getResponse().getContentAsString();
		SerializationTest.log.info("Construct: {}", request);

		result = mockMvc.perform(MockMvcRequestBuilders.post("/construct").content(request)) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		Assertions.assertEquals(request, result.getResponse().getContentAsString());

		result = mockMvc.perform(MockMvcRequestBuilders.post("/response").content(request)) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		Assertions.assertEquals(request, result.getResponse().getContentAsString());

		mockMvc.perform(MockMvcRequestBuilders.post("/construct").content("AAAA" + request)) //
				.andExpect(MockMvcResultMatchers.status().is(400));
	}

	@Test
	public void error() throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/error")) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		final String request = result.getResponse().getContentAsString();
		SerializationTest.log.info("Error: {}", request);

		result = mockMvc.perform(MockMvcRequestBuilders.post("/error").content(request)) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		Assertions.assertEquals(request, result.getResponse().getContentAsString());

		result = mockMvc.perform(MockMvcRequestBuilders.post("/response").content(request)) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		Assertions.assertEquals(request, result.getResponse().getContentAsString());

		mockMvc.perform(MockMvcRequestBuilders.post("/error").content("AAAA" + request)) //
				.andExpect(MockMvcResultMatchers.status().is(400));
	}

	@Test
	public void request() throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/request")) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		final String request = result.getResponse().getContentAsString();
		SerializationTest.log.info("Request: {}", request);

		result = mockMvc.perform(MockMvcRequestBuilders.post("/request").content(request)) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		Assertions.assertEquals(request, result.getResponse().getContentAsString());

		mockMvc.perform(MockMvcRequestBuilders.post("/request").content("AAAA" + request)) //
				.andExpect(MockMvcResultMatchers.status().is(400));
	}

	@Test
	public void select() throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/select")) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		final String request = result.getResponse().getContentAsString();
		SerializationTest.log.info("Select: {}", request);

		result = mockMvc.perform(MockMvcRequestBuilders.post("/select").content(request)) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		Assertions.assertEquals(request, result.getResponse().getContentAsString());

		result = mockMvc.perform(MockMvcRequestBuilders.post("/response").content(request)) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		Assertions.assertEquals(request, result.getResponse().getContentAsString());

		mockMvc.perform(MockMvcRequestBuilders.post("/select").content("AAAA" + request)) //
				.andExpect(MockMvcResultMatchers.status().is(400));
	}

	@Test
	public void vc() throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/vc")) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		final String request = result.getResponse().getContentAsString();
		SerializationTest.log.info("VC: {}", request);

		result = mockMvc.perform(MockMvcRequestBuilders.post("/vc").content(request)) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		Assertions.assertEquals(request, result.getResponse().getContentAsString());

		mockMvc.perform(MockMvcRequestBuilders.post("/vc").content("AAAA" + request)) //
				.andExpect(MockMvcResultMatchers.status().is(400));
	}

	@Test
	public void vp() throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/vp")) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		final String request = result.getResponse().getContentAsString();
		SerializationTest.log.info("VP: {}", request);

		result = mockMvc.perform(MockMvcRequestBuilders.post("/vp").content(request)) //
//				.andDo(print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andReturn();
		Assertions.assertEquals(request, result.getResponse().getContentAsString());

		mockMvc.perform(MockMvcRequestBuilders.post("/vp").content("AAAA" + request)) //
				.andExpect(MockMvcResultMatchers.status().is(400));
	}

}
