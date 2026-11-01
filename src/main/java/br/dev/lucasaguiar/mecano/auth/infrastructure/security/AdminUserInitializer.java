package br.dev.lucasaguiar.mecano.auth.infrastructure.security;

import br.dev.lucasaguiar.mecano.auth.application.RegisterUserCommand;
import br.dev.lucasaguiar.mecano.auth.application.RegisterUserUseCase;
import br.dev.lucasaguiar.mecano.auth.domain.Role;
import br.dev.lucasaguiar.mecano.auth.domain.User;
import br.dev.lucasaguiar.mecano.auth.domain.UserRepository;
import br.dev.lucasaguiar.mecano.shared.application.PersonalDataMasker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Cria o administrador inicial na subida da aplicação, se ADMIN_EMAIL e ADMIN_PASSWORD estiverem definidos.
 */
@Component
class AdminUserInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

	private final AdminUserProperties properties;
	private final UserRepository userRepository;
	private final RegisterUserUseCase registerUserUseCase;

	AdminUserInitializer(AdminUserProperties properties, UserRepository userRepository,
			RegisterUserUseCase registerUserUseCase) {
		this.properties = properties;
		this.userRepository = userRepository;
		this.registerUserUseCase = registerUserUseCase;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!StringUtils.hasText(properties.email()) || !StringUtils.hasText(properties.password())) {
			log.info("Administrador inicial não configurado (ADMIN_EMAIL/ADMIN_PASSWORD).");
			return;
		}
		if (userRepository.existsByEmail(User.normalizeEmail(properties.email()))) {
			return;
		}

		var name = StringUtils.hasText(properties.name()) ? properties.name() : "Administrador";
		registerUserUseCase.execute(new RegisterUserCommand(name, properties.email(), properties.password(), Role.ADMIN));
		log.info("Administrador inicial criado: {}", PersonalDataMasker.email(properties.email()));
	}
}
