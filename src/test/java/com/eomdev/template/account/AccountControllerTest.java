package com.eomdev.template.account;

import com.eomdev.template.TemplateDemoApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by eomdev on 2015. 9. 7..
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TemplateDemoApplication.class)
@WebAppConfiguration
@Transactional
@Slf4j
public class AccountControllerTest {

    @Autowired
    WebApplicationContext wac;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private AccountService service;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    MockMvc mockMvc;

    @Before
    public void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(springSecurityFilterChain)
                .build();
    }


    @Test
    public void createAccount() throws Exception {
        // given
        AccountDto.Create creatDto = accountCreateFixtureDto();

        ResultActions result = mockMvc.perform(
                post("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creatDto))
        );

        result.andDo(print());  // 프린트
        result.andExpect(status().isCreated());

        //JSONPATH : Body = {"message":"[testUser@eomdev.com] 중복된 email 주소 입니다.","code":"duplicated.username.exception","errors":null}
        result.andExpect(jsonPath("$.name", is("testUser")));

        // 중복 에러 테스트
        result = mockMvc.perform(
                post("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creatDto))
        );

        result.andDo(print());  // 프린트
        result.andExpect(status().isBadRequest());
        result.andExpect(jsonPath("$.code", is("duplicated.username.exception")));


    }

    // fixture factory
    private AccountDto.Create accountCreateFixtureDto() {
        AccountDto.Create createDto = new AccountDto.Create();
        createDto.setEmail("testUser@eomdev.com");
        createDto.setPassword("testUserPassword");
        createDto.setName("testUser");
        return createDto;
    }


    @Test
    public void createAccount_BadRequest() throws Exception {
        AccountDto.Create creatDto = new AccountDto.Create();
        creatDto.setName(" ");
        creatDto.setPassword("1234");
        creatDto.setEmail("testCreateAccount@eomdev.com");

        ResultActions result = mockMvc.perform(
                post("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creatDto))
        );

        result.andDo(print());
        result.andExpect(status().isBadRequest());
        result.andExpect(jsonPath("$.code", is("bad.request")));
    }



    @Test
    public void getAccounts() throws Exception {
        // given
        AccountDto.Create createDto = accountCreateFixtureDto();
        Account account = service.createAccount(createDto);


        ResultActions result = mockMvc.perform(
                get("/account")
                        .with(user("testUser@eomdev.com").password("testUserPassword").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        result.andDo(print());
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.name", is("testUser")));


    }

    
    @Test
    public void updateAccount() throws Exception {

        AccountDto.Create createDto = accountCreateFixtureDto();
        Account account = service.createAccount(createDto);

        AccountDto.Update updateDto = new AccountDto.Update();
        updateDto.setName("eomDaeng");
        updateDto.setPassword("1224");

        ResultActions result = mockMvc.perform(
                put("/account")
                        .with(
                                user(account.getEmail())
                                        .password(updateDto.getPassword())
                                        .roles("USER")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)));

        result.andDo(print());
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.name", is("eomDaeng")));

    }

    @Test
    public void deleteAccount() throws Exception {

        AccountDto.Create createDto = accountCreateFixtureDto();
        Account account = service.createAccount(createDto);

        // given
        ResultActions result = mockMvc.perform(
                delete("/account")
                        .with(
                                user(createDto.getEmail())
                                .password(createDto.getPassword())
                                .roles("USER")
                        )
        );
                result.andDo(print());
                result.andExpect(status().isNoContent());

    }

}
