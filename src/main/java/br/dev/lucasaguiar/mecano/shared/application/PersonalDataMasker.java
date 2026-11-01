package br.dev.lucasaguiar.mecano.shared.application;

/**
 * Mascara dados pessoais antes de irem para os logs (LGPD): o log identifica a pessoa para a equipe sem expor o
 * dado completo.
 */
public final class PersonalDataMasker {

	private static final String HIDDEN = "***";

	private PersonalDataMasker() {
	}

	/** "Maria Oliveira Santos" vira "Maria O. S.". */
	public static String name(String name) {
		if (name == null || name.isBlank()) {
			return HIDDEN;
		}
		var parts = name.trim().split("\\s+");
		var masked = new StringBuilder(parts[0]);
		for (var i = 1; i < parts.length; i++) {
			masked.append(' ').append(Character.toUpperCase(parts[i].charAt(0))).append('.');
		}
		return masked.toString();
	}

	/** "maria@email.com" vira "m***@email.com". */
	public static String email(String email) {
		if (email == null || email.isBlank()) {
			return HIDDEN;
		}
		var at = email.indexOf('@');
		if (at <= 0) {
			return HIDDEN;
		}
		return email.charAt(0) + HIDDEN + email.substring(at);
	}

	/** Mostra só os últimos caracteres de um código ou documento: "ABCDEFGHJKLM" vira "***JKLM". */
	public static String lastDigits(String value, int visible) {
		if (value == null || value.length() <= visible) {
			return HIDDEN;
		}
		return HIDDEN + value.substring(value.length() - visible);
	}
}
