package com.morning.rentasnowhero.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.morning.rentasnowhero.model.dto.admin.AdminQueryRequest;
import com.morning.rentasnowhero.model.entity.Admin;
import com.morning.rentasnowhero.model.vo.AdminVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理元服务
 *
 */
public interface AdminService extends IService<Admin> {

    /**
     * 校验数据
     *
     * @param admin
     * @param add 对创建的数据进行校验
     */
    void validAdmin(Admin admin, boolean add);

    /**
     * 获取查询条件
     *
     * @param adminQueryRequest
     * @return
     */
    QueryWrapper<Admin> getQueryWrapper(AdminQueryRequest adminQueryRequest);
    
    /**
     * 获取管理元封装
     *
     * @param admin
     * @param request
     * @return
     */
    AdminVO getAdminVO(Admin admin, HttpServletRequest request);

    /**
     * 分页获取管理元封装
     *
     * @param adminPage
     * @param request
     * @return
     */
    Page<AdminVO> getAdminVOPage(Page<Admin> adminPage, HttpServletRequest request);
}
