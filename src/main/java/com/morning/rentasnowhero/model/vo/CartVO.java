package com.morning.rentasnowhero.model.vo;

import cn.hutool.json.JSONUtil;
import com.morning.rentasnowhero.model.entity.Cart;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 租赁车视图
 *
 */
@Data
public class CartVO implements Serializable {

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
     * @param cartVO
     * @return
     */
    public static Cart voToObj(CartVO cartVO) {
        if (cartVO == null) {
            return null;
        }
        Cart cart = new Cart();
        BeanUtils.copyProperties(cartVO, cart);
        List<String> tagList = cartVO.getTagList();
        cart.setTags(JSONUtil.toJsonStr(tagList));
        return cart;
    }

    /**
     * 对象转封装类
     *
     * @param cart
     * @return
     */
    public static CartVO objToVo(Cart cart) {
        if (cart == null) {
            return null;
        }
        CartVO cartVO = new CartVO();
        BeanUtils.copyProperties(cart, cartVO);
        cartVO.setTagList(JSONUtil.toList(cart.getTags(), String.class));
        return cartVO;
    }
}
