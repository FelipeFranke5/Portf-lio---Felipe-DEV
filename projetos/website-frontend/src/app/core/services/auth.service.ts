import { Injectable } from '@angular/core';

/**
 * Wrapper de autenticação sobre o keycloak-angular, conforme descrito
 * na seção "core" do ARCHITECTURE.md.
 *
 * TODO: implementar login/logout/isAuthenticated/hasRole('ADMIN') e a
 * obtenção do token JWT delegando para o Keycloak (fluxo OIDC descrito
 * em "Fluxo de autenticação (admin)" no ARCHITECTURE.md).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {}
