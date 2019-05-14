package com.youzi.teaChain.bean;


import java.math.BigDecimal;
import java.util.Date;

public class TTeachainTransLog {

    private Integer id;
    private String uuid;
    private String aimUuid;
    private BigDecimal amount;
    private BigDecimal rate;
    private BigDecimal realCoin;
    private Integer type;
    private Date createTime;
    private Integer warnTag;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public String getAimUuid() {
        return aimUuid;
    }

    public void setAimUuid(String aimUuid) {
        this.aimUuid = aimUuid;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getRealCoin() {
        return realCoin;
    }

    public void setRealCoin(BigDecimal realCoin) {
        this.realCoin = realCoin;
    }


    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }


    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getWarnTag() {
        return warnTag;
    }

    public void setWarnTag(Integer warnTag) {
        this.warnTag = warnTag;
    }
}
