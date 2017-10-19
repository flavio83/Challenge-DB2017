package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Transfer {

  @Getter
  @Setter
  @NotNull
  private String fromAccountId;
  
  @Getter
  @Setter
  @NotNull
  private String toAccountId;

  @Getter
  @Setter
  @NotNull
  private BigDecimal amount;
  
  @JsonCreator
  public Transfer(
		  @JsonProperty("accountId") String fromAccountId,
		  @JsonProperty("toAccountId") String toAccountId,
		  @JsonProperty("amount") String amount) {
    this.fromAccountId = fromAccountId;
    this.toAccountId = toAccountId;
    this.amount = new BigDecimal(amount);
  }

}
