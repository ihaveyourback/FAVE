package com.yhkim.fave.services;

import com.yhkim.fave.entities.FaveInfoEntity;
import com.yhkim.fave.mappers.FaveInfoMapper;
import com.yhkim.fave.vos.FaveBoardVo;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FaveService {

    private final FaveInfoMapper faveInfoMapper;

    @Autowired
    public FaveService(FaveInfoMapper faveInfoMapper) {
        this.faveInfoMapper = faveInfoMapper;
    }

    public Pair<FaveBoardVo, FaveInfoEntity[]> selectFaveInfo(int page) {
        page = Math.max(page, 1);
        int totalCount = this.faveInfoMapper.selectFaveInfoCount();
        FaveBoardVo faveVo = new FaveBoardVo(page, totalCount);
        FaveInfoEntity[] faveInfo = this.faveInfoMapper.selectFaveInfo(faveVo.countPerPage, faveVo.offsetCount);
        return Pair.of(faveVo, faveInfo);
    }

    public FaveInfoEntity selectFaveInfoById(int index) {
        if (index < 0) {
            return null;
        }
        return this.faveInfoMapper.selectFaveInfoById(index);
    }

    public boolean updateFaveInfo(FaveInfoEntity faveInfo) {
        if (faveInfo == null) {
            return false;
        }
        faveInfo.setView(faveInfo.getView() + 1);
        return this.faveInfoMapper.updateFaveInfoView(faveInfo) > 0;
    }


    public Pair<FaveBoardVo, FaveInfoEntity[]> searchFaveInfo(int page, String filter, String keyword) {
        page = Math.max(1, page);
        if (filter == null || (!filter.equals("all") && !filter.equals("title") && !filter.equals("nickname"))) {
            filter = "all";
        }
        if (keyword == null) {
            keyword = "";
        }
        int totalCount = this.faveInfoMapper.selectFaveInfoCountBySearch(filter, keyword);
        FaveBoardVo pageInfo = new FaveBoardVo(page, totalCount);
        List<FaveInfoEntity> results = this.faveInfoMapper.searchFaveInfo(filter, keyword, pageInfo.countPerPage, pageInfo.offsetCount);
        return Pair.of(pageInfo, results.toArray(new FaveInfoEntity[0]));
    }
}
