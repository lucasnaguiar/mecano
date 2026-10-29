package br.dev.lucasaguiar.mecano.shared.domain;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Documento fiscal do cliente: CPF ou CNPJ (inclusive o alfanumérico), com dígitos verificadores válidos.
 * Armazenado sem pontuação e em maiúsculas.
 */
public record TaxId(String value) {

	private static final Pattern CPF = Pattern.compile("\\d{11}");
	private static final Pattern CNPJ = Pattern.compile("[0-9A-Z]{12}\\d{2}");

	public TaxId {
		if (value == null || value.isBlank()) {
			throw new InvalidValueException("O CPF ou CNPJ é obrigatório.");
		}
		value = value.replaceAll("[.\\-/\\s]", "").toUpperCase(Locale.ROOT);
		var valid = CPF.matcher(value).matches() ? isValidCpf(value) : CNPJ.matcher(value).matches() && isValidCnpj(value);
		if (!valid) {
			throw new InvalidValueException("CPF ou CNPJ inválido.");
		}
	}

	public boolean isCpf() {
		return value.length() == 11;
	}

	public boolean isCnpj() {
		return value.length() == 14;
	}

	private static boolean isValidCpf(String cpf) {
		return !allSameCharacter(cpf)
				&& cpfCheckDigit(cpf, 9) == cpf.charAt(9) - '0'
				&& cpfCheckDigit(cpf, 10) == cpf.charAt(10) - '0';
	}

	private static int cpfCheckDigit(String cpf, int length) {
		var sum = 0;
		for (var i = 0; i < length; i++) {
			sum += (cpf.charAt(i) - '0') * (length + 1 - i);
		}
		var rest = sum * 10 % 11;
		return rest == 10 ? 0 : rest;
	}

	private static boolean isValidCnpj(String cnpj) {
		return !allSameCharacter(cnpj)
				&& cnpjCheckDigit(cnpj, 12) == cnpj.charAt(12) - '0'
				&& cnpjCheckDigit(cnpj, 13) == cnpj.charAt(13) - '0';
	}

	/**
	 * Cada caractere vale seu código ASCII menos 48 (regra do CNPJ alfanumérico, compatível com o numérico).
	 */
	private static int cnpjCheckDigit(String cnpj, int length) {
		var sum = 0;
		var weight = 2;
		for (var i = length - 1; i >= 0; i--) {
			sum += (cnpj.charAt(i) - '0') * weight;
			weight = weight == 9 ? 2 : weight + 1;
		}
		var rest = sum % 11;
		return rest < 2 ? 0 : 11 - rest;
	}

	private static boolean allSameCharacter(String value) {
		return value.chars().distinct().count() == 1;
	}
}
