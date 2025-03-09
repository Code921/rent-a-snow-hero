package com.morning.rentasnowhero.model.dto.equipmenttype;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新装备类型请求
 *
 */
@Data
public class EquipmentTypeUpdateRequest implements Serializable {

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