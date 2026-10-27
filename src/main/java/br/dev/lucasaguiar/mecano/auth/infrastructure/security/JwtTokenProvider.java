package br.dev.lucasaguiar.mecano.auth.infrastructure.security;

import br.dev.lucasaguiar.mecano.auth.application.AccessToken;
import br.dev.lucasaguiar.mecano.auth.application.TokenProvider;
import br.dev.lucasaguiar.mecano.auth.domain.User;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * Emite tokens JWT assinados com HS256. O subject é o ID do usuário e o perfil vai na claim "roles".
 */
@Component
public class JwtTokenProvider implements TokenProvider {

	static final String ISSUER = "mecano";
	static final String ROLES_CLAIM = "roles";

	private final JwtEncoder jwtEncoder;
	private final JwtProperties properties;

	public JwtTokenProvider(JwtEncoder jwtEncoder, JwtProperties properties) {
		this.jwtEncoder = jwtEncoder;
		this.properties = properties;
	}

	@Override
	public AccessToken issue(User user) {
		var now = Instant.now();
		var claims = JwtClaimsSet.builder()
				.issuer(ISSUER)
				.subject(user.getId().value().toString())
				.issuedAt(now)
				.expiresAt(now.plus(properties.expiration()))
				.claim("email", user.getEmail())
				.claim(ROLES_CLAIM, List.of(user.getRole().name()))
				.build();
		var header = JwsHeader.with(MacAlgorithm.HS256).build();
		var token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new AccessToken(token, properties.expiration());
	}
}
