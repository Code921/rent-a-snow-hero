package com.morning.rentasnowhero.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.morning.rentasnowhero.common.ErrorCode;
import com.morning.rentasnowhero.constant.CommonConstant;
import com.morning.rentasnowhero.exception.ThrowUtils;
import com.morning.rentasnowhero.mapper.EquipmentRentalCalendarMapper;
import com.morning.rentasnowhero.model.dto.equipmentRentalCalendar.EquipmentRentalCalendarQueryRequest;
import com.morning.rentasnowhero.model.entity.EquipmentRentalCalendar;
import com.morning.rentasnowhero.model.entity.EquipmentRentalCalendarFavour;
import com.morning.rentasnowhero.model.entity.EquipmentRentalCalendarThumb;
import com.morning.rentasnowhero.model.entity.User;
import com.morning.rentasnowhero.model.vo.EquipmentRentalCalendarVO;
import com.morning.rentasnowhero.model.vo.UserVO;
import com.morning.rentasnowhero.service.EquipmentRentalCalendarService;
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
 * 装备出租日历表服务实现
 */
@Service
@Slf4j
public class EquipmentRentalCalendarServiceImpl extends ServiceImpl<EquipmentRentalCalendarMapper, EquipmentRentalCalendar> implements EquipmentRentalCalendarService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param equipmentRentalCalendar
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validEquipmentRentalCalendar(EquipmentRentalCalendar equipmentRentalCalendar, boolean add) {
        ThrowUtils.throwIf(equipmentRentalCalendar == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = equipmentRentalCalendar.getTitle();
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
     * @param equipmentRentalCalendarQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<EquipmentRentalCalendar> getQueryWrapper(EquipmentRentalCalendarQueryRequest equipmentRentalCalendarQueryRequest) {
        QueryWrapper<EquipmentRentalCalendar> queryWrapper = new QueryWrapper<>();
        if (equipmentRentalCalendarQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = equipmentRentalCalendarQueryRequest.getId();
        Long notId = equipmentRentalCalendarQueryRequest.getNotId();
        String title = equipmentRentalCalendarQueryRequest.getTitle();
        String content = equipmentRentalCalendarQueryRequest.getContent();
        String searchText = equipmentRentalCalendarQueryRequest.getSearchText();
        String sortField = equipmentRentalCalendarQueryRequest.getSortField();
        String sortOrder = equipmentRentalCalendarQueryRequest.getSortOrder();
        List<String> tagList = equipmentRentalCalendarQueryRequest.getTags();
        Long userId = equipmentRentalCalendarQueryRequest.getUserId();
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
     * 获取装备出租日历表封装
     *
     * @param equipmentRentalCalendar
     * @param request
     * @return
     */
    @Override
    public EquipmentRentalCalendarVO getEquipmentRentalCalendarVO(EquipmentRentalCalendar equipmentRentalCalendar, HttpServletRequest request) {
        // 对象转封装类
        EquipmentRentalCalendarVO equipmentRentalCalendarVO = EquipmentRentalCalendarVO.objToVo(equipmentRentalCalendar);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = equipmentRentalCalendar.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        equipmentRentalCalendarVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long equipmentRentalCalendarId = equipmentRentalCalendar.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<EquipmentRentalCalendarThumb> equipmentRentalCalendarThumbQueryWrapper = new QueryWrapper<>();
            equipmentRentalCalendarThumbQueryWrapper.in("equipmentRentalCalendarId", equipmentRentalCalendarId);
            equipmentRentalCalendarThumbQueryWrapper.eq("userId", loginUser.getId());
            EquipmentRentalCalendarThumb equipmentRentalCalendarThumb = equipmentRentalCalendarThumbMapper.selectOne(equipmentRentalCalendarThumbQueryWrapper);
            equipmentRentalCalendarVO.setHasThumb(equipmentRentalCalendarThumb != null);
            // 获取收藏
            QueryWrapper<EquipmentRentalCalendarFavour> equipmentRentalCalendarFavourQueryWrapper = new QueryWrapper<>();
            equipmentRentalCalendarFavourQueryWrapper.in("equipmentRentalCalendarId", equipmentRentalCalendarId);
            equipmentRentalCalendarFavourQueryWrapper.eq("userId", loginUser.getId());
            EquipmentRentalCalendarFavour equipmentRentalCalendarFavour = equipmentRentalCalendarFavourMapper.selectOne(equipmentRentalCalendarFavourQueryWrapper);
            equipmentRentalCalendarVO.setHasFavour(equipmentRentalCalendarFavour != null);
        }
        // endregion

        return equipmentRentalCalendarVO;
    }

    /**
     * 分页获取装备出租日历表封装
     *
     * @param equipmentRentalCalendarPage
     * @param request
     * @return
     */
    @Override
    public Page<EquipmentRentalCalendarVO> getEquipmentRentalCalendarVOPage(Page<EquipmentRentalCalendar> equipmentRentalCalendarPage, HttpServletRequest request) {
        List<EquipmentRentalCalendar> equipmentRentalCalendarList = equipmentRentalCalendarPage.getRecords();
        Page<EquipmentRentalCalendarVO> equipmentRentalCalendarVOPage = new Page<>(equipmentRentalCalendarPage.getCurrent(), equipmentRentalCalendarPage.getSize(), equipmentRentalCalendarPage.getTotal());
        if (CollUtil.isEmpty(equipmentRentalCalendarList)) {
            return equipmentRentalCalendarVOPage;
        }
        // 对象列表 => 封装对象列表
        List<EquipmentRentalCalendarVO> equipmentRentalCalendarVOList = equipmentRentalCalendarList.stream().map(equipmentRentalCalendar -> {
            return EquipmentRentalCalendarVO.objToVo(equipmentRentalCalendar);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = equipmentRentalCalendarList.stream().map(EquipmentRentalCalendar::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> equipmentRentalCalendarIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> equipmentRentalCalendarIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> equipmentRentalCalendarIdSet = equipmentRentalCalendarList.stream().map(EquipmentRentalCalendar::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<EquipmentRentalCalendarThumb> equipmentRentalCalendarThumbQueryWrapper = new QueryWrapper<>();
            equipmentRentalCalendarThumbQueryWrapper.in("equipmentRentalCalendarId", equipmentRentalCalendarIdSet);
            equipmentRentalCalendarThumbQueryWrapper.eq("userId", loginUser.getId());
            List<EquipmentRentalCalendarThumb> equipmentRentalCalendarEquipmentRentalCalendarThumbList = equipmentRentalCalendarThumbMapper.selectList(equipmentRentalCalendarThumbQueryWrapper);
            equipmentRentalCalendarEquipmentRentalCalendarThumbList.forEach(equipmentRentalCalendarEquipmentRentalCalendarThumb -> equipmentRentalCalendarIdHasThumbMap.put(equipmentRentalCalendarEquipmentRentalCalendarThumb.getEquipmentRentalCalendarId(), true));
            // 获取收藏
            QueryWrapper<EquipmentRentalCalendarFavour> equipmentRentalCalendarFavourQueryWrapper = new QueryWrapper<>();
            equipmentRentalCalendarFavourQueryWrapper.in("equipmentRentalCalendarId", equipmentRentalCalendarIdSet);
            equipmentRentalCalendarFavourQueryWrapper.eq("userId", loginUser.getId());
            List<EquipmentRentalCalendarFavour> equipmentRentalCalendarFavourList = equipmentRentalCalendarFavourMapper.selectList(equipmentRentalCalendarFavourQueryWrapper);
            equipmentRentalCalendarFavourList.forEach(equipmentRentalCalendarFavour -> equipmentRentalCalendarIdHasFavourMap.put(equipmentRentalCalendarFavour.getEquipmentRentalCalendarId(), true));
        }
        // 填充信息
        equipmentRentalCalendarVOList.forEach(equipmentRentalCalendarVO -> {
            Long userId = equipmentRentalCalendarVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            equipmentRentalCalendarVO.setUser(userService.getUserVO(user));
            equipmentRentalCalendarVO.setHasThumb(equipmentRentalCalendarIdHasThumbMap.getOrDefault(equipmentRentalCalendarVO.getId(), false));
            equipmentRentalCalendarVO.setHasFavour(equipmentRentalCalendarIdHasFavourMap.getOrDefault(equipmentRentalCalendarVO.getId(), false));
        });
        // endregion

        equipmentRentalCalendarVOPage.setRecords(equipmentRentalCalendarVOList);
        return equipmentRentalCalendarVOPage;
    }

}
