package io.onedev.server.commandhandler;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class TranslateOtherLanguagesTest {

    @Test
    public void test() {
        var englishContent = "m.put(\"container:image\", \"Image\");";
        var chineseContent = "" + 
            "m.put(\"hello\", \"你好\");\n" + 
            "m.put(\"world\", \"世界\");\n" + 
            "m.put(\"container:image\", \"容器镜像\");";
        var content = "{\n" + 
            "\t\tm.clear();\n" +
            "\t\tm.put(\"hello\", \"Hallo\");\n" + 
            "}";
        var translated = TranslateOtherLanguages.translate(englishContent, chineseContent, content, new Function<>() {
            
            public List<String> apply(List<String> t) {
                return t.stream().map(it->"translated " + it).collect(Collectors.toList());
            }

        }, 100);

        var expected = "{\n" + 
            "\t\tm.clear();\n" + 
            "\t\tm.put(\"hello\", \"Hallo\");\n" + 
            "\t\tm.put(\"container:image\", \"translated Image<<<container>>>\");\n" + 
            "\t\tm.put(\"world\", \"translated world\");\n" + 
            "}";
        Assert.assertEquals(expected, translated);
    }

}
