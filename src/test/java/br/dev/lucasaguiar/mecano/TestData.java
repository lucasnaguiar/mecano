package br.dev.lucasaguiar.mecano;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Gera documentos, placas e códigos únicos e válidos para os testes.
 */
public final class TestData {

	private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private TestData() {
	}

	public static String randomCpf() {
		var random = ThreadLocalRandom.current();
		var digits = new int[11];
		do {
			for (var i = 0; i < 9; i++) {
				digits[i] = random.nextInt(10);
			}
		} while (allEqual(digits));
		digits[9] = cpfCheckDigit(digits, 9);
		digits[10] = cpfCheckDigit(digits, 10);
		var cpf = new StringBuilder();
		for (var digit : digits) {
			cpf.append(digit);
		}
		return cpf.toString();
	}

	public static String randomPlate() {
		var random = ThreadLocalRandom.current();
		var plate = new StringBuilder();
		for (var i = 0; i < 3; i++) {
			plate.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
		}
		for (var i = 0; i < 4; i++) {
			plate.append(random.nextInt(10));
		}
		return plate.toString();
	}

	public static String randomCode(String prefix) {
		return prefix + "-" + Long.toString(ThreadLocalRandom.current().nextLong(1L << 40), 36).toUpperCase();
	}

	private static int cpfCheckDigit(int[] digits, int length) {
		var sum = 0;
		for (var i = 0; i < length; i++) {
			sum += digits[i] * (length + 1 - i);
		}
		var rest = sum * 10 % 11;
		return rest == 10 ? 0 : rest;
	}

	private static boolean allEqual(int[] digits) {
		for (var i = 1; i < 9; i++) {
			if (digits[i] != digits[0]) {
				return false;
			}
		}
		return true;
	}
}
