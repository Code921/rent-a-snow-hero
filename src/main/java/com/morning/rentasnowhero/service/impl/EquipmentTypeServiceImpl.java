package com.morning.rentasnowhero.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.morning.rentasnowhero.common.ErrorCode;
import com.morning.rentasnowhero.constant.CommonConstant;
import com.morning.rentasnowhero.exception.ThrowUtils;
import com.morning.rentasnowhero.mapper.EquipmentTypeMapper;
import com.morning.rentasnowhero.model.dto.equipmentType.EquipmentTypeQueryRequest;
import com.morning.rentasnowhero.model.entity.EquipmentType;
import com.morning.rentasnowhero.model.entity.EquipmentTypeFavour;
import com.morning.rentasnowhero.model.entity.EquipmentTypeThumb;
import com.morning.rentasnowhero.model.entity.User;
import com.morning.rentasnowhero.model.vo.EquipmentTypeVO;
import com.morning.rentasnowhero.model.vo.UserVO;
import com.morning.rentasnowhero.service.EquipmentTypeService;
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
 * 装备类型服务实现
 */
@Service
@Slf4j
public class EquipmentTypeServiceImpl extends ServiceImpl<EquipmentTypeMapper, EquipmentType> implements EquipmentTypeService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param equipmentType
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validEquipmentType(EquipmentType equipmentType, boolean add) {
        ThrowUtils.throwIf(equipmentType == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = equipmentType.getTitle();
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
     * @param equipmentTypeQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<EquipmentType> getQueryWrapper(EquipmentTypeQueryRequest equipmentTypeQueryRequest) {
        QueryWrapper<EquipmentType> queryWrapper = new QueryWrapper<>();
        if (equipmentTypeQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = equipmentTypeQueryRequest.getId();
        Long notId = equipmentTypeQueryRequest.getNotId();
        String title = equipmentTypeQueryRequest.getTitle();
        String content = equipmentTypeQueryRequest.getContent();
        String searchText = equipmentTypeQueryRequest.getSearchText();
        String sortField = equipmentTypeQueryRequest.getSortField();
        String sortOrder = equipmentTypeQueryRequest.getSortOrder();
        List<String> tagList = equipmentTypeQueryRequest.getTags();
        Long userId = equipmentTypeQueryRequest.getUserId();
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
     * 获取装备类型封装
     *
     * @param equipmentType
     * @param request
     * @return
     */
    @Override
    public EquipmentTypeVO getEquipmentTypeVO(EquipmentType equipmentType, HttpServletRequest request) {
        // 对象转封装类
        EquipmentTypeVO equipmentTypeVO = EquipmentTypeVO.objToVo(equipmentType);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = equipmentType.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        equipmentTypeVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long equipmentTypeId = equipmentType.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<EquipmentTypeThumb> equipmentTypeThumbQueryWrapper = new QueryWrapper<>();
            equipmentTypeThumbQueryWrapper.in("equipmentTypeId", equipmentTypeId);
            equipmentTypeThumbQueryWrapper.eq("userId", loginUser.getId());
            EquipmentTypeThumb equipmentTypeThumb = equipmentTypeThumbMapper.selectOne(equipmentTypeThumbQueryWrapper);
            equipmentTypeVO.setHasThumb(equipmentTypeThumb != null);
            // 获取收藏
            QueryWrapper<EquipmentTypeFavour> equipmentTypeFavourQueryWrapper = new QueryWrapper<>();
            equipmentTypeFavourQueryWrapper.in("equipmentTypeId", equipmentTypeId);
            equipmentTypeFavourQueryWrapper.eq("userId", loginUser.getId());
            EquipmentTypeFavour equipmentTypeFavour = equipmentTypeFavourMapper.selectOne(equipmentTypeFavourQueryWrapper);
            equipmentTypeVO.setHasFavour(equipmentTypeFavour != null);
        }
        // endregion

        return equipmentTypeVO;
    }

    /**
     * 分页获取装备类型封装
     *
     * @param equipmentTypePage
     * @param request
     * @return
     */
    @Override
    public Page<EquipmentTypeVO> getEquipmentTypeVOPage(Page<EquipmentType> equipmentTypePage, HttpServletRequest request) {
        List<EquipmentType> equipmentTypeList = equipmentTypePage.getRecords();
        Page<EquipmentTypeVO> equipmentTypeVOPage = new Page<>(equipmentTypePage.getCurrent(), equipmentTypePage.getSize(), equipmentTypePage.getTotal());
        if (CollUtil.isEmpty(equipmentTypeList)) {
            return equipmentTypeVOPage;
        }
        // 对象列表 => 封装对象列表
        List<EquipmentTypeVO> equipmentTypeVOList = equipmentTypeList.stream().map(equipmentType -> {
            return EquipmentTypeVO.objToVo(equipmentType);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = equipmentTypeList.stream().map(EquipmentType::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> equipmentTypeIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> equipmentTypeIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> equipmentTypeIdSet = equipmentTypeList.stream().map(EquipmentType::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<EquipmentTypeThumb> equipmentTypeThumbQueryWrapper = new QueryWrapper<>();
            equipmentTypeThumbQueryWrapper.in("equipmentTypeId", equipmentTypeIdSet);
            equipmentTypeThumbQueryWrapper.eq("userId", loginUser.getId());
            List<EquipmentTypeThumb> equipmentTypeEquipmentTypeThumbList = equipmentTypeThumbMapper.selectList(equipmentTypeThumbQueryWrapper);
            equipmentTypeEquipmentTypeThumbList.forEach(equipmentTypeEquipmentTypeThumb -> equipmentTypeIdHasThumbMap.put(equipmentTypeEquipmentTypeThumb.getEquipmentTypeId(), true));
            // 获取收藏
            QueryWrapper<EquipmentTypeFavour> equipmentTypeFavourQueryWrapper = new QueryWrapper<>();
            equipmentTypeFavourQueryWrapper.in("equipmentTypeId", equipmentTypeIdSet);
            equipmentTypeFavourQueryWrapper.eq("userId", loginUser.getId());
            List<EquipmentTypeFavour> equipmentTypeFavourList = equipmentTypeFavourMapper.selectList(equipmentTypeFavourQueryWrapper);
            equipmentTypeFavourList.forEach(equipmentTypeFavour -> equipmentTypeIdHasFavourMap.put(equipmentTypeFavour.getEquipmentTypeId(), true));
        }
        // 填充信息
        equipmentTypeVOList.forEach(equipmentTypeVO -> {
            Long userId = equipmentTypeVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            equipmentTypeVO.setUser(userService.getUserVO(user));
            equipmentTypeVO.setHasThumb(equipmentTypeIdHasThumbMap.getOrDefault(equipmentTypeVO.getId(), false));
            equipmentTypeVO.setHasFavour(equipmentTypeIdHasFavourMap.getOrDefault(equipmentTypeVO.getId(), false));
        });
        // endregion

        equipmentTypeVOPage.setRecords(equipmentTypeVOList);
        return equipmentTypeVOPage;
    }

}
