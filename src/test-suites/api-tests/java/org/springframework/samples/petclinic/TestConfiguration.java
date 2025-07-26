package org.springframework.samples.petclinic;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@TestConfiguration
public class TestConfiguration {

	@Bean
	public MockMvc mockMvc(WebApplicationContext context) {
		return MockMvcBuilders.webAppContextSetup(context).build();
	}

}