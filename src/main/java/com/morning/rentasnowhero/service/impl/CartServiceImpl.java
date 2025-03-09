package com.morning.rentasnowhero.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.morning.rentasnowhero.common.ErrorCode;
import com.morning.rentasnowhero.constant.CommonConstant;
import com.morning.rentasnowhero.exception.ThrowUtils;
import com.morning.rentasnowhero.mapper.CartMapper;
import com.morning.rentasnowhero.model.dto.cart.CartQueryRequest;
import com.morning.rentasnowhero.model.entity.Cart;
import com.morning.rentasnowhero.model.entity.CartFavour;
import com.morning.rentasnowhero.model.entity.CartThumb;
import com.morning.rentasnowhero.model.entity.User;
import com.morning.rentasnowhero.model.vo.CartVO;
import com.morning.rentasnowhero.model.vo.UserVO;
import com.morning.rentasnowhero.service.CartService;
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
 * 租赁车服务实现
 */
@Service
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param cart
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validCart(Cart cart, boolean add) {
        ThrowUtils.throwIf(cart == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = cart.getTitle();
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
     * @param cartQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Cart> getQueryWrapper(CartQueryRequest cartQueryRequest) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        if (cartQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = cartQueryRequest.getId();
        Long notId = cartQueryRequest.getNotId();
        String title = cartQueryRequest.getTitle();
        String content = cartQueryRequest.getContent();
        String searchText = cartQueryRequest.getSearchText();
        String sortField = cartQueryRequest.getSortField();
        String sortOrder = cartQueryRequest.getSortOrder();
        List<String> tagList = cartQueryRequest.getTags();
        Long userId = cartQueryRequest.getUserId();
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
     * 获取租赁车封装
     *
     * @param cart
     * @param request
     * @return
     */
    @Override
    public CartVO getCartVO(Cart cart, HttpServletRequest request) {
        // 对象转封装类
        CartVO cartVO = CartVO.objToVo(cart);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = cart.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        cartVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long cartId = cart.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<CartThumb> cartThumbQueryWrapper = new QueryWrapper<>();
            cartThumbQueryWrapper.in("cartId", cartId);
            cartThumbQueryWrapper.eq("userId", loginUser.getId());
            CartThumb cartThumb = cartThumbMapper.selectOne(cartThumbQueryWrapper);
            cartVO.setHasThumb(cartThumb != null);
            // 获取收藏
            QueryWrapper<CartFavour> cartFavourQueryWrapper = new QueryWrapper<>();
            cartFavourQueryWrapper.in("cartId", cartId);
            cartFavourQueryWrapper.eq("userId", loginUser.getId());
            CartFavour cartFavour = cartFavourMapper.selectOne(cartFavourQueryWrapper);
            cartVO.setHasFavour(cartFavour != null);
        }
        // endregion

        return cartVO;
    }

    /**
     * 分页获取租赁车封装
     *
     * @param cartPage
     * @param request
     * @return
     */
    @Override
    public Page<CartVO> getCartVOPage(Page<Cart> cartPage, HttpServletRequest request) {
        List<Cart> cartList = cartPage.getRecords();
        Page<CartVO> cartVOPage = new Page<>(cartPage.getCurrent(), cartPage.getSize(), cartPage.getTotal());
        if (CollUtil.isEmpty(cartList)) {
            return cartVOPage;
        }
        // 对象列表 => 封装对象列表
        List<CartVO> cartVOList = cartList.stream().map(cart -> {
            return CartVO.objToVo(cart);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = cartList.stream().map(Cart::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> cartIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> cartIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> cartIdSet = cartList.stream().map(Cart::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<CartThumb> cartThumbQueryWrapper = new QueryWrapper<>();
            cartThumbQueryWrapper.in("cartId", cartIdSet);
            cartThumbQueryWrapper.eq("userId", loginUser.getId());
            List<CartThumb> cartCartThumbList = cartThumbMapper.selectList(cartThumbQueryWrapper);
            cartCartThumbList.forEach(cartCartThumb -> cartIdHasThumbMap.put(cartCartThumb.getCartId(), true));
            // 获取收藏
            QueryWrapper<CartFavour> cartFavourQueryWrapper = new QueryWrapper<>();
            cartFavourQueryWrapper.in("cartId", cartIdSet);
            cartFavourQueryWrapper.eq("userId", loginUser.getId());
            List<CartFavour> cartFavourList = cartFavourMapper.selectList(cartFavourQueryWrapper);
            cartFavourList.forEach(cartFavour -> cartIdHasFavourMap.put(cartFavour.getCartId(), true));
        }
        // 填充信息
        cartVOList.forEach(cartVO -> {
            Long userId = cartVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            cartVO.setUser(userService.getUserVO(user));
            cartVO.setHasThumb(cartIdHasThumbMap.getOrDefault(cartVO.getId(), false));
            cartVO.setHasFavour(cartIdHasFavourMap.getOrDefault(cartVO.getId(), false));
        });
        // endregion

        cartVOPage.setRecords(cartVOList);
        return cartVOPage;
    }

}
