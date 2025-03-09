package com.morning.rentasnowhero.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.morning.rentasnowhero.common.ErrorCode;
import com.morning.rentasnowhero.constant.CommonConstant;
import com.morning.rentasnowhero.exception.ThrowUtils;
import com.morning.rentasnowhero.mapper.EquipmentMapper;
import com.morning.rentasnowhero.model.dto.equipment.EquipmentQueryRequest;
import com.morning.rentasnowhero.model.entity.Equipment;
import com.morning.rentasnowhero.model.entity.EquipmentFavour;
import com.morning.rentasnowhero.model.entity.EquipmentThumb;
import com.morning.rentasnowhero.model.entity.User;
import com.morning.rentasnowhero.model.vo.EquipmentVO;
import com.morning.rentasnowhero.model.vo.UserVO;
import com.morning.rentasnowhero.service.EquipmentService;
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
 * 装备服务实现
 */
@Service
@Slf4j
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param equipment
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validEquipment(Equipment equipment, boolean add) {
        ThrowUtils.throwIf(equipment == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = equipment.getTitle();
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
     * @param equipmentQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Equipment> getQueryWrapper(EquipmentQueryRequest equipmentQueryRequest) {
        QueryWrapper<Equipment> queryWrapper = new QueryWrapper<>();
        if (equipmentQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = equipmentQueryRequest.getId();
        Long notId = equipmentQueryRequest.getNotId();
        String title = equipmentQueryRequest.getTitle();
        String content = equipmentQueryRequest.getContent();
        String searchText = equipmentQueryRequest.getSearchText();
        String sortField = equipmentQueryRequest.getSortField();
        String sortOrder = equipmentQueryRequest.getSortOrder();
        List<String> tagList = equipmentQueryRequest.getTags();
        Long userId = equipmentQueryRequest.getUserId();
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
     * 获取装备封装
     *
     * @param equipment
     * @param request
     * @return
     */
    @Override
    public EquipmentVO getEquipmentVO(Equipment equipment, HttpServletRequest request) {
        // 对象转封装类
        EquipmentVO equipmentVO = EquipmentVO.objToVo(equipment);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = equipment.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        equipmentVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long equipmentId = equipment.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<EquipmentThumb> equipmentThumbQueryWrapper = new QueryWrapper<>();
            equipmentThumbQueryWrapper.in("equipmentId", equipmentId);
            equipmentThumbQueryWrapper.eq("userId", loginUser.getId());
            EquipmentThumb equipmentThumb = equipmentThumbMapper.selectOne(equipmentThumbQueryWrapper);
            equipmentVO.setHasThumb(equipmentThumb != null);
            // 获取收藏
            QueryWrapper<EquipmentFavour> equipmentFavourQueryWrapper = new QueryWrapper<>();
            equipmentFavourQueryWrapper.in("equipmentId", equipmentId);
            equipmentFavourQueryWrapper.eq("userId", loginUser.getId());
            EquipmentFavour equipmentFavour = equipmentFavourMapper.selectOne(equipmentFavourQueryWrapper);
            equipmentVO.setHasFavour(equipmentFavour != null);
        }
        // endregion

        return equipmentVO;
    }

    /**
     * 分页获取装备封装
     *
     * @param equipmentPage
     * @param request
     * @return
     */
    @Override
    public Page<EquipmentVO> getEquipmentVOPage(Page<Equipment> equipmentPage, HttpServletRequest request) {
        List<Equipment> equipmentList = equipmentPage.getRecords();
        Page<EquipmentVO> equipmentVOPage = new Page<>(equipmentPage.getCurrent(), equipmentPage.getSize(), equipmentPage.getTotal());
        if (CollUtil.isEmpty(equipmentList)) {
            return equipmentVOPage;
        }
        // 对象列表 => 封装对象列表
        List<EquipmentVO> equipmentVOList = equipmentList.stream().map(equipment -> {
            return EquipmentVO.objToVo(equipment);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = equipmentList.stream().map(Equipment::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> equipmentIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> equipmentIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> equipmentIdSet = equipmentList.stream().map(Equipment::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<EquipmentThumb> equipmentThumbQueryWrapper = new QueryWrapper<>();
            equipmentThumbQueryWrapper.in("equipmentId", equipmentIdSet);
            equipmentThumbQueryWrapper.eq("userId", loginUser.getId());
            List<EquipmentThumb> equipmentEquipmentThumbList = equipmentThumbMapper.selectList(equipmentThumbQueryWrapper);
            equipmentEquipmentThumbList.forEach(equipmentEquipmentThumb -> equipmentIdHasThumbMap.put(equipmentEquipmentThumb.getEquipmentId(), true));
            // 获取收藏
            QueryWrapper<EquipmentFavour> equipmentFavourQueryWrapper = new QueryWrapper<>();
            equipmentFavourQueryWrapper.in("equipmentId", equipmentIdSet);
            equipmentFavourQueryWrapper.eq("userId", loginUser.getId());
            List<EquipmentFavour> equipmentFavourList = equipmentFavourMapper.selectList(equipmentFavourQueryWrapper);
            equipmentFavourList.forEach(equipmentFavour -> equipmentIdHasFavourMap.put(equipmentFavour.getEquipmentId(), true));
        }
        // 填充信息
        equipmentVOList.forEach(equipmentVO -> {
            Long userId = equipmentVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            equipmentVO.setUser(userService.getUserVO(user));
            equipmentVO.setHasThumb(equipmentIdHasThumbMap.getOrDefault(equipmentVO.getId(), false));
            equipmentVO.setHasFavour(equipmentIdHasFavourMap.getOrDefault(equipmentVO.getId(), false));
        });
        // endregion

        equipmentVOPage.setRecords(equipmentVOList);
        return equipmentVOPage;
    }

}
