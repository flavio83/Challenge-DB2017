package com.db.awmd.challenge;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.Test;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;

@Test
@RunWith(SpringRunner.class)
@SpringBootTest
public class MultiThreadAccountsServiceTest extends AbstractTestNGSpringContextTests {

	  @Autowired
	  private AccountsService accountsService;

	  @Autowired
	  private WebApplicationContext webApplicationContext;
	
	//@Test(threadPoolSize = 3, invocationCount = 6, timeOut = 1000)
    public void beforeMethod() {
        long id = Thread.currentThread().getId();
        System.out.println("Before test-method. Thread id is: " + id);
    }

	public MultiThreadAccountsServiceTest() {
		// TODO Auto-generated constructor stub
	}
	
	
	@Test(threadPoolSize = 30, invocationCount = 10,  timeOut = 10000)
	  public void makeMultipleTransfer() {
		  List<Account> accounts = new ArrayList<Account>(100);
		  for(int i=0;i<100;i++) {
			  Account newRandomAccount = new Account("Id-" + new SecureRandom().nextLong(),new BigDecimal(new Random().nextInt()).abs());
			  accountsService.createAccount(newRandomAccount);
			  accounts.add(newRandomAccount);
		  }
		    for(int i=0;i<1000;i++) {
		    		Account from = getRandomAccount(accounts);
		    		Account to = getRandomAccount(accounts,from);
		    		BigDecimal toBalance = to.getBalance();
		    		Integer amount = Math.abs(new Random().nextInt(1000000)+100);
		    		this.accountsService.doTransfer(from.getAccountId(), to.getAccountId(), new BigDecimal(amount));
		    		assertEquals(toBalance.add(new BigDecimal(amount)),to.getBalance());
		    }
	  }
	
	public Account getRandomAccount(List<Account> accounts, Account... exclude) {
		  Account acc = null;
		  do {
			  Collections.shuffle(accounts);
			  acc = accounts.get(0);
		  } while(exclude.length==1 && acc.equals(exclude[0]));
		  return acc;
	  }


}


