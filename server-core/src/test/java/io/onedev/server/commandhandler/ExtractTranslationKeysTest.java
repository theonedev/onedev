package io.onedev.server.commandhandler;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class ExtractTranslationKeysTest {

        @Test
        public void testExtractFromMethod() {
                String content = "text = _T( var )";
                assertArrayEquals(new String[] {}, ExtractTranslationKeys.extractFromMethods(content).toArray());

                content = "text = _T( var + \"string\")";
                assertArrayEquals(new String[] {}, ExtractTranslationKeys.extractFromMethods(content).toArray());

                content = "text = _T(\"a message\") + (_T(\"a message with \\\"quoted part\\\"\")) + _T(\"a message\\nanother message\")";
                assertArrayEquals(
                                new String[] { "a message", "a message with \"quoted part\"",
                                                "a message\nanother message" },
                                ExtractTranslationKeys.extractFromMethods(content).toArray());

                content = "text = _T(\"a message with \\u00A0\\u00A1\\u00A2\\u00A3\\u00A4\\u00A5\\u00A6\\u00A7\\u00A8\\u00A9\\u00AA\\u00AB\\u00AC\\u00AD\\u00AE\\u00AF\\u00B0\\u00B1\\u00B2\\u00B3\\u00B4\\u00B5\\u00B6\\u00B7\\u00B8\\u00B9\\u00BA\\u00BB\\u00BC\\u00BD\\u00BE\\u00BF\")";
                assertArrayEquals(new String[] {
                                "a message with \u00A0\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7\u00A8\u00A9\u00AA\u00AB\u00AC\u00AD\u00AE\u00AF\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD\u00BE\u00BF" },
                                ExtractTranslationKeys.extractFromMethods(content).toArray());

                content = "text = _T(\"\")";
                assertArrayEquals(new String[] {}, ExtractTranslationKeys.extractFromMethods(content).toArray());

                content = "text = _T(\"\\t\\r\\n\\f\\b\\\\\")";
                assertArrayEquals(new String[] { "\t\r\n\f\b\\" },
                                ExtractTranslationKeys.extractFromMethods(content).toArray());

                content = "text = _T(\"line1\")\n + _T(\"line2\")\n + _T(\"line3\")";
                assertArrayEquals(new String[] { "line1", "line2", "line3" },
                                ExtractTranslationKeys.extractFromMethods(content).toArray());

                content = "text = _T(\"Unicode: \\u0048\\u0065\\u006C\\u006C\\u006F\")";
                assertArrayEquals(new String[] { "Unicode: Hello" },
                                ExtractTranslationKeys.extractFromMethods(content).toArray());

                content = "text = _T(   \"spaced\"   )";
                assertArrayEquals(new String[] { "spaced" },
                                ExtractTranslationKeys.extractFromMethods(content).toArray());

                content = "text = _T(\"double backslash: \\\\\\\\\")";
                assertArrayEquals(new String[] { "double backslash: \\\\" },
                                ExtractTranslationKeys.extractFromMethods(content).toArray());

                content = "text = _T(\"hello\" \n + \"world\")\n\n _T(\"one\" + \n \"two\"\n + \"three\")";
                assertArrayEquals(new String[] { "helloworld", "onetwothree" },
                                ExtractTranslationKeys.extractFromMethods(content).toArray());
        }

        @Test
        public void testExtractFromTag() {
                String content = "<wicket:t>message1</wicket:t>\n<wicket:t><b>message2</b></wicket:t>\n<wicket:t>message\n3</wicket:t>";
                assertArrayEquals(new String[] { "message1", "<b>message2</b>", "message 3" },
                                ExtractTranslationKeys.extractFromTags(content).toArray());

                content = "<wicket:t>\n\thello\n\t<br>\n\tworld\n</wicket:t>";
                assertArrayEquals(new String[] { "hello <br> world" },
                                ExtractTranslationKeys.extractFromTags(content).toArray());
        }

        @Test
        public void testExtractFromAttribute() {
                String content = "<a t:title=\"message1\">message1</a>\n<a t:title='message2'>message2</a> <input t:placeholder = \"message '3' \" >message3</input> \n"
                                + "<a t:title  =  ' message \"5\" '\nt:value='message  \n  &quot;6'>message5</a> \n\n <div wicket:id='test'>test</div>";
                assertArrayEquals(
                                new String[] { "message1", "message2", "message '3'", "message \"5\"",
                                                "message  \n  \"6" },
                                ExtractTranslationKeys.extractFromAttributes(content).toArray());
        }

        @Test
        public void testUpdateTranslationKeys() {
                var content = "\n"
                                + "\t\t//  Extracted  keys\n"
                                + "\t\tm.put(\"message1\", \"消息$100\");\n"
                                + "\n"
                                + "\t\tm.put(\"message2\", \n\t\t\"**** translate this ****\");\n"
                                + "\n"
                                + "\t\t\t//  Manually added  keys";
                assertEquals(
                                "\n\t\t//  Extracted  keys\n\t\tm.put(\"message1\", \"消息$100\");\n\t\tm.put(\"message2\", \"**** translate this ****\");\n\t\tm.put(\"message3\", \"**** translate this ****\");\n\n\t\t\t//  Manually added  keys",
                                ExtractTranslationKeys.updateTranslationKeys(content,
                                                List.of("message1", "message2", "message3")));
        }
}
