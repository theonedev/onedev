package io.onedev.server.web.avatar;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.common.collect.ImmutableSet;

public class AvatarGenerator {
	
	private static class Holder {
		static final GraphicsEnvironment localGraphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
		static final Set<String> ALL_FONT_FAMILIES =
				ImmutableSet.copyOf(localGraphics.getAvailableFontFamilyNames());
	}
	
	private static final int DEFAULT_WIDTH = 256;
	private static final int DEFAULT_HEIGHT = 256;
	private static final String DEFAULT_FONT = "Helvetica";
	
	private static final int VERSION = 1;
	
//	private static final Color BLACK = new Color(51, 51, 51); // #333
	
	public static int version() {
		return VERSION;
	}
	
	public static BufferedImage generate(String name, String email) throws NoSuchAlgorithmException {
		Long index = getColorIndex(email);
		String family;
		if (Holder.ALL_FONT_FAMILIES.contains(DEFAULT_FONT)) {
			family = DEFAULT_FONT;
		} else {
			family = Font.MONOSPACED;
		}

		Color background = COLORS[index.intValue()];
		Color foreground = Color.white;

		return generate(
				DEFAULT_WIDTH,
				DEFAULT_HEIGHT,
				name,
				family,
				background,
				foreground);
	}

	public static BufferedImage generate(int width, int height, String message,
			String fontFamily, Color background, Color foreground) {
		BufferedImage bi = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		Graphics ig2 = null;

		try {
			ig2 = bi.getGraphics();

			ig2.setColor(background);
			ig2.fillRect(0, 0, width, height);

			int fontSize = new Double(height * 0.5d).intValue();
			Font font = new Font(fontFamily, Font.PLAIN, fontSize);
			Map<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
			map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
			font = font.deriveFont(map);
			ig2.setFont(font);
			ig2.setColor(foreground);
			drawCenteredString(message.toUpperCase(), width, height, ig2);
		} finally {
			if (ig2 != null)
				ig2.dispose();
		}

		return bi;
	}

	private static void drawCenteredString(String s, int w, int h, Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int x = (w - fm.stringWidth(s)) / 2;
		int y = h / 2 - (fm.getAscent() + fm.getDescent()) / 2 + fm.getAscent() + 5;
		g.drawString(s, x, y);
	}

	private static Long getColorIndex(String email) throws NoSuchAlgorithmException {
		String hex = DigestUtils.md5Hex(email);
		return Long.parseLong(hex.substring(0, 15), 16) % COLORS.length;
	}

	/* palette of optimally disctinct colors
	 * cf. http://tools.medialab.sciences-po.fr/iwanthue/index.php
	 * parameters used:
	 *   - H: 0 - 360
	 *   - C: 0 - 2
	 *   - L: 0.75 - 1.5
	 */
	static final Color[] COLORS = new Color[] {
	      new Color(198,125,40),
	      new Color(61,155,243),
	      new Color(74,243,75),
	      new Color(238,89,166),
	      new Color(52,240,224),
	      new Color(177,156,155),
	      new Color(240,120,145),
	      new Color(111,154,78),
	      new Color(237,179,245),
	      new Color(237,101,95),
	      new Color(89,239,155),
	      new Color(43,254,70),
	      new Color(163,212,245),
	      new Color(65,152,142),
	      new Color(165,135,246),
	      new Color(181,166,38),
	      new Color(187,229,206),
	      new Color(77,164,25),
	      new Color(179,246,101),
	      new Color(234,93,37),
	      new Color(225,155,115),
	      new Color(142,140,188),
	      new Color(223,120,140),
	      new Color(249,174,27),
	      new Color(244,117,225),
	      new Color(137,141,102),
	      new Color(75,191,146),
	      new Color(188,239,142),
	      new Color(164,199,145),
	      new Color(173,120,149),
	      new Color(59,195,89),
	      new Color(222,198,220),
	      new Color(68,145,187),
	      new Color(236,204,179),
	      new Color(159,195,72),
	      new Color(188,121,189),
	      new Color(166,160,85),
	      new Color(181,233,37),
	      new Color(236,177,85),
	      new Color(121,147,160),
	      new Color(234,218,110),
	      new Color(241,157,191),
	      new Color(62,200,234),
	      new Color(133,243,34),
	      new Color(88,149,110),
	      new Color(59,228,248),
	      new Color(183,119,118),
	      new Color(251,195,45),
	      new Color(113,196,122),
	      new Color(197,115,70),
	      new Color(80,175,187),
	      new Color(103,231,238),
	      new Color(240,72,133),
	      new Color(228,149,241),
	      new Color(180,188,159),
	      new Color(172,132,85),
	      new Color(180,135,251),
	      new Color(236,194,58),
	      new Color(217,176,109),
	      new Color(88,244,199),
	      new Color(186,157,239),
	      new Color(113,230,96),
	      new Color(206,115,165),
	      new Color(244,178,163),
	      new Color(230,139,26),
	      new Color(241,125,89),
	      new Color(83,160,66),
	      new Color(107,190,166),
	      new Color(197,161,210),
	      new Color(198,203,245),
	      new Color(238,117,19),
	      new Color(228,119,116),
	      new Color(131,156,41),
	      new Color(145,178,168),
	      new Color(139,170,220),
	      new Color(233,95,125),
	      new Color(87,178,230),
	      new Color(157,200,119),
	      new Color(237,140,76),
	      new Color(229,185,186),
	      new Color(144,206,212),
	      new Color(236,209,158),
	      new Color(185,189,79),
	      new Color(34,208,66),
	      new Color(84,238,129),
	      new Color(133,140,134),
	      new Color(67,157,94),
	      new Color(168,179,25),
	      new Color(140,145,240),
	      new Color(151,241,125),
	      new Color(67,162,107),
	      new Color(200,156,21),
	      new Color(169,173,189),
	      new Color(226,116,189),
	      new Color(133,231,191),
	      new Color(194,161,63),
	      new Color(241,77,99),
	      new Color(241,217,53),
	      new Color(123,204,105),
	      new Color(210,201,119),
	      new Color(229,108,155),
	      new Color(240,91,72),
	      new Color(187,115,210),
	      new Color(240,163,100),
	      new Color(178,217,57),
	      new Color(179,135,116),
	      new Color(204,211,24),
	      new Color(186,135,57),
	      new Color(223,176,135),
	      new Color(204,148,151),
	      new Color(116,223,50),
	      new Color(95,195,46),
	      new Color(123,160,236),
	      new Color(181,172,131),
	      new Color(142,220,202),
	      new Color(240,140,112),
	      new Color(172,145,164),
	      new Color(228,124,45),
	      new Color(135,151,243),
	      new Color(42,205,125),
	      new Color(192,233,116),
	      new Color(119,170,114),
	      new Color(158,138,26),
	      new Color(73,190,183),
	      new Color(185,229,243),
	      new Color(227,107,55),
	      new Color(196,205,202),
	      new Color(132,143,60),
	      new Color(233,192,237),
	      new Color(62,150,220),
	      new Color(205,201,141),
	      new Color(106,140,190),
	      new Color(161,131,205),
	      new Color(135,134,158),
	      new Color(198,139,81),
	      new Color(115,171,32),
	      new Color(101,181,67),
	      new Color(149,137,119),
	      new Color(37,142,183),
	      new Color(183,130,175),
	      new Color(168,125,133),
	      new Color(124,142,87),
	      new Color(236,156,171),
	      new Color(232,194,91),
	      new Color(219,200,69),
	      new Color(144,219,34),
	      new Color(219,95,187),
	      new Color(145,154,217),
	      new Color(165,185,100),
	      new Color(127,238,163),
	      new Color(224,178,198),
	      new Color(119,153,120),
	      new Color(124,212,92),
	      new Color(172,161,105),
	      new Color(231,155,135),
	      new Color(157,132,101),
	      new Color(122,185,146),
	      new Color(53,166,51),
	      new Color(70,163,90),
	      new Color(150,190,213),
	      new Color(210,107,60),
	      new Color(166,152,185),
	      new Color(159,194,159),
	      new Color(39,141,222),
	      new Color(202,176,161),
	      new Color(95,140,229),
	      new Color(168,142,87),
	      new Color(93,170,203),
	      new Color(159,142,54),
	      new Color(14,168,39),
	      new Color(94,150,149),
	      new Color(187,206,136),
	      new Color(157,224,166),
	      new Color(235,158,208),
	      new Color(109,232,216),
	      new Color(141,201,87),
	      new Color(208,124,118),
	      new Color(142,125,214),
	      new Color(19,237,174),
	      new Color(72,219,41),
	      new Color(234,102,111),
	      new Color(168,142,79),
	      new Color(188,135,35),
	      new Color(95,155,143),
	      new Color(148,173,116),
	      new Color(223,112,95),
	      new Color(228,128,236),
	      new Color(206,114,54),
	      new Color(195,119,88),
	      new Color(235,140,94),
	      new Color(235,202,125),
	      new Color(233,155,153),
	      new Color(214,214,238),
	      new Color(246,200,35),
	      new Color(151,125,171),
	      new Color(132,145,172),
	      new Color(131,142,118),
	      new Color(199,126,150),
	      new Color(61,162,123),
	      new Color(58,176,151),
	      new Color(215,141,69),
	      new Color(225,154,220),
	      new Color(220,77,167),
	      new Color(233,161,64),
	      new Color(130,221,137),
	      new Color(81,191,129),
	      new Color(169,162,140),
	      new Color(174,177,222),
	      new Color(236,174,47),
	      new Color(233,188,180),
	      new Color(69,222,172),
	      new Color(71,232,93),
	      new Color(118,211,238),
	      new Color(157,224,83),
	      new Color(218,105,73),
	      new Color(126,169,36)
	};
}
