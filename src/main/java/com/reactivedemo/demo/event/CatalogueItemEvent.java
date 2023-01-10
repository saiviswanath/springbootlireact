package com.reactivedemo.demo.event;

import org.springframework.context.ApplicationEvent;

import com.reactivedemo.demo.entity.CatalogueItem;

/**
 * Event thrown when CatalogueItem is created or updated
 */
public class CatalogueItemEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;
	public static final String CATALOGUEITEM_CREATED = "CREATED";
    public static final String CATALOGUEITEM_UPDATED = "UPDATED";

    private String eventType;

    public CatalogueItemEvent(String eventType, CatalogueItem catalogueItem) {
        super(catalogueItem);
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }
}
