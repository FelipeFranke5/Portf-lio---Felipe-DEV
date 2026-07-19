# website-frontend

Front-end Angular (standalone components) do Website Pessoal, conforme
`ARCHITECTURE.md` do repositório principal.

## Stack

Angular 17+, TypeScript, `keycloak-angular` para autenticação OIDC contra o
Keycloak. Servido em produção como estático pelo NGINX, que também faz proxy
de `/api/*` para o back-end Spring Boot e `/auth/*` para o Keycloak.

## Estrutura

```
src/app/
├── core/           # transversal: guards, interceptors, services de baixo nível
│   ├── guards/          # admin.guard.ts -> protege /admin
│   ├── interceptors/    # auth.interceptor.ts -> injeta Bearer token
│   ├── services/         # api.service.ts (cliente HTTP), auth.service.ts (wrapper Keycloak)
│   └── models/           # interfaces/DTOs do front (espelham os DTOs do back)
│
├── features/        # uma pasta por seção pública do site + área admin separada
│   ├── home/             # hero/about (dados estáticos)
│   ├── portfolio/        # projetos (GET público / CRUD admin)
│   ├── skills/            # skills (GET público / CRUD admin)
│   ├── contact/           # formulário de contato (POST público)
│   └── admin/              # CRUD protegido, sub-rotas próprias (admin.routes.ts)
│       ├── projects/
│       ├── skills/
│       └── logs/           # Log Interno (diagnóstico)
│
└── shared/          # componentes, pipes e diretivas reaproveitáveis entre features
    ├── components/
    ├── pipes/
    └── directives/
```

Cada rota de feature é carregada via lazy loading (`loadComponent` /
`loadChildren`), para não inflar o bundle inicial com telas que o visitante
comum nunca vai acessar (ex.: toda a área `admin`).

## Status atual

Apenas o bootstrap da aplicação foi criado (estrutura de pastas, roteamento,
componentes standalone vazios com `TODO`, guard e interceptor stub). **Nenhuma
lógica de negócio, chamada real à API ou integração com Keycloak foi
implementada ainda** — isso fica para os próximos passos.

## Rodando localmente

```bash
npm install
ng serve
```

Depende do back-end e do Keycloak estarem no ar.
