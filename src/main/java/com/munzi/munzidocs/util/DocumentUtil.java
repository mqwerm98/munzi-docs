package com.munzi.munzidocs.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.munzi.munzidocs.component.EnumType;
import com.munzi.munzidocs.component.CustomResponseFieldsSnippet;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.web.servlet.MvcResult;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;

public class DocumentUtil<ENUM_DOCS> {

    private final ObjectMapper objectMapper;

    public DocumentUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // 커스텀 템플릿 사용을 위한 함수
    private CustomResponseFieldsSnippet customResponseFields(String subName, String title, Map<String, String> enumValues) {
        return new CustomResponseFieldsSnippet("custom-response",
                beneathPath(subName).withSubsectionId(subName),
                Arrays.asList(enumConvertFieldDescriptor(enumValues)),
                attributes(key("title").value(title)),
                true);
    }

    // Map으로 넘어온 enumValue를 fieldWithPath로 변경하여 리턴
    private FieldDescriptor[] enumConvertFieldDescriptor(Map<String, String> enumValues) {
        return enumValues.entrySet().stream()
                .map(x -> fieldWithPath(x.getKey()).description(x.getValue()))
                .toArray(FieldDescriptor[]::new);
    }

    // mvc result 데이터 파싱
    public ENUM_DOCS toEnumDocs(MvcResult result) throws IOException {
        return objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                new TypeReference<ENUM_DOCS>() {
                }
        );
    }

    public Snippet[] toDocs(ENUM_DOCS enumDocs) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Field[] fields = enumDocs.getClass().getDeclaredFields();
        Snippet[] ss = new Snippet[fields.length];
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            Method getter = new PropertyDescriptor(f.getName(), enumDocs.getClass()).getReadMethod();
            Map<String, String> result = (Map<String, String>) getter.invoke(enumDocs);
            ss[i] = customResponseFields(f.getName(), f.getName(), result);
        }
        return ss;
    }

    public Map<String, String> toMap(EnumType[] enumTypes) {
        Map<String, String> map = Arrays.stream(enumTypes)
                .collect(Collectors.toMap(EnumType::getValue, EnumType::getDescription));

        // map key 기준으로 정렬
        List<Map.Entry<String, String>> entries = new LinkedList<>(map.entrySet());
        entries.sort(Map.Entry.comparingByKey());
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

}
