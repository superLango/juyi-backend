package com.lango.juyi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lango.juyi.model.domain.Team;
import com.lango.juyi.mapper.TeamMapper;
import com.lango.juyi.service.TeamService;
import org.springframework.stereotype.Service;

/**
* @author 26449
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-05-11 15:32:59
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




