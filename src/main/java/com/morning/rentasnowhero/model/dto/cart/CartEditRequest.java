package com.morning.rentasnowhero.model.dto.cart;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑租赁车请求
 *
 */
@Data
public class CartEditRequest implements Serializable {

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
     * 标签列表
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}