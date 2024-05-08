package org.nott.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface CommonMapper {

    int UpdateRowByCAS(@Param("table") String tableName, @Param("setKeyPairs") Map<String,Object> setKeyPairs,@Param("valKeyPairs") Map<String,Object> valKeyPairs);
}
