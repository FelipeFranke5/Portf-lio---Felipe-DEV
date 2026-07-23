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
    │   ├── header/          # app-header: cabeçalho padrão com navegação (Início/Portfólio/Habilidades/Contato)
    │   └── footer/          # app-footer: rodapé padrão com copyright
    ├── pipes/
    └── directives/
```

Cada rota de feature é carregada via lazy loading (`loadComponent` /
`loadChildren`), para não inflar o bundle inicial com telas que o visitante
comum nunca vai acessar (ex.: toda a área `admin`).

## Status atual

Bootstrap da aplicação criado (estrutura de pastas, roteamento, guard e
interceptor stub). A maioria dos componentes de feature ainda são standalone
vazios com `TODO`. **Nenhuma chamada real à API ou integração com Keycloak foi
implementada ainda** — isso fica para os próximos passos.

`HomeComponent` já possui conteúdo estático (hero/about) e utiliza os novos
componentes padrão `HeaderComponent` (`app-header`) e `FooterComponent`
(`app-footer`), criados em `shared/components/`. Esses dois componentes são
standalone e reaproveitáveis — qualquer outra feature pode importá-los e
incluir `<app-header>`/`<app-footer>` no próprio template.

`FooterComponent` também exibe links para as redes sociais (LinkedIn, GitHub
e Instagram), com ícones SVG inline. As URLs (`linkedinUrl`, `githubUrl`,
`instagramUrl` em `footer.component.ts`) estão com valores placeholder e
precisam ser preenchidas manualmente com os perfis reais.

Os três blocos de conteúdo do `HomeComponent` (introdução, Sobre Mim e
Primeiros passos na programação) são exibidos como um carrossel — apenas um
trecho fica visível por vez, com transição animada (`transform: translateX`)
entre eles. Navegação via botões anterior/próximo (`previousSlide()` /
`nextSlide()`) ou diretamente pelos indicadores/dots (`goToSlide(index)`).
Cada trecho tem cor de fundo e de fonte próprias, definidas em
`home.component.scss`. O card do carrossel tem largura máxima de 960px e
altura mínima de 560px (antes 720px/420px), os títulos `h1`/`h2` de cada
trecho são centralizados, e o GIF do trecho de introdução é exibido com
tamanho fixo (420x260px, `object-fit: cover`) para caber de forma adequada
no carrossel.

## Rodando localmente

```bash
npm install
ng serve
```

Depende do back-end e do Keycloak estarem no ar.
