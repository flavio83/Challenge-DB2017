package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.AccountsService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

	@Autowired
	private AccountsService accountsService;

	@Test
	public void addAccount() throws Exception {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);
		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	public void makeTransfer() {
		String uniqueId = "Id-" + System.nanoTime();
		Account from = new Account(uniqueId, new BigDecimal(2000));
		this.accountsService.createAccount(from);

		uniqueId = "Id-" + System.nanoTime();
		Account to = new Account(uniqueId, new BigDecimal(1000));
		this.accountsService.createAccount(to);
		this.accountsService.doTransfer(from.getAccountId(), to.getAccountId(), new BigDecimal(1000));

		assertEquals(new BigDecimal(2000), to.getBalance());
	}

	@Test
	public void makeMultipleTransfer() {
		List<Account> accounts = new ArrayList<Account>(100);
		for (int i = 0; i < 100; i++) {
			Account newRandomAccount = new Account("Id-" + new SecureRandom().nextLong(),
					new BigDecimal(new Random().nextInt()).abs());
			accountsService.createAccount(newRandomAccount);
			accounts.add(newRandomAccount);
		}
		for (int i = 0; i < 1000; i++) {
			Account from = getRandomAccount(accounts);
			Account to = getRandomAccount(accounts, from);
			BigDecimal toBalance = to.getBalance();
			Integer amount = Math.abs(new Random().nextInt(1000000) + 100);
			this.accountsService.doTransfer(from.getAccountId(), to.getAccountId(), new BigDecimal(amount));
			assertEquals(toBalance.add(new BigDecimal(amount)), to.getBalance());
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

	@Test
	public void addAccount_failsOnDuplicateId() throws Exception {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}

	}
}
