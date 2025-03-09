package com.morning.rentasnowhero.model.vo;

import cn.hutool.json.JSONUtil;
import com.morning.rentasnowhero.model.entity.Admin;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 管理元视图
 *
 */
@Data
public class AdminVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param adminVO
     * @return
     */
    public static Admin voToObj(AdminVO adminVO) {
        if (adminVO == null) {
            return null;
        }
        Admin admin = new Admin();
        BeanUtils.copyProperties(adminVO, admin);
        List<String> tagList = adminVO.getTagList();
        admin.setTags(JSONUtil.toJsonStr(tagList));
        return admin;
    }

    /**
     * 对象转封装类
     *
     * @param admin
     * @return
     */
    public static AdminVO objToVo(Admin admin) {
        if (admin == null) {
            return null;
        }
        AdminVO adminVO = new AdminVO();
        BeanUtils.copyProperties(admin, adminVO);
        adminVO.setTagList(JSONUtil.toList(admin.getTags(), String.class));
        return adminVO;
    }
}
