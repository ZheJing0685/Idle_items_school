package com.idleitems.school.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 分类变更事件，分类 CRUD 操作后发布
 */
@Getter
public class CategoryChangedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private final Long categoryId;

    /**
     * 变更类型：CREATE / UPDATE / DELETE / STATUS_CHANGE
     */
    private final String action;

    public CategoryChangedEvent(Object source, Long categoryId, String action) {
        super(source);
        this.categoryId = categoryId;
        this.action = action;
    }

    public CategoryChangedEvent(Object source, Long categoryId) {
        super(source);
        this.categoryId = categoryId;
        this.action = "UPDATE";
    }
}
