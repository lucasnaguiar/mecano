package br.dev.lucasaguiar.mecano.workorder.domain;

import br.dev.lucasaguiar.mecano.shared.domain.InvalidValueException;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Código aleatório e difícil de adivinhar com que o cliente consulta e aprova a OS sem acesso administrativo.
 * Não usa caracteres ambíguos (0, O, 1, I).
 */
public record TrackingCode(String value) {

	private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final int LENGTH = 12;
	private static final Pattern FORMAT = Pattern.compile("[" + ALPHABET + "]{" + LENGTH + "}");
	private static final SecureRandom RANDOM = new SecureRandom();

	public TrackingCode {
		if (value == null || value.isBlank()) {
			throw new InvalidValueException("O código de acompanhamento é obrigatório.");
		}
		value = value.trim().toUpperCase(Locale.ROOT);
		if (!FORMAT.matcher(value).matches()) {
			throw new InvalidValueException("Código de acompanhamento inválido.");
		}
	}

	public static TrackingCode generate() {
		var code = new StringBuilder(LENGTH);
		for (var i = 0; i < LENGTH; i++) {
			code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
		}
		return new TrackingCode(code.toString());
	}
}
