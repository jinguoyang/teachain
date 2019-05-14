package com.youzi.teaChain.bean;

import java.math.BigDecimal;

public class TUser {

    private Integer id;
    private String uuid;
    private String name;
    private String nickName;
    private String icon;
    private String wechatNum;
    private String phone;
    private Integer superior;
    private Integer sex;
    private Integer level;
    private Integer treeLevel;
    private Integer waterLevel;
    private Integer treeType;
    private BigDecimal totalMoney;
    private BigDecimal calcPower;
    private BigDecimal tempCalcPower;
    private Integer awardState;
    private Integer hasPayPassword;
    private Integer hasLoginPassword;
    private String idCardNum;
    private String idCardName;
    private Integer tradeLicense;
    private Integer hasFirstTCC;
    private Integer diamond;
    private Integer vipLevel;
    private Integer vipValidDay;

    public Integer getTradeLicense() {
        return tradeLicense;
    }

    public void setTradeLicense(Integer tradeLicense) {
        this.tradeLicense = tradeLicense;
    }

    public String getIdCardNum() {
        return idCardNum;
    }

    public void setIdCardNum(String idCardNum) {
        this.idCardNum = idCardNum;
    }

    public String getIdCardName() {
        return idCardName;
    }

    public void setIdCardName(String idCardName) {
        this.idCardName = idCardName;
    }

    public Integer getHasPayPassword() {
        return hasPayPassword;
    }

    public void setHasPayPassword(Integer hasPayPassword) {
        this.hasPayPassword = hasPayPassword;
    }

    public Integer getHasLoginPassword() {
        return hasLoginPassword;
    }

    public void setHasLoginPassword(Integer hasLoginPassword) {
        this.hasLoginPassword = hasLoginPassword;
    }

    public BigDecimal getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(BigDecimal totalMoney) {
        this.totalMoney = totalMoney;
    }

    public BigDecimal getCalcPower() {
        return calcPower;
    }

    public void setCalcPower(BigDecimal calcPower) {
        this.calcPower = calcPower;
    }

    public BigDecimal getTempCalcPower() {
        return tempCalcPower;
    }

    public void setTempCalcPower(BigDecimal tempCalcPower) {
        this.tempCalcPower = tempCalcPower;
    }

    public Integer getAwardState() {
        return awardState;
    }

    public void setAwardState(Integer awardState) {
        this.awardState = awardState;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getWechatNumber() {
        return wechatNum;
    }

    public void setWechatNumber(String wechatNum) {
        this.wechatNum = wechatNum;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getSuperior() {
        return superior;
    }

    public void setSuperior(Integer superior) {
        this.superior = superior;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getTreeLevel() {
        return treeLevel;
    }

    public void setTreeLevel(Integer treeLevel) {
        this.treeLevel = treeLevel;
    }

    public Integer getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(Integer waterLevel) {
        this.waterLevel = waterLevel;
    }

    public Integer getTreeType() {
        return treeType;
    }

    public void setTreeType(Integer treeType) {
        this.treeType = treeType;
    }

    public String getWechatNum() {
        return wechatNum;
    }

    public void setWechatNum(String wechatNum) {
        this.wechatNum = wechatNum;
    }

    public Integer getHasFirstTCC() {
        return hasFirstTCC;
    }

    public void setHasFirstTCC(Integer hasFirstTCC) {
        this.hasFirstTCC = hasFirstTCC;
    }

    public Integer getDiamond() {
        return diamond;
    }

    public void setDiamond(Integer diamond) {
        this.diamond = diamond;
    }

    public Integer getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(Integer vipLevel) {
        this.vipLevel = vipLevel;
    }

    public Integer getVipValidDay() {
        return vipValidDay;
    }

    public void setVipValidDay(Integer vipValidDay) {
        this.vipValidDay = vipValidDay;
    }
}
