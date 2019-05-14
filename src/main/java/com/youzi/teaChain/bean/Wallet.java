package com.youzi.teaChain.bean;

import java.math.BigDecimal;

public class Wallet {
    private String uuid;
    private BigDecimal balance;
    private String coinAddress;
    private String payPassword;
    private Integer transLock;

    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCoinAddress() {
        return coinAddress;
    }

    public void setCoinAddress(String coinAddress) {
        this.coinAddress = coinAddress;
    }

    public Integer getTransLock() {
        return transLock;
    }

    public void setTransLock(Integer transLock) {
        this.transLock = transLock;
    }
}
