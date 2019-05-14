package com.youzi.teaChain.bean;


import java.math.BigDecimal;
import java.util.Date;

public class TTeachainPatronsaintUser {

    private Integer id;
    private String uuid;
    private BigDecimal golden;
    private BigDecimal tea;
    private BigDecimal diamond;
    private BigDecimal genTea;
    private BigDecimal genTeaMultiple;
    private Integer allLevel;
    private Integer maxLevel;
    private Date createTime;
    private BigDecimal totalTea;
    private BigDecimal todaySun;
    private Integer putDay;


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


    public BigDecimal getGolden() {
        return golden;
    }

    public void setGolden(BigDecimal golden) {
        this.golden = golden;
    }

    public BigDecimal getTea() {
        return tea;
    }

    public void setTea(BigDecimal tea) {
        this.tea = tea;
    }

    public BigDecimal getDiamond() {
        return diamond;
    }

    public void setDiamond(BigDecimal diamond) {
        this.diamond = diamond;
    }

    public BigDecimal getGenTea() {
        return genTea;
    }

    public void setGenTea(BigDecimal genTea) {
        this.genTea = genTea;
    }

    public BigDecimal getGenTeaMultiple() {
        return genTeaMultiple;
    }

    public void setGenTeaMultiple(BigDecimal genTeaMultiple) {
        this.genTeaMultiple = genTeaMultiple;
    }

    public Integer getAllLevel() {
        return allLevel;
    }

    public void setAllLevel(Integer allLevel) {
        this.allLevel = allLevel;
    }


    public Integer getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(Integer maxLevel) {
        this.maxLevel = maxLevel;
    }


    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public BigDecimal getTotalTea() {
        return totalTea;
    }

    public void setTotalTea(BigDecimal totalTea) {
        this.totalTea = totalTea;
    }

    public BigDecimal getTodaySun() {
        return todaySun;
    }

    public void setTodaySun(BigDecimal todaySun) {
        this.todaySun = todaySun;
    }

    public Integer getPutDay() {
        return putDay;
    }

    public void setPutDay(Integer putDay) {
        this.putDay = putDay;
    }
}
