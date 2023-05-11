package com.lango.juyi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lango.juyi.model.domain.UserTeam;
import com.lango.juyi.mapper.UserTeamMapper;
import com.lango.juyi.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author 26449
* @description 针对表【user_team(用户 - 队伍)】的数据库操作Service实现
* @createDate 2023-05-11 15:34:58
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




