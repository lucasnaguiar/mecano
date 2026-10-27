package br.dev.lucasaguiar.mecano.auth.presentation;

import br.dev.lucasaguiar.mecano.auth.application.InvalidCredentialsException;
import br.dev.lucasaguiar.mecano.auth.domain.EmailAlreadyRegisteredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AuthExceptionHandler.class)
class AuthExceptionHandler {

	@ExceptionHandler(InvalidCredentialsException.class)
	ProblemDetail handleInvalidCredentials(InvalidCredentialsException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
	}

	@ExceptionHandler(EmailAlreadyRegisteredException.class)
	ProblemDetail handleEmailAlreadyRegistered(EmailAlreadyRegisteredException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
	}
}
