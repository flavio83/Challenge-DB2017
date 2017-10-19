package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void prepareMockMvc() {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

		// Reset the existing accounts before each test.
		accountsService.getAccountsRepository().clearAccounts();
	}

	@Test
	public void createAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		Account account = accountsService.getAccount("Id-123");
		assertThat(account.getAccountId()).isEqualTo("Id-123");
		assertThat(account.getBalance()).isEqualByComparingTo("1000");
	}

	@Test
	public void createDuplicateAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNoAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"balance\":1000}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNoBalance() throws Exception {
		this.mockMvc.perform(
				post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"accountId\":\"Id-123\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNoBody() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNegativeBalance() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountEmptyAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void getAccount() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId)).andExpect(status().isOk())
				.andExpect(content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
	}

	@Test
	public void makeTransfer() throws Exception {
		String fromAccountId = "Id-" + System.nanoTime();
		Account account = new Account(fromAccountId, new BigDecimal("823.11"));
		this.accountsService.createAccount(account);

		String toAccountId = "Id-" + System.nanoTime();
		account = new Account(toAccountId, new BigDecimal("233.15"));
		this.accountsService.createAccount(account);

		mockMvc.perform(
				post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON).content("{\"fromAccountId\":\""
						+ fromAccountId + "\",\"toAccountId\":\"" + toAccountId + "\",\"amount\":623.45}"))
				.andExpect(status().isAccepted());
	}

	@Test
	public void makeMultiTransfer() throws Exception {
		List<Account> accounts = new ArrayList<Account>(100);
		for (int i = 0; i < 100; i++) {
			Account newRandomAccount = new Account("Id-" + new SecureRandom().nextLong(),
					new BigDecimal(new Random().nextInt()).abs());
			accountsService.createAccount(newRandomAccount);
			accounts.add(newRandomAccount);
		}
		for (int i = 0; i < 1000; i++) {
			Account fromAccountId = getRandomAccount(accounts);
			Account toAccountId = getRandomAccount(accounts, fromAccountId);
			Integer amount = Math.abs(new Random().nextInt(1000000) + 100);
			String jsonReq = ("{\"fromAccountId\":\"" + fromAccountId.getAccountId() + "\",\"toAccountId\":\""
					+ toAccountId.getAccountId() + "\",\"amount\":" + amount + "}");
			mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON).content(jsonReq))
					.andExpect(status().isAccepted());

		}
	}

	public Account getRandomAccount(List<Account> accounts, Account... exclude) {
		Account acc = null;
		do {
			Collections.shuffle(accounts);
			acc = accounts.get(0);
		} while (exclude.length == 1 && acc.equals(exclude[0]));
		return acc;
	}

}
