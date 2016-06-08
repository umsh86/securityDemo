package com.eomdev.template.account;

import com.eomdev.template.TemplateDemoApplication;
import lombok.extern.slf4j.Slf4j;
import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.LocalDateTime;

import static org.fest.assertions.Assertions.*;

/**
 * Created by eomdev on 2016. 4. 21..
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TemplateDemoApplication.class)
@Slf4j
@ActiveProfiles("local")
public class AccountTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void testAccountCreateDate() throws Exception {
        // given : 테스트 상황을 설정
        Account account = new Account();
        account.setName("testUser");
        LocalDateTime joinedDateTime = LocalDateTime.now();
        account.setJoined(joinedDateTime);

        // when : 테스트 대상을 실행
        Account afterAccount = accountRepository.save(account);
        log.info("{}", afterAccount.toString());

        // then : 결과를 검증
        assertThat(afterAccount.getJoined()).isEqualTo(joinedDateTime);

    }

}
