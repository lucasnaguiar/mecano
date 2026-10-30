package br.dev.lucasaguiar.mecano.customer.presentation.dto;

import br.dev.lucasaguiar.mecano.customer.application.CustomerData;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados editáveis do cliente (o documento não muda)")
public record UpdateCustomerRequest(
		@Schema(description = "Nome ou razão social")
		@NotBlank(message = "O nome é obrigatório.") @Size(max = 255, message = "O nome deve ter até 255 caracteres.")
		String name,

		@Schema(description = "E-mail para envio do orçamento")
		String email,

		@Schema(description = "Telefone com DDD")
		String phone) {

	public CustomerData toData() {
		return new CustomerData(name, email, phone);
	}
}
