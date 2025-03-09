package com.morning.rentasnowhero.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.morning.rentasnowhero.model.dto.user.UserQueryRequest;
import com.morning.rentasnowhero.model.entity.User;
import com.morning.rentasnowhero.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
 */
public interface UserService extends IService<User> {

    /**
     * 校验数据
     *
     * @param user
     * @param add 对创建的数据进行校验
     */
    void validUser(User user, boolean add);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
    
    /**
     * 获取用户封装
     *
     * @param user
     * @param request
     * @return
     */
    UserVO getUserVO(User user, HttpServletRequest request);

    /**
     * 分页获取用户封装
     *
     * @param userPage
     * @param request
     * @return
     */
    Page<UserVO> getUserVOPage(Page<User> userPage, HttpServletRequest request);
}
