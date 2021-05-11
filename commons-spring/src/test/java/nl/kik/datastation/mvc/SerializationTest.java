package nl.kik.datastation.mvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = SerializationController.class)
@AutoConfigureMockMvc
@Slf4j
public class SerializationTest {
	@Autowired
	private MockMvc mockMvc;

	@Test
	public void request() throws Exception {
		MvcResult result = mockMvc.perform(get("/request")) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		String request = result.getResponse().getContentAsString();
		log.info("Request: {}", request);

		result = mockMvc.perform(post("/request").content(request)) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		assertEquals(request, result.getResponse().getContentAsString());

		mockMvc.perform(post("/request").content("AAAA" + request)) //
				.andExpect(status().is(400));
	}

	@Test
	public void error() throws Exception {
		MvcResult result = mockMvc.perform(get("/error")) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		String request = result.getResponse().getContentAsString();
		log.info("Error: {}", request);

		result = mockMvc.perform(post("/error").content(request)) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		assertEquals(request, result.getResponse().getContentAsString());

		result = mockMvc.perform(post("/response").content(request)) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		assertEquals(request, result.getResponse().getContentAsString());

		mockMvc.perform(post("/error").content("AAAA" + request)) //
				.andExpect(status().is(400));
	}

	@Test
	public void select() throws Exception {
		MvcResult result = mockMvc.perform(get("/select")) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		String request = result.getResponse().getContentAsString();
		log.info("Select: {}", request);

		result = mockMvc.perform(post("/select").content(request)) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		assertEquals(request, result.getResponse().getContentAsString());

		result = mockMvc.perform(post("/response").content(request)) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		assertEquals(request, result.getResponse().getContentAsString());

		mockMvc.perform(post("/select").content("AAAA" + request)) //
				.andExpect(status().is(400));
	}

	@Test
	public void construct() throws Exception {
		MvcResult result = mockMvc.perform(get("/construct")) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		String request = result.getResponse().getContentAsString();
		log.info("Construct: {}", request);

		result = mockMvc.perform(post("/construct").content(request)) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		assertEquals(request, result.getResponse().getContentAsString());

		result = mockMvc.perform(post("/response").content(request)) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		assertEquals(request, result.getResponse().getContentAsString());

		mockMvc.perform(post("/construct").content("AAAA" + request)) //
				.andExpect(status().is(400));
	}

	@Test
	public void ask() throws Exception {
		MvcResult result = mockMvc.perform(get("/ask")) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		String request = result.getResponse().getContentAsString();
		log.info("Ask: {}", request);

		result = mockMvc.perform(post("/ask").content(request)) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		assertEquals(request, result.getResponse().getContentAsString());

		result = mockMvc.perform(post("/response").content(request)) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		assertEquals(request, result.getResponse().getContentAsString());

		mockMvc.perform(post("/ask").content("AAAA" + request)) //
				.andExpect(status().is(400));
	}

	@Test
	public void vp() throws Exception {
		MvcResult result = mockMvc.perform(get("/vp")) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		String request = result.getResponse().getContentAsString();
		log.info("VP: {}", request);

		result = mockMvc.perform(post("/vp").content(request)) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		assertEquals(request, result.getResponse().getContentAsString());

		mockMvc.perform(post("/vp").content("AAAA" + request)) //
				.andExpect(status().is(400));
	}

	@Test
	public void vc() throws Exception {
		MvcResult result = mockMvc.perform(get("/vc")) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		String request = result.getResponse().getContentAsString();
		log.info("VC: {}", request);

		result = mockMvc.perform(post("/vc").content(request)) //
//				.andDo(print()) //
				.andExpect(status().isOk()) //
				.andReturn();
		assertEquals(request, result.getResponse().getContentAsString());

		mockMvc.perform(post("/vc").content("AAAA" + request)) //
				.andExpect(status().is(400));
	}

}
