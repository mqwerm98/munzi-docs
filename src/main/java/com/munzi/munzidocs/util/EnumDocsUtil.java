package com.munzi.munzidocs.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.munzi.munzidocs.component.IDocsEnumBase;
import com.munzi.munzidocs.component.CustomFieldsSnippet;
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

/**
 * EnumDocs로 변환하는데 사용하는 Util 모음
 *
 * @param <ENUM_DOCS> 문서화 할 enum map 모음
 */
public class EnumDocsUtil<ENUM_DOCS> {

    private final ObjectMapper objectMapper;

    public EnumDocsUtil() {
        this.objectMapper = new ObjectMapper();
    }

    public EnumDocsUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    /**
     * MvcResult -> responseType으로 parsing
     *
     * @param mvcResult MvcResult
     * @param responseType 변경할 타입
     * @return responseType으로 parsing된 mvcResult
     * @throws IOException parsing 오류
     */
    public ENUM_DOCS toEnumDocs(MvcResult mvcResult, TypeReference<ENUM_DOCS> responseType) throws IOException {
        return objectMapper.readValue(
                mvcResult.getResponse().getContentAsByteArray(), responseType
        );
    }

    /**
     * enumDocs를 받아서 문서(snippet)화 진행
     *
     * @param enumDocs 문서화 할 enumDocs
     * @return snippet들로 변환한 enumDocs
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
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

    /**
     * DocsEnum 배열을 map으로 변환하고, value를 기준으로 정렬한다.
     *
     * @param docsEnumBases map으로 변환 할 DocsEnum 배열
     * @return map으로 변환한 docsEnumBases
     */
    public Map<String, String> toMap(IDocsEnumBase[] docsEnumBases) {
        Map<String, String> map = Arrays.stream(docsEnumBases)
                .collect(Collectors.toMap(IDocsEnumBase::getValue, IDocsEnumBase::getDescription));

        // map key 기준으로 정렬
        List<Map.Entry<String, String>> entries = new LinkedList<>(map.entrySet());
        entries.sort(Map.Entry.comparingByKey());
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * custom-response-{subName}-field.snippet 생성
     *
     * @param subName    custom-response- 뒤에 붙일 이름
     * @param title      enum docs의 title (페이지 상단에 붙는 이름)
     * @param enumMap 해당 enum의 map화된 값
     * @return custom된 Snippet
     */
    private CustomFieldsSnippet customResponseFields(String subName, String title, Map<String, String> enumMap) {
        FieldDescriptor[] fieldDescriptors = enumMap.entrySet().stream()
                .map(x -> fieldWithPath(x.getKey()).description(x.getValue()))
                .toArray(FieldDescriptor[]::new);

        return new CustomFieldsSnippet("custom-response",
                beneathPath(subName).withSubsectionId(subName),
                Arrays.asList(fieldDescriptors),
                attributes(key("title").value(title)),
                true);
    }

}
