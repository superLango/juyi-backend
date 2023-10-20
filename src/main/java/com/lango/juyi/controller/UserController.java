package com.lango.juyi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lango.juyi.common.BaseResponse;
import com.lango.juyi.common.ErrorCode;
import com.lango.juyi.common.ResultUtils;
import com.lango.juyi.exception.BusinessException;
import com.lango.juyi.model.domain.User;
import com.lango.juyi.model.request.UserLoginRequest;
import com.lango.juyi.model.request.UserRegisterRequest;
import com.lango.juyi.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lango.juyi.contant.UserConstant.USER_LOGIN_STATE;

/**
 * @author lango
 * @version 1.0
 * 用户接口
 */
@RestController
@RequestMapping("/user")
// 跨域注解
//@CrossOrigin(origins = {"http://localhost:3000"},allowCredentials = "true")
@Slf4j
@Api(tags = "用户管理模块")
public class UserController {

    @Resource
    private UserService userService;

    // redis 操作类
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求参数
     * @return BaseResponse<Long>
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userRegisterRequest", value = "用户注册请求参数")
    })
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不全");
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求参数
     * @param request          request请求
     * @return BaseResponse<User>
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userLoginRequest", value = "用户登录请求参数"),
            @ApiImplicitParam(name = "request", value = "request请求")
    })
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码为空");
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request request请求
     * @return BaseResponse<Integer>
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户注销")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "request请求")
    })
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);

    }

    /**
     * 返回当前用户状态
     *
     * @param request request请求
     * @return BaseResponse<User>
     */
    @GetMapping("/current")
    @ApiOperation(value = "当前用户状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "request请求")
    })
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        // 根据 session 获得当前对象
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 获取当前对象 id，查询数据库获取新数据
        long userId = currentUser.getId();
        // todo 校验用户是否合法
        User user = userService.getById(userId);
        System.out.println(user);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 通过用户名搜索用户
     *
     * @param username 用户名
     * @param request  request 请求
     * @return BaseResponse<List < User>>
     */
    @GetMapping("/search")
    @ApiOperation(value = "通过用户名搜索用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名"),
            @ApiImplicitParam(name = "request", value = "request 请求")
    })
    public BaseResponse<List<User>> searchUsersByUserName(String username, HttpServletRequest request) {
        // 校验用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 判断当前用户是否为管理员
//        if (!userService.isAdmin(request)) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 模糊查询
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        // 处理查询结果，将每个用户进行脱敏
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    /**
     * 通过标签搜索用户
     *
     * @param tagNameList 选择的标签集合
     * @return BaseResponse<List < User>>
     */
    @GetMapping("/search/tags")
    @ApiOperation(value = "通过标签搜索用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tagNameList", value = "标签集合")
    })
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList) {
        // 判断标签集合是否为空
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 主页查询
     *
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("juyi:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存，直接读缓存
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
        // 无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        // 写缓存
        try {
            valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(userPage);
    }

    /**
     * 更新用户
     *
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 1. 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/update/tags")
    public BaseResponse<String> updateTagsUser(@RequestBody List<String> tags, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        userService.updateTagsUser(tags,loginUser.getId());
        return ResultUtils.success("ok");
    }

    @GetMapping("/tags")
    public BaseResponse<List<String>> getTagsUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        List<String> userTags =userService.getTagsUser(loginUser.getId());
        return ResultUtils.success(userTags);
    }

    /**
     * 根据 id 删除
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 获取最匹配的用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, loginUser));
    }


    @PostMapping("/upload")
    public BaseResponse<String> upload(MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "请登录");
        }
        String avatarUrl = userService.upload(file);
        User user = new User();
        user.setId(loginUser.getId());
        user.setAvatarUrl(avatarUrl);
        userService.updateUser(user, loginUser);
        return ResultUtils.success(avatarUrl);
    }
}
