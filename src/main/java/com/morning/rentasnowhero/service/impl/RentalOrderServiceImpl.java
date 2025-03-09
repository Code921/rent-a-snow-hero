package com.morning.rentasnowhero.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.morning.rentasnowhero.common.ErrorCode;
import com.morning.rentasnowhero.constant.CommonConstant;
import com.morning.rentasnowhero.exception.ThrowUtils;
import com.morning.rentasnowhero.mapper.RentalOrderMapper;
import com.morning.rentasnowhero.model.dto.rentalOrder.RentalOrderQueryRequest;
import com.morning.rentasnowhero.model.entity.RentalOrder;
import com.morning.rentasnowhero.model.entity.RentalOrderFavour;
import com.morning.rentasnowhero.model.entity.RentalOrderThumb;
import com.morning.rentasnowhero.model.entity.User;
import com.morning.rentasnowhero.model.vo.RentalOrderVO;
import com.morning.rentasnowhero.model.vo.UserVO;
import com.morning.rentasnowhero.service.RentalOrderService;
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
 * 出租订单服务实现
 */
@Service
@Slf4j
public class RentalOrderServiceImpl extends ServiceImpl<RentalOrderMapper, RentalOrder> implements RentalOrderService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param rentalOrder
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validRentalOrder(RentalOrder rentalOrder, boolean add) {
        ThrowUtils.throwIf(rentalOrder == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = rentalOrder.getTitle();
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
     * @param rentalOrderQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<RentalOrder> getQueryWrapper(RentalOrderQueryRequest rentalOrderQueryRequest) {
        QueryWrapper<RentalOrder> queryWrapper = new QueryWrapper<>();
        if (rentalOrderQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = rentalOrderQueryRequest.getId();
        Long notId = rentalOrderQueryRequest.getNotId();
        String title = rentalOrderQueryRequest.getTitle();
        String content = rentalOrderQueryRequest.getContent();
        String searchText = rentalOrderQueryRequest.getSearchText();
        String sortField = rentalOrderQueryRequest.getSortField();
        String sortOrder = rentalOrderQueryRequest.getSortOrder();
        List<String> tagList = rentalOrderQueryRequest.getTags();
        Long userId = rentalOrderQueryRequest.getUserId();
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
     * 获取出租订单封装
     *
     * @param rentalOrder
     * @param request
     * @return
     */
    @Override
    public RentalOrderVO getRentalOrderVO(RentalOrder rentalOrder, HttpServletRequest request) {
        // 对象转封装类
        RentalOrderVO rentalOrderVO = RentalOrderVO.objToVo(rentalOrder);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = rentalOrder.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        rentalOrderVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long rentalOrderId = rentalOrder.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<RentalOrderThumb> rentalOrderThumbQueryWrapper = new QueryWrapper<>();
            rentalOrderThumbQueryWrapper.in("rentalOrderId", rentalOrderId);
            rentalOrderThumbQueryWrapper.eq("userId", loginUser.getId());
            RentalOrderThumb rentalOrderThumb = rentalOrderThumbMapper.selectOne(rentalOrderThumbQueryWrapper);
            rentalOrderVO.setHasThumb(rentalOrderThumb != null);
            // 获取收藏
            QueryWrapper<RentalOrderFavour> rentalOrderFavourQueryWrapper = new QueryWrapper<>();
            rentalOrderFavourQueryWrapper.in("rentalOrderId", rentalOrderId);
            rentalOrderFavourQueryWrapper.eq("userId", loginUser.getId());
            RentalOrderFavour rentalOrderFavour = rentalOrderFavourMapper.selectOne(rentalOrderFavourQueryWrapper);
            rentalOrderVO.setHasFavour(rentalOrderFavour != null);
        }
        // endregion

        return rentalOrderVO;
    }

    /**
     * 分页获取出租订单封装
     *
     * @param rentalOrderPage
     * @param request
     * @return
     */
    @Override
    public Page<RentalOrderVO> getRentalOrderVOPage(Page<RentalOrder> rentalOrderPage, HttpServletRequest request) {
        List<RentalOrder> rentalOrderList = rentalOrderPage.getRecords();
        Page<RentalOrderVO> rentalOrderVOPage = new Page<>(rentalOrderPage.getCurrent(), rentalOrderPage.getSize(), rentalOrderPage.getTotal());
        if (CollUtil.isEmpty(rentalOrderList)) {
            return rentalOrderVOPage;
        }
        // 对象列表 => 封装对象列表
        List<RentalOrderVO> rentalOrderVOList = rentalOrderList.stream().map(rentalOrder -> {
            return RentalOrderVO.objToVo(rentalOrder);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = rentalOrderList.stream().map(RentalOrder::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> rentalOrderIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> rentalOrderIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> rentalOrderIdSet = rentalOrderList.stream().map(RentalOrder::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<RentalOrderThumb> rentalOrderThumbQueryWrapper = new QueryWrapper<>();
            rentalOrderThumbQueryWrapper.in("rentalOrderId", rentalOrderIdSet);
            rentalOrderThumbQueryWrapper.eq("userId", loginUser.getId());
            List<RentalOrderThumb> rentalOrderRentalOrderThumbList = rentalOrderThumbMapper.selectList(rentalOrderThumbQueryWrapper);
            rentalOrderRentalOrderThumbList.forEach(rentalOrderRentalOrderThumb -> rentalOrderIdHasThumbMap.put(rentalOrderRentalOrderThumb.getRentalOrderId(), true));
            // 获取收藏
            QueryWrapper<RentalOrderFavour> rentalOrderFavourQueryWrapper = new QueryWrapper<>();
            rentalOrderFavourQueryWrapper.in("rentalOrderId", rentalOrderIdSet);
            rentalOrderFavourQueryWrapper.eq("userId", loginUser.getId());
            List<RentalOrderFavour> rentalOrderFavourList = rentalOrderFavourMapper.selectList(rentalOrderFavourQueryWrapper);
            rentalOrderFavourList.forEach(rentalOrderFavour -> rentalOrderIdHasFavourMap.put(rentalOrderFavour.getRentalOrderId(), true));
        }
        // 填充信息
        rentalOrderVOList.forEach(rentalOrderVO -> {
            Long userId = rentalOrderVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            rentalOrderVO.setUser(userService.getUserVO(user));
            rentalOrderVO.setHasThumb(rentalOrderIdHasThumbMap.getOrDefault(rentalOrderVO.getId(), false));
            rentalOrderVO.setHasFavour(rentalOrderIdHasFavourMap.getOrDefault(rentalOrderVO.getId(), false));
        });
        // endregion

        rentalOrderVOPage.setRecords(rentalOrderVOList);
        return rentalOrderVOPage;
    }

}
