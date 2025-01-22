package com.wefky.RESTfulWeb.projection;

public interface ImageMetadata {
    Long getImageId();
    String getOwner();
    boolean isDeleted();
    String getContentType();
}
