package com.munzi.munzidocs.util;

import com.google.common.base.CaseFormat;
import com.munzi.munzidocs.component.CustomFieldsSnippet;
import com.munzi.munzidocs.component.IDocsEnumBase;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.snippet.Snippet;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;

/**
 * EnumDocs로 변환하는데 사용하는 Util 모음
 */
public class EnumDocsUtil {

    /**
     * packageName 하위에 있는 enum을 Map형대로 변환
     *
     * @param packageName enum들이 있는 package명
     * @return map형태로 변환된 enum
     */
    public Map<String, Map<String, String>> toEnumDocs(String packageName) {
        EnumDocsUtil enumDocsUtil = new EnumDocsUtil();
        Map<String, Map<String, String>> enumDocs = new HashMap<>();

        Set<Class> classes = getClasses(packageName);
        assert classes != null;
        for (Class c : classes) {
            String name = c.getSimpleName();
            if (name.isEmpty()) continue;
            String lowerName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);

            IDocsEnumBase[] enumConstants = (IDocsEnumBase[]) c.getEnumConstants();
            enumDocs.put(lowerName, enumDocsUtil.toMap(enumConstants));
        }

        return enumDocs;
    }

    /**
     * enumDocs를 받아서 문서(snippet)화 진행
     *
     * @param enumDocs 문서화 할 enumDocs
     * @return snippet들로 변환한 enumDocs
     */
    public Snippet[] toDocs(Map<String, Map<String, String>> enumDocs) {
        Set<String> keys = enumDocs.keySet();
        Snippet[] ss = new Snippet[keys.size()];

        int i = 0;
        for (String key : keys) {
            Map<String, String> map = enumDocs.get(key);
            ss[i] = customResponseFields(key, key, map);
            i++;
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
     * enum명을 받아 enumDocsAdocPath에 adoc을 자동으로 생성해 준다.
     *
     * @param enumDocsAdocPath enum adoc을 생성할 경로
     * @param enumDocsNames enum명들
     * @throws Exception 파일 생성 에러
     */
    public void createEnumDocAdoc(String enumDocsAdocPath, Set<String> enumDocsNames) throws Exception {
        for (String name : enumDocsNames) {
            String hyphenName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, name);

            String txt = ":doctype: book\n" +
                    ":icons: font\n\n" +
                    "[[" + hyphenName + "]]\n" +
                    "include::{snippets}/document-controller-test/enums/custom-response-fields-" + name + ".adoc[]";
            String fileName = enumDocsAdocPath + (enumDocsAdocPath.endsWith("/") ? "" : "/") + hyphenName + ".adoc";

            // 파일 객체 생성
            File file = new File(fileName);

            // true 지정시 파일의 기존 내용에 이어서 작성
            FileWriter fw = new FileWriter(file, true);

            // 파일안에 문자열 쓰기
            fw.write(txt);
            fw.flush();

            // 객체 닫기
            fw.close();
        }
    }

    /**
     * enum docs를 팝업으로 띄우기 위한 docinfo.html 파일을 docInfoFilePath 경로에 생성해 준다.
     *
     * @param docInfoFilePath docinfo.html 파일을 생성할 경로
     * @throws Exception 파일 생성 오류
     */
    public void createDocInfoFile(String docInfoFilePath) throws Exception {
        String script = "<script>\n" +
                "    function ready(callbackFunc) {\n" +
                "        if (document.readyState !== 'loading') {\n" +
                "            // Document is already ready, call the callback directly\n" +
                "            callbackFunc();\n" +
                "        } else if (document.addEventListener) {\n" +
                "            // All modern browsers to register DOMContentLoaded\n" +
                "            document.addEventListener('DOMContentLoaded', callbackFunc);\n" +
                "        } else {\n" +
                "            // Old IE browsers\n" +
                "            document.attachEvent('onreadystatechange', function () {\n" +
                "                if (document.readyState === 'complete') {\n" +
                "                    callbackFunc();\n" +
                "                }\n" +
                "            });\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    function openPopup(event) {\n" +
                "        const target = event.target;\n" +
                "        if (target.className !== \"popup\") { //(1)\n" +
                "            return;\n" +
                "        }\n" +
                "\n" +
                "        event.preventDefault();\n" +
                "        const screenX = event.screenX;\n" +
                "        const screenY = event.screenY;\n" +
                "        window.open(target.href, target.text, `left=$, top=$, width=500, height=600, status=no, menubar=no, toolbar=no, resizable=no`);\n" +
                "    }\n" +
                "\n" +
                "    ready(function () {\n" +
                "        const el = document.getElementById(\"content\");\n" +
                "        el.addEventListener(\"click\", event => openPopup(event), false);\n" +
                "    });\n" +
                "</script>";

        String fileName = docInfoFilePath + (docInfoFilePath.endsWith("/") ? "" : "/") + "docinfo.html";

        File file = new File(fileName);
        FileWriter fw = new FileWriter(file, true);
        fw.write(script);
        fw.flush();
        fw.close();
    }


    /**
     * packageName 하위의 모든 Class들을 받아온다.
     *
     * @param packageName 조회할 package 명(경로)
     * @return packageName 하위의 모든 class들
     */
    private Set<Class> getClasses(String packageName) {
        Set<Class> classes = new HashSet<Class>();
        String packageNameSlash = "./" + packageName.replace(".", "/");
        java.net.URL directoryURL = Thread.currentThread().getContextClassLoader().getResource(packageNameSlash);
        if (directoryURL == null) {
            System.err.println("Could not retrieve URL resource : " + packageNameSlash);
            return null;
        }

        String directoryString = directoryURL.getFile();
        if (directoryString == null) {
            System.err.println("Could not find directory for URL resource : " + packageNameSlash);
            return null;
        }

        File directory = new File(directoryString);
        if (directory.exists()) {
            String[] files = directory.list();
            for (String fileName : files) {
                if (fileName.endsWith(".class")) {
                    fileName = fileName.substring(0, fileName.length() - 6);  // remove .class
                    try {
                        Class c = Class.forName(packageName + "." + fileName);
                        if (!Modifier.isAbstract(c.getModifiers())) // add a class which is not abstract
                            classes.add(c);
                    } catch (ClassNotFoundException e) {
                        System.err.println(packageName + "." + fileName + " does not appear to be a valid class");
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.err.println(packageName + " does not appear to exist as a valid package on the file system.");
        }

        return classes;
    }


    /**
     * custom-response-{subName}-field.snippet 생성
     *
     * @param subName custom-response- 뒤에 붙일 이름
     * @param title   enum docs의 title (페이지 상단에 붙는 이름)
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
