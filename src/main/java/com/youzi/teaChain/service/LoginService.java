package com.youzi.teaChain.service;

import com.youzi.teaChain.bean.TTeachainLogin;
import com.youzi.teaChain.dao.LoginMapper;
import com.youzi.teaChain.dao.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.Map;

@Service
public class LoginService {
    private Logger log = LoggerFactory.getLogger(LoginService.class);

    @Resource
    private LoginMapper loginMapper;
    @Resource
    private UserMapper userMapper;

    public int countDeviceId(String deviceId){
        try {
            return loginMapper.countDeviceId(deviceId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("countDeviceId 设备ID统计出错：" + e.getMessage());
            return 0;
        }
    }

    public Boolean clearOtherDeviceIdByOpenId(String openId,String deviceId){
        try {
            loginMapper.setOtherDeviceIdByOpenId(openId,deviceId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("clearOtherDeviceIdByOpenId 清除多余设备ID出错：" + e.getMessage());
            return false;
        }
    }

    public TTeachainLogin getLoginByDeviceId(String deviceId) {
        try {
            return loginMapper.getLoginByDeviceId(deviceId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("selectLoginByDeviceId 通过设备ID查询用户出错：" + e.getMessage());
            return null;
        }
    }

    public Boolean setDeviceIdAndIpByOpenId(String openId,String deviceId,String ip){
        try {
            boolean notChangeDevice = loginMapper.checkDeviceIdByOpenId(openId,deviceId,ip);
            if(!notChangeDevice){
                loginMapper.setDeviceIdAndIpByOpenId(openId,deviceId,ip);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setDeviceIdByOpenId 修改设备ID出错：" + e.getMessage());
            return false;
        }
    }

    public Boolean setDeviceIdAndIpByPhone(String phone,String deviceId,String ip){
        try {
            boolean notChangeDevice = loginMapper.checkDeviceIdByPhone(phone,deviceId,ip);
            if(!notChangeDevice){
//                System.out.println("更新了设备ID");
                loginMapper.setDeviceIdAndIpByPhone(phone,deviceId,ip);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setDeviceIdByOpenId 修改设备ID出错：" + e.getMessage());
            return false;
        }
    }

    public Boolean setStatusByDeviceId(String deviceId,Integer status){
        try {
            loginMapper.setStatusByDeviceId(deviceId,status);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setStatusByDeviceId 修改登录状态出错：" + e.getMessage());
            return false;
        }
    }

    public Boolean updateUnionId(String openId,String unionId){
        try {
            loginMapper.setUnionIdByOpenId(openId,unionId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setStatusByDeviceId 修改登录状态出错：" + e.getMessage());
            return false;
        }
    }

    public Boolean logoutByDeviceId(String deviceId) {
        try {
            loginMapper.setStatusByDeviceId(deviceId,0);
            loginMapper.setOtherDeviceIdByOpenId("1",deviceId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("logoutByDeviceId 用户登出出错：" + e.getMessage());
            return false;
        }
    }

    public TTeachainLogin checkLoginByOpenId(String openId) {
        try {
            return loginMapper.checkLoginByOpenId(openId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("checkLoginByOpenId 通过OpenId查询用户出错：" + e.getMessage());
            return null;
        }
    }

    public Boolean insertTTeachainLogin(Map param) {
        try {
            loginMapper.insertTTeachainLogin(param);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("insertTTeachainLogin 保存用户信息出错：" + e.getMessage());
            return false;
        }
    }

    public TTeachainLogin getLoginInfoByUuid(String uuid) {
        try {
            return loginMapper.getLoginInfoByUuid(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getLoginInfoByUuid 未找到该用户：" + e.getMessage());
            return null;
        }
    }

    public Boolean checkPhone(String phone) {
        try {
            Integer phoneCount = loginMapper.checkPhone(phone);
            return phoneCount > 0;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("checkPhone 查找电话号码出错：" + e.getMessage());
            return false;
        }
    }

    public TTeachainLogin getLoginByPhone(String phone){
        try {
            //根据用户名实例化用户对象
            TTeachainLogin tTeachainLogin = loginMapper.getLoginByPhone(phone);
            return tTeachainLogin;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("checkPhoneAndPassword 登陆验证出错：" + e.getMessage());
            return null;
        }
    }

    public Boolean updateLoginPhoneByUuid(String uuid, BigInteger phone) {
        try {
            loginMapper.updateLoginPhoneByUuid(uuid, phone);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateLoginPhoneByUuid 更新用户表手机号出错：" + e.getMessage());
            return false;
        }
    }

    public Boolean setPasswordByUuid(String password,String uuid){
        try {
            loginMapper.setPasswordByUuid(password,uuid);
            userMapper.setHasLoginPassword(uuid,1);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setPasswordByUuid 修改登录密码出错：" + e.getMessage());
            return false;
        }
    }

    public void setStatusByUuid(String uuid,int status){
        try {
            loginMapper.setStatusByUuid(uuid,status);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("setPasswordByUuid 修改登录密码出错：" + e.getMessage());
        }
    }

}
