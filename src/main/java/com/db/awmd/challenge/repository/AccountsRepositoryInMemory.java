package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.OverdraftAccountException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

  private final Map<String, Account> accounts = new ConcurrentHashMap<>();

  @Override
  public void createAccount(Account account) throws DuplicateAccountIdException {
    Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
    if (previousAccount != null) {
      throw new DuplicateAccountIdException(
        "Account id " + account.getAccountId() + " already exists!");
    }
  }

  @Override
  public Account getAccount(String accountId) {
    return accounts.get(accountId);
  }

  @Override
  public void clearAccounts() {
    accounts.clear();
  }
  
  public Account transfer(String accountFrom, String accountTo, BigDecimal amount) {
	  if(accounts.compute(accountFrom, (key,value) -> value.withdraw(amount))!=null) {
		  return accounts.compute(accountTo, (key,value) -> value.deposit(amount));
	  } else {
		  throw new OverdraftAccountException(
			        "Account id " + accountFrom + " does not have enough fund in order to perform this transfer");
	  }
  }  

}
