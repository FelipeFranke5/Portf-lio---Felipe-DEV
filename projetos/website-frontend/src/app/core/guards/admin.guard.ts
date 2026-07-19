import { CanActivateFn } from '@angular/router';

/**
 * Protege a rota /admin conforme descrito no fluxo de autenticação
 * do ARCHITECTURE.md: se não houver token, o usuário deve ser
 * redirecionado para o Keycloak.
 *
 * TODO: implementar a checagem real via AuthService (wrapper sobre
 * keycloak-angular) assim que a lógica de autenticação for construída.
 */
export const adminGuard: CanActivateFn = () => {
  return true;
};
