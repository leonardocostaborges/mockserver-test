package com.mockerservertest;

import javax.annotation.Resource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.HttpResponse;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import static org.mockserver.model.HttpRequest.request;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RestCtrlIT {

    private MockServerClient mockServerClient;

    @Resource
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeClass
    public static void startProxy() {
    }

    @AfterClass
    public static void stopProxy() {
    }

    @Before
    public void setupFixture() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        this.mockServerClient = new MockServerClient("localhost", 1090);
    }

    @After
    public void after() {
        this.mockServerClient.reset();
    }

    @Test
    public void shouldMockRealRequest() throws Exception {
        // given
        mockServerClient
            .when(
                request()
                    .withPath("/data/2.5/forecast")
                    .withQueryStringParameter("id", "524901")
                    .withQueryStringParameter("appid", "b1b15e88fa797225412429c1c50c122a1")
            )
            .respond(HttpResponse.response()
                .withStatusCode(200)
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withBody("{\"test\": \"testing\"}")
            );

        // when
        MvcResult response = mockMvc.perform(get("/test")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .characterEncoding("UTF-8")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();

        MatcherAssert.assertThat(
            response.getResponse().getContentAsString(),
            Matchers.is("{test=testing}")
        );
    }

}
