import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Injeta o token Bearer nas requisições autenticadas, conforme
 * descrito no fluxo de autenticação do ARCHITECTURE.md (passo 4).
 *
 * TODO: obter o token via AuthService (wrapper sobre keycloak-angular)
 * e anexar apenas nas requisições que exigem autenticação (rotas /admin).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req);
};
