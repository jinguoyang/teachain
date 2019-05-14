package com.youzi.teaChain.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletHistoryMapper {

    void delete2History();

    void update2History();

    void insertWH();
}
