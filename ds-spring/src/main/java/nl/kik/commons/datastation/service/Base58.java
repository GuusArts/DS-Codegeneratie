package nl.kik.commons.datastation.service;

import java.util.Arrays;

public class Base58 {
	private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
	private static final int BASE_58 = Base58.ALPHABET.length;
	private static final int BASE_256 = 256;

	private static final int[] INDEXES = new int[128];
	static {
		Arrays.fill(Base58.INDEXES, -1);
		for (int i = 0; i < Base58.ALPHABET.length; i++) {
			Base58.INDEXES[Base58.ALPHABET[i]] = i;
		}
	}

	private static byte[] copyOfRange(final byte[] source, final int from, final int to) {
		final byte[] range = new byte[to - from];
		System.arraycopy(source, from, range, 0, range.length);

		return range;
	}

	public static byte[] decode(final String input) {
		if (input.length() == 0)
			return new byte[0];

		final byte[] input58 = new byte[input.length()];
		for (int i = 0; i < input.length(); ++i) {
			final char c = input.charAt(i);

			int digit58 = -1;
			if (c >= 0 && c < 128) {
				digit58 = Base58.INDEXES[c];
			}
			if (digit58 < 0)
				throw new RuntimeException("Not a Base58 input: " + input);

			input58[i] = (byte) digit58;
		}

		int zeroCount = 0;
		while (zeroCount < input58.length && input58[zeroCount] == 0) {
			++zeroCount;
		}

		final byte[] temp = new byte[input.length()];
		int j = temp.length;

		int startAt = zeroCount;
		while (startAt < input58.length) {
			final byte mod = Base58.divmod256(input58, startAt);
			if (input58[startAt] == 0) {
				++startAt;
			}

			temp[--j] = mod;
		}

		while (j < temp.length && temp[j] == 0) {
			++j;
		}

		return Base58.copyOfRange(temp, j - zeroCount, temp.length);
	}

	private static byte divmod256(final byte[] number58, final int startAt) {
		int remainder = 0;
		for (int i = startAt; i < number58.length; i++) {
			final int digit58 = number58[i] & 0xFF;
			final int temp = remainder * Base58.BASE_58 + digit58;

			number58[i] = (byte) (temp / Base58.BASE_256);

			remainder = temp % Base58.BASE_256;
		}

		return (byte) remainder;
	}

	private static byte divmod58(final byte[] number, final int startAt) {
		int remainder = 0;
		for (int i = startAt; i < number.length; i++) {
			final int digit256 = number[i] & 0xFF;
			final int temp = remainder * Base58.BASE_256 + digit256;

			number[i] = (byte) (temp / Base58.BASE_58);

			remainder = temp % Base58.BASE_58;
		}

		return (byte) remainder;
	}

	public static String encode(byte[] input) {
		if (input.length == 0)
			return "";
		input = Base58.copyOfRange(input, 0, input.length);
		int zeroCount = 0;
		while (zeroCount < input.length && input[zeroCount] == 0) {
			++zeroCount;
		}
		final byte[] temp = new byte[input.length * 2];
		int j = temp.length;
		int startAt = zeroCount;
		while (startAt < input.length) {
			final byte mod = Base58.divmod58(input, startAt);
			if (input[startAt] == 0) {
				++startAt;
			}

			temp[--j] = (byte) Base58.ALPHABET[mod];
		}

		while (j < temp.length && temp[j] == Base58.ALPHABET[0]) {
			++j;
		}

		while (--zeroCount >= 0) {
			temp[--j] = (byte) Base58.ALPHABET[0];
		}

		final byte[] output = Base58.copyOfRange(temp, j, temp.length);
		return new String(output);
	}
}