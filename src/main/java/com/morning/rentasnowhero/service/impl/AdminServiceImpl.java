package com.morning.rentasnowhero.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.morning.rentasnowhero.common.ErrorCode;
import com.morning.rentasnowhero.constant.CommonConstant;
import com.morning.rentasnowhero.exception.ThrowUtils;
import com.morning.rentasnowhero.mapper.AdminMapper;
import com.morning.rentasnowhero.model.dto.admin.AdminQueryRequest;
import com.morning.rentasnowhero.model.entity.Admin;
import com.morning.rentasnowhero.model.entity.AdminFavour;
import com.morning.rentasnowhero.model.entity.AdminThumb;
import com.morning.rentasnowhero.model.entity.User;
import com.morning.rentasnowhero.model.vo.AdminVO;
import com.morning.rentasnowhero.model.vo.UserVO;
import com.morning.rentasnowhero.service.AdminService;
import com.morning.rentasnowhero.service.UserService;
import com.morning.rentasnowhero.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理元服务实现
 */
@Service
@Slf4j
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param admin
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validAdmin(Admin admin, boolean add) {
        ThrowUtils.throwIf(admin == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = admin.getTitle();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }
    }

    /**
     * 获取查询条件
     *
     * @param adminQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Admin> getQueryWrapper(AdminQueryRequest adminQueryRequest) {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        if (adminQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = adminQueryRequest.getId();
        Long notId = adminQueryRequest.getNotId();
        String title = adminQueryRequest.getTitle();
        String content = adminQueryRequest.getContent();
        String searchText = adminQueryRequest.getSearchText();
        String sortField = adminQueryRequest.getSortField();
        String sortOrder = adminQueryRequest.getSortOrder();
        List<String> tagList = adminQueryRequest.getTags();
        Long userId = adminQueryRequest.getUserId();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取管理元封装
     *
     * @param admin
     * @param request
     * @return
     */
    @Override
    public AdminVO getAdminVO(Admin admin, HttpServletRequest request) {
        // 对象转封装类
        AdminVO adminVO = AdminVO.objToVo(admin);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = admin.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        adminVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long adminId = admin.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<AdminThumb> adminThumbQueryWrapper = new QueryWrapper<>();
            adminThumbQueryWrapper.in("adminId", adminId);
            adminThumbQueryWrapper.eq("userId", loginUser.getId());
            AdminThumb adminThumb = adminThumbMapper.selectOne(adminThumbQueryWrapper);
            adminVO.setHasThumb(adminThumb != null);
            // 获取收藏
            QueryWrapper<AdminFavour> adminFavourQueryWrapper = new QueryWrapper<>();
            adminFavourQueryWrapper.in("adminId", adminId);
            adminFavourQueryWrapper.eq("userId", loginUser.getId());
            AdminFavour adminFavour = adminFavourMapper.selectOne(adminFavourQueryWrapper);
            adminVO.setHasFavour(adminFavour != null);
        }
        // endregion

        return adminVO;
    }

    /**
     * 分页获取管理元封装
     *
     * @param adminPage
     * @param request
     * @return
     */
    @Override
    public Page<AdminVO> getAdminVOPage(Page<Admin> adminPage, HttpServletRequest request) {
        List<Admin> adminList = adminPage.getRecords();
        Page<AdminVO> adminVOPage = new Page<>(adminPage.getCurrent(), adminPage.getSize(), adminPage.getTotal());
        if (CollUtil.isEmpty(adminList)) {
            return adminVOPage;
        }
        // 对象列表 => 封装对象列表
        List<AdminVO> adminVOList = adminList.stream().map(admin -> {
            return AdminVO.objToVo(admin);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = adminList.stream().map(Admin::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> adminIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> adminIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> adminIdSet = adminList.stream().map(Admin::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<AdminThumb> adminThumbQueryWrapper = new QueryWrapper<>();
            adminThumbQueryWrapper.in("adminId", adminIdSet);
            adminThumbQueryWrapper.eq("userId", loginUser.getId());
            List<AdminThumb> adminAdminThumbList = adminThumbMapper.selectList(adminThumbQueryWrapper);
            adminAdminThumbList.forEach(adminAdminThumb -> adminIdHasThumbMap.put(adminAdminThumb.getAdminId(), true));
            // 获取收藏
            QueryWrapper<AdminFavour> adminFavourQueryWrapper = new QueryWrapper<>();
            adminFavourQueryWrapper.in("adminId", adminIdSet);
            adminFavourQueryWrapper.eq("userId", loginUser.getId());
            List<AdminFavour> adminFavourList = adminFavourMapper.selectList(adminFavourQueryWrapper);
            adminFavourList.forEach(adminFavour -> adminIdHasFavourMap.put(adminFavour.getAdminId(), true));
        }
        // 填充信息
        adminVOList.forEach(adminVO -> {
            Long userId = adminVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            adminVO.setUser(userService.getUserVO(user));
            adminVO.setHasThumb(adminIdHasThumbMap.getOrDefault(adminVO.getId(), false));
            adminVO.setHasFavour(adminIdHasFavourMap.getOrDefault(adminVO.getId(), false));
        });
        // endregion

        adminVOPage.setRecords(adminVOList);
        return adminVOPage;
    }

}
