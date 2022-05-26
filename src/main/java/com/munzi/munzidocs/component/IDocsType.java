package com.munzi.munzidocs.component;

/**
 * Docs의 Type을 정의하는 Base
 */
public interface IDocsType {

    String getType();

    String getFormat();

    String getLinkPageId();

    String getDescription();

    default String getLink() {
        if (getLinkPageId() == null || getDescription() == null) return null;
        return String.format("link:common/%s.html[%s,role=\"popup\"]", getLinkPageId(), getDescription());
    }

}
