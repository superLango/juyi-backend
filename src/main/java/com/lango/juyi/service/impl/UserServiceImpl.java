package com.lango.juyi.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lango.juyi.common.ErrorCode;
import com.lango.juyi.exception.BusinessException;
import com.lango.juyi.mapper.UserMapper;
import com.lango.juyi.model.domain.User;
import com.lango.juyi.service.UserService;
import com.lango.juyi.utils.AlgorithmUtils;
import com.lango.juyi.utils.ConstantPropertiesUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lango.juyi.contant.UserConstant.ADMIN_ROLE;
import static com.lango.juyi.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author lango
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    @Resource
    private UserMapper userMapper;
    /**
     * 盐值 混淆密码
     */
    private static final String salt = "kjfcsddkjfdsajfdiusf8743urf";

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return long
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验，参数不能为空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 用户名5到16位（字母开头，允许字母数字下划线组合)
        String userAccountPatten = "^[a-zA-Z]\\w{4,15}$";
        Matcher accountMatcher = Pattern.compile(userAccountPatten).matcher(userAccount);
        if (!accountMatcher.matches()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名5到16位（字母开头，允许字母数字下划线组合)");
        }

        // 密码最少6位，包括至少1个字母，1个数字，1个特殊字符
        String passwordPatten = "^\\S*(?=\\S{6,})(?=\\S*\\d)(?=\\S*[a-zA-Z])(?=\\S*[!.@#$%^&*? ])\\S*$";
        Matcher passwordMatcher = Pattern.compile(passwordPatten).matcher(userPassword);
        if (!passwordMatcher.matches()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码最少6位，包括至少1个字母，1个数字，1个特殊字符");
        }

        // 密码和检验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码输入不一致");
        }

        // 账户不能重复
        // 查询数据库中是否有相同账户的人
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

//        // 星球编号不能重复
//        queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("PlanetCode", planetCode);
//        count = userMapper.selectCount(queryWrapper);
//        if (count > 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
//        }

        // 2. 加密 盐值 + md5 加密
        String encodedPassword = DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());

        // 封装
        User user = new User();
        user.setUsername(userAccount);
        user.setUserAccount(userAccount);
        user.setUserPassword(encodedPassword);
        // 3. 插入数据
        boolean saveResult = this.save(user);

        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误，请联系管理员");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      request请求
     * @return User 返回脱敏后的安全用户
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验参数是否为空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能为空");
        }
        // 用户名5到16位（字母开头，允许字母数字下划线组合)
        String userAccountPatten = "^[a-zA-Z]\\w{4,15}$";
        Matcher accountMatcher = Pattern.compile(userAccountPatten).matcher(userAccount);
        if (!accountMatcher.matches()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名5到16位（字母开头，允许字母数字下划线组合)");
        }

        // 密码最少6位，包括至少1个字母，1个数字，1个特殊字符
        String passwordPatten = "^\\S*(?=\\S{6,})(?=\\S*\\d)(?=\\S*[a-zA-Z])(?=\\S*[!.@#$%^&*? ])\\S*$";
        Matcher passwordMatcher = Pattern.compile(passwordPatten).matcher(userPassword);
        if (!passwordMatcher.matches()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码最少6位，包括至少1个字母，1个数字，1个特殊字符");
        }

        // 2. 加密
        String encodedPassword = DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encodedPassword);
        // 只查询一条数据
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误，请重新输入");
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        // 将脱敏后的用户信息记录到 session 中
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        // 返回安全用户
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser 原始用户
     * @return User 脱敏后安全用户
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        // 创建一个新的用户实例，将安全的字段存储在新的用户实例中
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setProfile(originUser.getProfile());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setTags(originUser.getTags());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        // 返回安全用户
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request request请求
     * @return int 结果
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户(内存过滤)
     *
     * @param tagNameList 用户要查询的标签集合
     * @return List<User>
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        // 判断是否为空
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 内存查询
        // 1. 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2. 在内存中判断是否包含要求的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            // 使用 gson 反序列化 将 json 转换为 java 对象
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            // java 8 新特性 降低代码复杂度 消除没有意义的分支 代替 if 判断是否为空
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    @Override
    public int updateUser(User user, User loginUser) {
        Long userId = user.getId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        String email = user.getEmail();
//        String userEmailPatten = "^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$";
//        Matcher emailMatcher = Pattern.compile(userEmailPatten).matcher(email);
//        if (!emailMatcher.matches()) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入合法的邮箱");
//        }
//
//        String phone = user.getPhone();
//        String userPhonePatten = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";
//        Matcher phoneMatcher = Pattern.compile(userPhonePatten).matcher(phone);
//        if (!phoneMatcher.matches()) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入合法的手机号码");
//        }
        // todo 补充校验，如果用户没有传任何要更新的值，就直接报错，不用执行更新语句
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
        if (!(isAdmin(loginUser) || userId.equals(loginUser.getId()))) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    /**
     * 获取当前登录用户信息
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 鉴权 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 匹配用户
     *
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或者用户自己
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 原本数据的 userId 列表
        List<Long> userIdList = topUserPairList
                .stream()
                .map(pair -> pair.getKey().getId())
                .collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1, User2, User3
        // 1 => User1, 2 => User2, 3 => User3
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * 根据标签搜索用户(SQL 查询版)
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        // 判断是否为空
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // SQL 查询
        // 创建查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接 and 查询
        // like '%Java%' and like '%Python%'
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

//    @Value("${tencent.cos.file.region}")
//    private String region;
//    @Value("${tencent.cos.file.secretid}")
//    private String secretid;
//    @Value("${tencent.cos.file.secretkey}")
//    private String secretkey;
//    @Value("${tencent.cos.file.bucketname}")
//    private String bucketname;

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @Override
    public String upload(@RequestParam(value = "file") MultipartFile file) {
        // 1 传入获取到的临时密钥 (tmpSecretId, tmpSecretKey, sessionToken)
        String tmpSecretId = ConstantPropertiesUtil.ACCESS_KEY_ID;
        String tmpSecretKey = ConstantPropertiesUtil.ACCESS_KEY_SECRET;
        String bucketName = ConstantPropertiesUtil.BUCKET_NAME;
        String endpoint = ConstantPropertiesUtil.END_POINT;
        String sessionToken = "TOKEN";
        BasicSessionCredentials cred = new BasicSessionCredentials(tmpSecretId, tmpSecretKey, sessionToken);
        // 2 设置 bucket 的地域
        // clientConfig 中包含了设置 region, https(默认 http),
        // 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分
        Region region = new Region(endpoint); //COS_REGION 参数：配置成存储桶 bucket 的实际地域，例如 ap-beijing，更多 COS 地域的简称请参见 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(region);
        // 3 生成 cos 客户端
        COSClient cosClient = new COSClient(cred, clientConfig);
        try {
            // 指定要上传的文件
            InputStream inputStream = file.getInputStream();
            // 指定文件将要存放的存储桶
            // 指定文件上传到 COS 上的路径，即对象键。例如对象键为folder/picture.jpg，则表示将文件 picture.jpg 上传到 folder 路径下
            //上传同名的文件，后面会覆盖掉前面的，需要添加uuid让文件名不重复，并且为了美观可以将-去掉
            String key = UUID.randomUUID().toString().replaceAll("-", "") +
                    file.getOriginalFilename();
            //对文件可以进行分组处理，比如当前2023/4/2，通过刚刚添加的依赖
            /**
             *  日期工具栏依赖
             *     <dependency>
             *         <groupId>joda-time</groupId>
             *         <artifactId>joda-time</artifactId>
             *     </dependency>
             */
            String dateUrl = new DateTime().toString("yyyy-MM-dd");
            key = dateUrl + "/" + key;

            ObjectMetadata objectMetadata = new ObjectMetadata();

            PutObjectRequest putObjectRequest = new PutObjectRequest
                    (bucketName, key, inputStream, objectMetadata);

            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            System.out.println(JSON.toJSONString(putObjectResult));
            //返回上传文件的路径
            //https://yu-1317492111.cos.ap-beijing.myqcloud.com/2.jpg
            return "https://" + bucketName + "." + "cos" + "." + endpoint + ".myqcloud.com" + "/" + key;
        } catch (Exception clientException) {
            clientException.printStackTrace();
            return null;
        }
    }

    @Override
    public List<String> getTagsUser(Long id) {
        User user = this.getById(id);
        String tags = user.getTags();
        Gson gson = new Gson();
        return gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
    }

    @Override
    public void updateTagsUser(List<String> tags, Long id) {
        User user = new User();
        Gson gson = new Gson();
        String userTags = gson.toJson(tags);
        user.setId(id);
        user.setTags(userTags);
        this.updateById(user);
    }
}




