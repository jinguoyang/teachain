package com.youzi.teaChain.service;

import com.youzi.teaChain.dao.ShareMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ShareService {
    private Logger log = LoggerFactory.getLogger(ShareService.class);

    @Resource
    private ShareMapper shareMapper;

    public int selectShareUserId(String openId, String unionId){
        try {
            return shareMapper.selectShareUserId(openId, unionId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("selectShareOpenId 查询上级分享OpenId出错：" + e.getMessage());
            return 0;
        }
    }

}
