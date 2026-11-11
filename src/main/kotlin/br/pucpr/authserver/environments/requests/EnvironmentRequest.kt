package br.pucpr.authserver.environments.requests

import jakarta.validation.constraints.NotBlank

data class EnvironmentRequest(
    @field:NotBlank
    val name: String
)
