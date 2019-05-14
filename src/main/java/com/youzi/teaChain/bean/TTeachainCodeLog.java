package com.youzi.teaChain.bean;


import java.math.BigInteger;
import java.util.Date;

public class TTeachainCodeLog {

    private Integer id;
    private String uuid;
    private BigInteger phone;
    private String code;
    private String res;
    private Integer status;
    private Date invaildTime;


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


    public BigInteger getPhone() {
        return phone;
    }

    public void setPhone(BigInteger phone) {
        this.phone = phone;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public String getRes() {
        return res;
    }

    public void setRes(String res) {
        this.res = res;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getInvaildTime() {
        return invaildTime;
    }

    public void setInvaildTime(Date invaildTime) {
        this.invaildTime = invaildTime;
    }
}
