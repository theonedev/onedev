package io.onedev.server.commandhandler;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.onedev.server.util.Pair;

public class ExtractTranslationKeysTest {

    @Test
    public void testExtractWithMethod() {
        String content = "text = _T(\"a message\") + (_T(\"a message with \\\"quoted part\\\"\")) + _T(\"a message\\nanother message\")";
        assertArrayEquals(new String[] {"a message", "a message with \"quoted part\"", "a message\nanother message"}, ExtractTranslationKeys.extract_T(content).toArray());

        content = "text = _T(\"a message with \\u00A0\\u00A1\\u00A2\\u00A3\\u00A4\\u00A5\\u00A6\\u00A7\\u00A8\\u00A9\\u00AA\\u00AB\\u00AC\\u00AD\\u00AE\\u00AF\\u00B0\\u00B1\\u00B2\\u00B3\\u00B4\\u00B5\\u00B6\\u00B7\\u00B8\\u00B9\\u00BA\\u00BB\\u00BC\\u00BD\\u00BE\\u00BF\")";
        assertArrayEquals(new String[] {"a message with \u00A0\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7\u00A8\u00A9\u00AA\u00AB\u00AC\u00AD\u00AE\u00AF\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD\u00BE\u00BF"}, ExtractTranslationKeys.extract_T(content).toArray());

        content = "text = _T(\"\")";
        assertArrayEquals(new String[] {}, ExtractTranslationKeys.extract_T(content).toArray());
        
        content = "text = _T(\"\\t\\r\\n\\f\\b\\\\\")";
        assertArrayEquals(new String[] {"\t\r\n\f\b\\"}, ExtractTranslationKeys.extract_T(content).toArray());
        
        content = "text = _T(\"line1\")\n + _T(\"line2\")\n + _T(\"line3\")";
        assertArrayEquals(new String[] {"line1", "line2", "line3"}, ExtractTranslationKeys.extract_T(content).toArray());
        
        content = "text = _T(\"Unicode: \\u0048\\u0065\\u006C\\u006C\\u006F\")";
        assertArrayEquals(new String[] {"Unicode: Hello"}, ExtractTranslationKeys.extract_T(content).toArray());
        
        content = "text = _T(   \"spaced\"   )";
        assertArrayEquals(new String[] {"spaced"}, ExtractTranslationKeys.extract_T(content).toArray());
        
        content = "text = _T(\"double backslash: \\\\\\\\\")";
        assertArrayEquals(new String[] {"double backslash: \\\\"}, ExtractTranslationKeys.extract_T(content).toArray());
        
        content = "text = _T(\"Octal: \\101\\102\\103\")";
        assertArrayEquals(new String[] {"Octal: ABC"}, ExtractTranslationKeys.extract_T(content).toArray());
    }

    @Test
    public void testExtractWithTag() {
        String content = "<wicket:t>message1</wicket:t>\n<wicket:t><b>message2</b></wicket:t>\n<wicket:t>message\n3</wicket:t>";
        assertArrayEquals(new String[] {"message1", "<b>message2</b>", "message 3"}, ExtractTranslationKeys.extract_wicket_t(content).toArray());

        content = "<wicket:t>\n\thello\n\t<br>\n\tworld\n</wicket:t>";
        assertArrayEquals(new String[] {"hello <br> world"}, ExtractTranslationKeys.extract_wicket_t(content).toArray());
    }

    @Test
    public void testExtractTranslation() {
        String line = "                   m.put(\"Dashboards\", \"仪表盘\");";
        assertEquals(new Pair<>("Dashboards", "仪表盘"), 
                ExtractTranslationKeys.parseTranslation(line));

        line = "			m . put( \"Hello\" ,\"你好\" ) ;";
        assertEquals(new Pair<>("Hello", "你好"), 
                ExtractTranslationKeys.parseTranslation(line));
        
        line = "			m.put(\"Complex \\\"quoted\\\" text\", \"复杂的\\\"引用\\\"文本\");";
        assertEquals(new Pair<>("Complex \"quoted\" text", "复杂的\"引用\"文本"), 
                ExtractTranslationKeys.parseTranslation(line));
        
        line = "			m.put(\"Escaped characters: \\t\\r\\n\\f\\b\\\\\", \"转义字符: \\t\\r\\n\\f\\b\\\\\");";
        assertEquals(new Pair<>("Escaped characters: \t\r\n\f\b\\", "转义字符: \t\r\n\f\b\\"), 
                ExtractTranslationKeys.parseTranslation(line));
        
        line = "			m.put(\"Unicode: \\u0048\\u0065\\u006C\\u006C\\u006F\", \"Unicode: \\u4F60\\u597D\");";
        assertEquals(new Pair<>("Unicode: Hello", "Unicode: 你好"), 
                ExtractTranslationKeys.parseTranslation(line));
    }
}
