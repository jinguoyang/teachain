package com.youzi.teaChain.bean;


import java.util.Date;

public class TTeachainPowerUp {

    private long id;
    private String uuid;
    private String support;
    private long type;
    private double accelerate;
    private long temp;
    private Date createTime;
    private Date invaildTime;

    public TTeachainPowerUp() {
    }

    public TTeachainPowerUp(String uuid, String support, long type, double accelerate, long temp, Date createTime, Date invaildTime) {
        this.uuid = uuid;
        this.support = support;
        this.type = type;
        this.accelerate = accelerate;
        this.temp = temp;
        this.createTime = createTime;
        this.invaildTime = invaildTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public String getSupport() {
        return support;
    }

    public void setSupport(String support) {
        this.support = support;
    }


    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }


    public double getAccelerate() {
        return accelerate;
    }

    public void setAccelerate(double accelerate) {
        this.accelerate = accelerate;
    }


    public long getTemp() {
        return temp;
    }

    public void setTemp(long temp) {
        this.temp = temp;
    }


    public Date getInvaildTime() {
        return invaildTime;
    }

    public void setInvaildTime(Date invaildTime) {
        this.invaildTime = invaildTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
