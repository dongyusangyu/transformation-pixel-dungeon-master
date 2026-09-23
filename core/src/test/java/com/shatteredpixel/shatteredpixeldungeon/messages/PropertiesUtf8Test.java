package com.shatteredpixel.shatteredpixeldungeon.messages;

import org.junit.Test;

import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PropertiesUtf8Test {

	@Test
	public void projectPropertiesAreUtf8WithoutBomOrUnicodeEscapes() throws Exception {
		Path root = projectRoot();
		List<Path> propertyFiles = new ArrayList<>();
		for (String path : new String[]{"gradle.properties", "local.properties", "ios/robovm.properties"}) {
			Path file = root.resolve(path);
			if (Files.isRegularFile(file)) propertyFiles.add(file);
		}
		try (Stream<Path> files = Files.walk(root.resolve("gradle"))) {
			files.filter(path -> path.toString().endsWith(".properties"))
					.forEach(propertyFiles::add);
		}
		try (Stream<Path> files = Files.walk(root.resolve("core/src/main/assets/messages"))) {
			files.filter(path -> path.toString().endsWith(".properties"))
					.forEach(propertyFiles::add);
		}

		assertFalse(propertyFiles.isEmpty());
		for (Path file : propertyFiles) {
			byte[] bytes = Files.readAllBytes(file);
			assertFalse(file + " has a UTF-8 BOM", hasUtf8Bom(bytes));
			String text = decodeUtf8(file, bytes);
			assertFalse(file + " still contains escaped Unicode", hasUnicodeEscape(text));
		}

		String editorConfig = new String(Files.readAllBytes(root.resolve(".editorconfig")), StandardCharsets.UTF_8);
		assertTrue(editorConfig.contains("[*.properties]\ncharset = utf-8"));
	}

	@Test
	public void utf8ReaderLoadsChineseJapaneseAndKoreanText() throws Exception {
		Properties chinese = loadProperties(projectRoot().resolve(
				"core/src/main/assets/messages/custom/custom_zh.properties"));
		Properties japanese = loadProperties(projectRoot().resolve(
				"core/src/main/assets/messages/actors/actors_ja.properties"));
		Properties korean = loadProperties(projectRoot().resolve(
				"core/src/main/assets/messages/actors/actors_ko.properties"));
		Properties french = loadProperties(projectRoot().resolve(
				"core/src/main/assets/messages/actors/actors_fr.properties"));

		assertEquals("奥术炸弹", chinese.getProperty("custom.dict.dict.bomb_arcane"));
		assertEquals("吹雪", japanese.getProperty("actors.blobs.blizzard.name"));
		assertEquals("뇌진폭", korean.getProperty("actors.blobs.blizzard.name"));
		assertTrue(french.getProperty("actors.mobs.dm300.desc_supercharged").contains("énergie_　;"));
	}

	private static Properties loadProperties(Path file) throws Exception {
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		return properties;
	}

	private static String decodeUtf8(Path file, byte[] bytes) throws CharacterCodingException {
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT);
		return decoder.decode(ByteBuffer.wrap(bytes)).toString();
	}

	private static boolean hasUtf8Bom(byte[] bytes) {
		return bytes.length >= 3
				&& (bytes[0] & 0xFF) == 0xEF
				&& (bytes[1] & 0xFF) == 0xBB
				&& (bytes[2] & 0xFF) == 0xBF;
	}

	private static boolean hasUnicodeEscape(String text) {
		for (int i = 0; i < text.length();) {
			if (text.charAt(i) != '\\') {
				i++;
				continue;
			}

			int start = i;
			while (i < text.length() && text.charAt(i) == '\\') i++;
			int slashCount = i - start;
			if ((slashCount & 1) == 1 && i < text.length() && text.charAt(i) == 'u'
					&& i + 4 < text.length() && isHex(text, i + 1, i + 5)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isHex(String text, int start, int end) {
		for (int i = start; i < end; i++) {
			if (Character.digit(text.charAt(i), 16) < 0) return false;
		}
		return true;
	}

	private static Path projectRoot() {
		Path working = Paths.get(System.getProperty("user.dir"));
		return Files.isDirectory(working.resolve("core")) ? working : working.getParent();
	}
}
