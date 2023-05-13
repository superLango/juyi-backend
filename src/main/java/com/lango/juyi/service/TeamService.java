package com.lango.juyi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lango.juyi.model.domain.Team;
import com.lango.juyi.model.domain.User;
import com.lango.juyi.model.dto.TeamQuery;
import com.lango.juyi.model.request.TeamJoinRequest;
import com.lango.juyi.model.request.TeamUpdateRequest;
import com.lango.juyi.model.vo.TeamUserVO;

import java.util.List;

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

    /**
     * 搜索队伍
     *
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 更新队伍
     *
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);
}
