package com.munzi.munzidocs.util;

import com.munzi.munzidocs.component.IDocsType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.snippet.Attributes;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.snippet.Attributes.key;

/**
 * RestDocs Custom Attribute
 */
public class DocsAttribute {

    public static Attributes.Attribute type(String type) {
        return key("type").value(type);
    }

    public static Attributes.Attribute type(IDocsType type) {
        return key("type").value(type.getType());
    }

    public static Attributes.Attribute type(IDocsType type, int size) {
        return key("type").value(type.getType() + "(" + size + ")");
    }

    public static Attributes.Attribute format(IDocsType type) {
        return key("format").value(type.getFormat());
    }

    public static Attributes.Attribute format(String format) {
        return key("format").value(format);
    }

    public static FieldDescriptor enumFieldAttributes(String fieldName, IDocsType type) {
        return fieldWithPath(fieldName).attributes(type(type), format(type)).description(type.getLink());
    }

    public static FieldDescriptor fieldAttributes(String fieldName, IDocsType type, String format, String description) {
        return fieldWithPath(fieldName).attributes(type(type), format(format)).description(description);
    }

    public static FieldDescriptor fieldAttributes(String fieldName, IDocsType type, String description) {
        return fieldWithPath(fieldName).attributes(type(type), format(type)).description(description);
    }

    public static FieldDescriptor mapFieldAttributes(String fieldName, IDocsType keyType, IDocsType valueType) {
        String format = "map (key : " + keyType.getType() + " / value : " + valueType.getType() + ")";
        return fieldWithPath(fieldName).attributes(type("map"), format(format));
    }

    public static FieldDescriptor mapFieldAttributes(String fieldName, IDocsType keyType, IDocsType valueType, String description) {
        return mapFieldAttributes(fieldName, keyType, valueType).description(description);
    }

    public static FieldDescriptor arrayFieldAttributes(String fieldName, IDocsType type) {
        String format = "array[" + type.getType() + "]";
        return fieldWithPath(fieldName).attributes(type("array"), format(format));
    }

    public static FieldDescriptor arrayFieldAttributes(String fieldName, IDocsType type, String description) {
        return arrayFieldAttributes(fieldName, type).description(description);
    }

    public static ParameterDescriptor enumParamAttributes(String paramName, IDocsType type) {
        return parameterWithName(paramName).attributes(type(type), format(type)).description(type.getLink());
    }

    public static ParameterDescriptor paramAttributes(String paramName, IDocsType type, String description) {
        return parameterWithName(paramName).attributes(type(type), format(type)).description(description);
    }

    public static ParameterDescriptor arrayParamAttributes(String paramName, IDocsType type) {
        String format = "array[" + type.getType() + "]";
        return parameterWithName(paramName).attributes(type("array"), format(format));
    }

    public static ParameterDescriptor arrayParamAttributes(String paramName, IDocsType type, String description) {
        return arrayParamAttributes(paramName, type).description(description);
    }
}