# munzi-docs
RestDocs 자동화를 위한 Utility 제공
  - attribute 생성, enum docs 생성 자동화 기능 제공


# 1. build.gradle 세팅

```groovy
plugins {
		...
    id "org.asciidoctor.convert" version "1.5.9" // adoc 파일 변환, build dir에 복사하는 역할
}

ext {
    snippetsDir = file('build/generated-snippets') // snippet 생성 dir명 전역변수 설정
}

test {
    outputs.dir snippetsDir
}

asciidoctor.doFirst {
    println "### start asciidoctor"
    delete file('src/main/resources/static/docs') // 기존 docs 삭제
}

asciidoctor.doLast {
    println "### end asciidoctor"
}

asciidoctor {
    dependsOn test
    attributes 'snippets': snippetsDir
    inputs.dir snippetsDir
}

task copyDocument(type: Copy) {
    dependsOn asciidoctor

    from file("build/asciidoc/html5/")
    into file("src/main/resources/static/docs")
}

build {
    dependsOn copyDocument
}

dependencies {
		"..."
		asciidoctor 'org.springframework.restdocs:spring-restdocs-asciidoctor'
    testCompile 'org.springframework.restdocs:spring-restdocs-mockmvc'
    annotationProcessor 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
		implementation group: 'com.munzi', name: 'munzi-docs', version: "${version_munzi_docs}"
}
```

# 2. Enum 설정

Document 화할 enum들을 한 패키지에 몰아 넣고, enum들은 munzi docs의 ‘IDocsEnumBase’를 implements 하도록 설정한다.

```java
//예시

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.munzi.munzidocs.component.IDocsEnumBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.*;

/**
 * 단위 유형
 */
@Getter
@AllArgsConstructor
@ToString(of = "value")
public enum UnitType implements IDocsEnumBase {
    MM("MM", "mm"),
    CM("CM","cm"),
    M("M","m");

    private String value;
    private String description;

    @JsonValue
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return description;
    }
   
		...

}
```

# 3. 기본 adoc(조각) 및 조각이 담길 디렉토리 생성

1. src 하위에 src/docs/asciidoc 디렉토리를 생성하고 index.adoc을 생성한다.

<index.adoc>

```
= MUNZI API
:doctype: book
:icons: font
:source-highlighter: highlightjs
:toc: left
:toclevels: 4
:sectlinks:
:operation-curl-request-title: Example request
:operation-http-response-title: Example response
:docinfo: shared-head

[[overview]]
= 개요

MUNZI API 문서 입니다.

'''

[[overview-http-verbs]]
== HTTP 동사

본 REST API에서 사용하는 HTTP 동사(verbs)는 가능한한 표준 HTTP와 REST 규약을 따릅니다.

|===
| 동사 | 용례

| `GET`
| 리소스를 가져올 때 사용

| `POST`
| 새 리소스를 만들 때 사용

| `PUT`
| 기존 리소스를 수정할 때 사용

| `PATCH`
| 기존 리소스의 일부를 수정할 때 사용

| `DELETE`
| 기존 리소스를 삭제할 때 사용
|===

[[overview-http-status-codes]]
== HTTP 상태 코드

본 REST API에서 사용하는 HTTP 상태 코드는 가능한한 표준 HTTP와 REST 규약을 따릅니다.

|===
| 상태 코드 | 용례

| `200 OK`
| 요청을 성공적으로 처리함

| `201 Created`
| 새 리소스를 성공적으로 생성함. 응답의 `Location` 헤더에 해당 리소스의 URI가 담겨있다.

| `204 No Content`
| 기존 리소스를 성공적으로 수정함.

| `400 Bad Request`
| 잘못된 요청을 보낸 경우. 응답 본문에 더 오류에 대한 정보가 담겨있다.

| `404 Not Found`
| 요청한 리소스가 없음.
|===

[[overview-errors]]
== 오류

에러 응답이 발생했을 때 (상태 코드 >= 400), 본문에 해당 문제를 기술한 JSON 객체가 담겨있다. 에러 객체는 다음의 구조를 따른다.

|===
|Path|Type

|code
|int

|message
|string

|timestamp
|string

|===

[[resources]]
= 리소스

[[resources-order]]
== 주문 관리

주문 관리 API

[[resources-order-create]]
=== 주문 단건 생성

`POST` 단건 주문을 생성한다. (주문 단건 생성)

operation::order-api-user-controller-docs-test/create-order_success[snippets='http-request,request-headers,request-fields,response-fields,http-response']

'''
```

index.adoc은 형식에 맞춰서 api 서비스에 맞게 작성하면 된다.

2. src/docs/asciidoc 하위에 enum adoc들이 담길 디렉토리를 하나 더 생성해 준다. (ex. common)

# 4. Rest Docs Configuration 설정

test에 RestDocsConfig.class 생성

```java
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.Preprocessors;

/**
 * REST Docs Configuration
 */
@TestConfiguration // test에서만 사용하는 configuration
public class RestDocsConfig {

    @Bean
    public RestDocumentationResultHandler write() {
        return MockMvcRestDocumentation.document(
                "{class-name}/{method-name}", // adoc 조각 명을 자동으로 생성
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()), // Request 본문 이쁘게 보이도록 설정
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()) // Response 본문 이쁘게 보이도록 설정
        );
    }

}
```

# 5. Template 추가

test/resources에 org.springframework.restdocs.templates 디렉토리를 생성하고,

다음 snippet들을 추가한다.

[custom-response-fields.snippet](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/27e1cc5f-d484-4d5b-9d8f-97e1c9c4e49b/custom-response-fields.snippet)

[request-fields.snippet](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/6333ef51-b291-479b-b6ad-706bf2893f8a/request-fields.snippet)

[request-parameters.snippet](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/041f8c03-89ad-40ee-b751-ea7e87394ff7/request-parameters.snippet)

[response-fields.snippet](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/08255a6d-0c0e-49dc-92d0-c5c77141ff97/response-fields.snippet)

# 6. EnumDocsController 생성

```java
import com.munzi.munzidocs.util.EnumDocsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/test/enum/docs")
@RequiredArgsConstructor
public class EnumDocsController {

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Map<String, String>> getEnumDocs() {
        return EnumDocsUtil.toEnumDocs("{enum package name}");
    }

}
```

# 7. EnumDocsControllerTest 생성

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.munzi.munzidocs.util.EnumDocsUtil;
import ~~.docs.config.RestDocsConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "docs.munzi.com", uriPort = 8081) // rest docs 설정
@Import(RestDocsConfig.class)
class EnumDocsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestDocumentationResultHandler restDocs;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void preCreateEnumDocs() {
        try {
            // 요청
            ResultActions resultActions = this.mockMvc.perform(get("/test/enum/docs")
                    .contentType(MediaType.APPLICATION_JSON));
            // 결과값
            MvcResult mvcResult = resultActions.andReturn();

            // 데이터 파싱
            Map<String, Map<String, String>> enumDocs = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Map.class);

            // enum별 adoc 생성
            EnumDocsUtil.createEnumDocsAdoc("~/src/docs/asciidoc/common", "EnumDocsControllerTest", "createEnumDocs", enumDocs.keySet());

            // enum docs를 popup으로 열기 위한 script가 들어있는 html 생성
            EnumDocsUtil.createDocInfoFile("~/src/docs/asciidoc");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createEnumDocs() {
        try {
            // 요청
            ResultActions resultActions = this.mockMvc.perform(get("/test/enum/docs")
                    .contentType(MediaType.APPLICATION_JSON));
            // 결과값
            MvcResult mvcResult = resultActions.andReturn();

            // 데이터 파싱
            Map<String, Map<String, String>> enumDocs = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Map.class);

            // 문서화 진행
            resultActions.andExpect(status().isOk())
                    .andDo(restDocs.document(EnumDocsUtil.toDocs(enumDocs)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

1. 먼저 preCreateEnumDocs를 실행시키면   

![tt1](https://user-images.githubusercontent.com/37896733/171124864-3016332f-9731-479e-9537-43e6b15cc3a7.png)

3번에서 생성한 src/docs/asciidoc 하위에 popup 설정을 위한 docinfo.html 파일이 생성되고, src/docs/asciidoc/common 하위에 enum들이 adoc으로 생성된다.

2. createEnumDocs를 실행시키면   

![tt2](https://user-images.githubusercontent.com/37896733/171124925-5dbd6aff-437e-498d-9025-08a73a3c9c57.png)

build/generated-snippets 하위에 lower hypen 형태로 변환된 EnumDocsControllerTest/createEnumDocs 디렉토리가 생성되고, 

그 하위에 custom-response-fields-{enum}.adoc 형태의 adoc들이 생성된다.

# 8. DocsType 생성

기본 type들과, document화한 enum들을 모두 DocsType으로 만들어 준다.

enum type의 경우, linkPageId와 description을 필수로 넣어주고, linkPageId는 enum명을 lower_hyphen으로 변환한 값이자 src/docs/asciidoc/common에 생성된 adoc 명을 적어주면 된다.

```java
import com.munzi.munzidocs.component.IDocsType;

public enum DocsType implements IDocsType {

    OBJECT("object", ""),
    BOOLEAN("boolean", ""),
    STRING("string", ""),
    PHONE("string", "010-0000-0000"),
    NUMBER("number", "1"),
    DECIMAL("decimal", "0.1"),
    FILE("file", "multipart-file"),
    DATETIME("datetime", "yyyy-MM-dd HH:mm:ss"),
    ARRAY("array", ""),
    MAP("map", ""),

    // enum type

    ACTIVE_TYPE("string", "", "active-type", "활성 상태 타입"), 
		...
    COUNTRY_TYPE("string", "", "country-type", "국가 코드");

    private String type;
    private String format;

    private String linkPageId;

    private String description;

    DocsType(String type, String format) {
        this.type = type;
        this.format = format;
    }

    DocsType(String type, String format, String linkPageId, String description) {
        this.type = type;
        this.format = format;
        this.linkPageId = linkPageId;
        this.description = description;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public String getLinkPageId() {
        return linkPageId;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
```

# 9. API Test Code 작성

```java
// example

@Test
@DisplayName("주문 상태 변경 (화주용) - 성공")
void updateOrderStatusByShipper_success() throws Exception {
	  ...

    ResultActions resultActions = this.mockMvc.perform(RestDocumentationRequestBuilders.patch(URL + "/{orderId}/status/shipper", orderId)
            .header("shipperId", 1L)
            .header("companyId", 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json));

    MvcResult result = resultActions.andExpect(status().is2xxSuccessful())
                .andDo(restDocs.document(
                    requestHeaders(
                            headerWithName("shipperId").description("화주 ID"),
                            headerWithName("companyId").description("회사 ID")
                    ),
                    pathParameters(
                            parameterWithName("orderId").description("주문 ID")
                    ),
                    requestFields(
                            fieldAttributes("orderId", DocsType.NUMBER, "주문 ID").ignored(),
                            enumFieldAttributes("status", DocsType.ORDER_STATUS)
                    ),
                    responseFields(
                            enumFieldAttributes("status", DocsType.ORDER_STATUS)
                    )
                ))
                .andReturn();

		...
}
```

1. 기본적으론 get, post, patch, delete method org.springframework.test.web.servlet.request.MockMvcRequestBuilders 의 것을 쓰지만, pathParameter를 사용하는 경우엔 org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders의 것을 써야 한다.
2. field 기본 설정
    1. request header : requestHeaders - headerWithName
    2. pathvariable : pathParameters - parameterWithName
    3. request parameter : requestParameters - parameterWithName
    4. request json field : requestFields - fieldWithName
    5. response json field : responseFields - fieldWithName
3. munzi docs의 Attribute : DocsAttribute
    1. requestFields/responseFields - fieldAttributes
        1. 기본 : fieldAttributes
        2. enum type : enumFieldAttrbutes(String fieldName, IDocsType type) → **link처리 된다!**
        3. mapFieldAttributes : key, value type 설정 가능
        4. arrayFieldAttributes : array type 설정 가능
    2. requestParam - paramWithName
        1. paramAttributes
        2. enumParamAttributes
        3. arrayParamAttributes
    

# 9.build

build시, build.gradle에서 snippetsDir로 선언해 줬던 build/generated-snippets 하위에

RestDocsConfig에서 선언해준 대로 class-name/method-name 경로에 조각(adoc)들이 생성된다.   

![tt3](https://user-images.githubusercontent.com/37896733/171125217-8765c321-49c1-483c-9b05-8d83ca82d5c6.png)

이 조각들을 이용해 최종 문서인  index.adoc을 작성해 줘야 한다.

```
[[resources-order-create]]
=== 주문 단건 생성

`POST`단건 주문을 생성한다. (주문 단건 생성)

operation::order-api-user-docs-controller-test/create-order_success[snippets='http-request,request-headers,request-fields,response-fields,http-response']
```

다음과 같이 adoc의 경로와 snippets에는 adoc 명을 적어주면, 순서대로 화면에 나오게 되고

intellij plugin을 설치하면 바로 보여주기도 한다.   

![tt4](https://user-images.githubusercontent.com/37896733/171127119-57c3acd5-faaf-48d5-ba3d-d1abd17e35a4.png)

index.adoc을 마저 작성 후 다시 build하면, build/asciidoc/html5에 index.html이 생성되고, common 하위에 enum document html들도 생성이 된다.

build시 copyDocument가 실행되어 src/main/resources/static/docs 경로로 enum document html들과 index.html이 copy가 되어, /docs/index.html로 해당 rest docs 문서를 확인할 수 있다.
