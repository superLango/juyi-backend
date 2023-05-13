package com.lango.juyi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lango.juyi.common.BaseResponse;
import com.lango.juyi.common.ErrorCode;
import com.lango.juyi.common.ResultUtils;
import com.lango.juyi.exception.BusinessException;
import com.lango.juyi.model.domain.Team;
import com.lango.juyi.model.domain.User;
import com.lango.juyi.model.dto.TeamQuery;
import com.lango.juyi.model.request.TeamAddRequest;
import com.lango.juyi.model.request.TeamJoinRequest;
import com.lango.juyi.model.request.TeamUpdateRequest;
import com.lango.juyi.model.vo.TeamUserVO;
import com.lango.juyi.service.TeamService;
import com.lango.juyi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lango
 * @version 1.0
 * 队伍接口
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
@Slf4j
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    /**
     * 创建队伍
     *
     * @param teamAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    /**
     * 删
     *
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody int id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 修改队伍信息
     *
     * @param teamUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 查单个数据
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    /**
     * 查列表
     *
     * @param teamQuery
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery,HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,isAdmin);
        return ResultUtils.success(teamList);
    }

    /**
     * 分页查询
     *
     * @param teamQuery
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request){
        if (teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest,loginUser);
        return ResultUtils.success(result);
    }
}
