package com.morning.rentasnowhero.model.dto.cart;

import com.morning.rentasnowhero.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 查询租赁车请求
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CartQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * id
     */
    private Long notId;

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}