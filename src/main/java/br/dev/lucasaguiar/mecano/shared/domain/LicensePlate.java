package br.dev.lucasaguiar.mecano.shared.domain;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Placa do veículo, no formato antigo (ABC-1234) ou Mercosul (ABC1D23). Armazenada sem hífen e em maiúsculas.
 */
public record LicensePlate(String value) {

	private static final Pattern FORMAT = Pattern.compile("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}");

	public LicensePlate {
		if (value == null || value.isBlank()) {
			throw new InvalidValueException("A placa é obrigatória.");
		}
		value = value.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
		if (!FORMAT.matcher(value).matches()) {
			throw new InvalidValueException("Placa inválida. Use o formato antigo (ABC-1234) ou Mercosul (ABC1D23).");
		}
	}

	public boolean isMercosul() {
		return Character.isLetter(value.charAt(4));
	}
}
