package com.eomdev.template;

import com.eomdev.template.account.Account;
import com.eomdev.template.account.AccountRepository;
import com.eomdev.template.account.RoleType;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;

@SpringBootApplication
public class TemplateDemoApplication {

	@Autowired
	private PasswordEncoder encoder;

	public static void main(String[] args) {
		SpringApplication.run(TemplateDemoApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	@Profile("local")
	public CommandLineRunner runner(AccountRepository accountRepository){
		return args -> {
			Arrays.asList(
					new Account("admin@gmail.com", encoder.encode("1224"), "adminName", LocalDateTime.now(), RoleType.ROLE_ADMIN),
					new Account("eom@gmail.com", encoder.encode("1212"), "userName", LocalDateTime.now(), RoleType.ROLE_USER)
			).forEach(account -> accountRepository.save(account));

			accountRepository.findAll().forEach(System.out::println);

		};

	}


}
