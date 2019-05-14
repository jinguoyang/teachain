package com.youzi.teaChain.bean;


import java.math.BigDecimal;

public class TTeachainOrdersTrade {

    private String id;
    private String homeUuid;
    private String awayUuid;
    private BigDecimal price;
    private BigDecimal count;
    private BigDecimal amount;
    private BigDecimal brokerage1;
    private BigDecimal brokerage2;
    private long status;
    private long type;
    private java.sql.Timestamp createTime;
    private java.sql.Timestamp tradeTime;
    private int opLock;

    public TTeachainOrdersTrade() {
    }

    public TTeachainOrdersTrade(String homeUuid, BigDecimal price, BigDecimal count, BigDecimal amount, BigDecimal brokerage1, BigDecimal brokerage2, long type) {
        this.homeUuid = homeUuid;
        this.price = price;
        this.count = count;
        this.amount = amount;
        this.brokerage1 = brokerage1;
        this.brokerage2 = brokerage2;
        this.type = type;
    }

    public int getOpLock() {
        return opLock;
    }

    public void setOpLock(int opLock) {
        this.opLock = opLock;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHomeUuid() {
        return homeUuid;
    }

    public void setHomeUuid(String homeUuid) {
        this.homeUuid = homeUuid;
    }

    public String getAwayUuid() {
        return awayUuid;
    }

    public void setAwayUuid(String awayUuid) {
        this.awayUuid = awayUuid;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCount() {
        return count;
    }

    public void setCount(BigDecimal count) {
        this.count = count;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBrokerage1() {
        return brokerage1;
    }

    public void setBrokerage1(BigDecimal brokerage1) {
        this.brokerage1 = brokerage1;
    }

    public BigDecimal getBrokerage2() {
        return brokerage2;
    }

    public void setBrokerage2(BigDecimal brokerage2) {
        this.brokerage2 = brokerage2;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public java.sql.Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(java.sql.Timestamp createTime) {
        this.createTime = createTime;
    }

    public java.sql.Timestamp getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(java.sql.Timestamp tradeTime) {
        this.tradeTime = tradeTime;
    }

}
