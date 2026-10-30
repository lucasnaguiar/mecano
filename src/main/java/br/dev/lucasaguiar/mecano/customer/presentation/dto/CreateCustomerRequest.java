package br.dev.lucasaguiar.mecano.customer.presentation.dto;

import br.dev.lucasaguiar.mecano.customer.application.CustomerData;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastrar um cliente")
public record CreateCustomerRequest(
		@Schema(description = "Nome ou razão social", example = "Ana Souza")
		@NotBlank(message = "O nome é obrigatório.") @Size(max = 255, message = "O nome deve ter até 255 caracteres.")
		String name,

		@Schema(description = "CPF ou CNPJ, com ou sem pontuação", example = "529.982.247-25")
		@NotBlank(message = "O CPF ou CNPJ é obrigatório.")
		String taxId,

		@Schema(description = "E-mail para envio do orçamento", example = "ana@email.com")
		String email,

		@Schema(description = "Telefone com DDD", example = "(11) 99999-0000")
		String phone) {

	public CustomerData toData() {
		return new CustomerData(name, email, phone);
	}
}
