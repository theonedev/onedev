package io.onedev.server.commandhandler;

import static org.junit.Assert.assertArrayEquals;

import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class TranslateTest {

        @Test
        public void testScanJavaMethods() {
                String content = "text = _T( var )";
                assertArrayEquals(new String[] {}, Translate.scanJavaMethods(content).toArray());

                content = "text = _T( var + \"string\")";
                assertArrayEquals(new String[] {}, Translate.scanJavaMethods(content).toArray());

                content = "text = _T(\"a message\") + (_T(\"a message with \\\"quoted part\\\"\")) + _T(\"a message\\nanother message\")";
                assertArrayEquals(
                                new String[] { "a message", "a message with \"quoted part\"",
                                                "a message\nanother message" },
                                Translate.scanJavaMethods(content).toArray());

                content = "text = _T(\"a message with \\u00A0\\u00A1\\u00A2\\u00A3\\u00A4\\u00A5\\u00A6\\u00A7\\u00A8\\u00A9\\u00AA\\u00AB\\u00AC\\u00AD\\u00AE\\u00AF\\u00B0\\u00B1\\u00B2\\u00B3\\u00B4\\u00B5\\u00B6\\u00B7\\u00B8\\u00B9\\u00BA\\u00BB\\u00BC\\u00BD\\u00BE\\u00BF\")";
                assertArrayEquals(new String[] {
                                "a message with \u00A0\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7\u00A8\u00A9\u00AA\u00AB\u00AC\u00AD\u00AE\u00AF\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD\u00BE\u00BF" },
                                Translate.scanJavaMethods(content).toArray());

                content = "text = _T(\"\")";
                assertArrayEquals(new String[] {}, Translate.scanJavaMethods(content).toArray());

                content = "text = _T(\"\\t\\r\\n\\f\\b\\\\\")";
                assertArrayEquals(new String[] { "\t\r\n\f\b\\" },
                                Translate.scanJavaMethods(content).toArray());

                content = "text = _T(\"line1\")\n + _T(\"line2\")\n + _T(\"line3\")";
                assertArrayEquals(new String[] { "line1", "line2", "line3" },
                                Translate.scanJavaMethods(content).toArray());

                content = "text = _T(\"Unicode: \\u0048\\u0065\\u006C\\u006C\\u006F\")";
                assertArrayEquals(new String[] { "Unicode: Hello" },
                                Translate.scanJavaMethods(content).toArray());

                content = "text = _T(   \"spaced\"   )";
                assertArrayEquals(new String[] { "spaced" },
                                Translate.scanJavaMethods(content).toArray());

                content = "text = _T(\"double backslash: \\\\\\\\\")";
                assertArrayEquals(new String[] { "double backslash: \\\\" },
                                Translate.scanJavaMethods(content).toArray());

                content = "text = _T(\"hello\" \n + \"world\")\n\n _T(\"one\" + \n \"two\"\n + \"three\")";
                assertArrayEquals(new String[] { "helloworld", "onetwothree" },
                                Translate.scanJavaMethods(content).toArray());
        }

        @Test
        public void testScanHtmlTags() {
                String content = "<wicket:t>message1</wicket:t>\n<wicket:t><b>message2</b></wicket:t>\n<wicket:t>message\n3</wicket:t>";
                assertArrayEquals(new String[] { "message1", "<b>message2</b>", "message 3" },
                                Translate.scanHtmlTags(content).toArray());

                content = "<wicket:t>\n\thello\n\t<br>\n\tworld\n</wicket:t>";
                assertArrayEquals(new String[] { "hello <br> world" },
                                Translate.scanHtmlTags(content).toArray());
        }

        @Test
        public void testScanHtmlAttributes() {
                String content = "<a t:title=\"message1\">message1</a>\n<a t:title='message2'>message2</a> <input t:placeholder = \"message '3' \" >message3</input> \n"
                                + "<a t:title  =  ' message \"5\" '\nt:value='message  \n  &quot;6'>message5</a> \n\n <div wicket:id='test'>test</div>";
                assertArrayEquals(
                                new String[] { "message1", "message2", "message '3'", "message \"5\"",
                                                "message  \n  \"6" },
                                Translate.scanHtmlAttributes(content).toArray());
        }

        @Test
        public void testGenerateFileContent() {
            var englishTranslations = new TreeMap<String, String>();
            englishTranslations.put("hello", "hello");
            englishTranslations.put("container:image", "Image");
            englishTranslations.put("world", "world");

            var existingTranslations = new TreeMap<String, String>();
            existingTranslations.put("hello", "你好");
            existingTranslations.put("test", "测试");

            var oldFileContent = "{\n" + 
                "\t\tm.clear();\n" + 
                "\t\tm.put(\"hello\", \"你好\");\n" + 
                "\t\tm.put(\"test\", \"测试\");\n" + 
                "}";

            var translated = Translate.generateFileContent(englishTranslations, existingTranslations, oldFileContent, new Function<>() {
                
                public List<String> apply(List<String> t) {
                    return t.stream().map(it->"translated " + it).collect(Collectors.toList());
                }
    
            }, 100);
    
            var expected = "{\n" + 
                "\t\tm.clear();\n" + 
                "\t\tm.put(\"hello\", \"你好\");\n" + 
                "\t\tm.put(\"container:image\", \"translated Image <<<translation context: container>>>\");\n" + 
                "\t\tm.put(\"world\", \"translated world\");\n" + 
                "}";
            Assert.assertEquals(expected, translated);
        }
    
}
