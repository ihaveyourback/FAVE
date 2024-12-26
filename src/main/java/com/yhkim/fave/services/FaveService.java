package com.yhkim.fave.services;

import com.yhkim.fave.entities.FaveInfoEntity;
import com.yhkim.fave.mappers.FaveInfoMapper;
import com.yhkim.fave.vos.FaveBoardVo;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
