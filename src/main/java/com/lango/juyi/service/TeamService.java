package com.lango.juyi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lango.juyi.model.domain.Team;
import com.lango.juyi.model.domain.User;

/**
* @author 26449
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-05-11 15:32:59
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);
}
