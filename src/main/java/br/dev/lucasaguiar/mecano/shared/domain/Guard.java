package br.dev.lucasaguiar.mecano.shared.domain;

/**
 * Validações de entrada reutilizadas pelos modelos de domínio.
 */
public final class Guard {

	private Guard() {
	}

	public static <T> T requireNonNull(T value, String message) {
		if (value == null) {
			throw new InvalidValueException(message);
		}
		return value;
	}

	public static String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new InvalidValueException(message);
		}
		return value.trim();
	}

	public static String optionalText(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
