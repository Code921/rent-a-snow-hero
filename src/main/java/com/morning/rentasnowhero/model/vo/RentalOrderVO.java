package com.morning.rentasnowhero.model.vo;

import cn.hutool.json.JSONUtil;
import com.morning.rentasnowhero.model.entity.RentalOrder;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 出租订单视图
 *
 */
@Data
public class RentalOrderVO implements Serializable {

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
     * @param rentalOrderVO
     * @return
     */
    public static RentalOrder voToObj(RentalOrderVO rentalOrderVO) {
        if (rentalOrderVO == null) {
            return null;
        }
        RentalOrder rentalOrder = new RentalOrder();
        BeanUtils.copyProperties(rentalOrderVO, rentalOrder);
        List<String> tagList = rentalOrderVO.getTagList();
        rentalOrder.setTags(JSONUtil.toJsonStr(tagList));
        return rentalOrder;
    }

    /**
     * 对象转封装类
     *
     * @param rentalOrder
     * @return
     */
    public static RentalOrderVO objToVo(RentalOrder rentalOrder) {
        if (rentalOrder == null) {
            return null;
        }
        RentalOrderVO rentalOrderVO = new RentalOrderVO();
        BeanUtils.copyProperties(rentalOrder, rentalOrderVO);
        rentalOrderVO.setTagList(JSONUtil.toList(rentalOrder.getTags(), String.class));
        return rentalOrderVO;
    }
}
