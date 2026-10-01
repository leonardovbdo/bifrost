# Postgres local — Bifrost (Docker Compose)

MVP de ops: **somente PostgreSQL**. A API Spring entra no Compose quando existir `apps/backend`.

## Subir

Na raiz do repositório:

```bash
docker compose up -d
```

## Conexão default

| Item | Valor |
|------|--------|
| Host | `localhost` |
| Port | `5432` |
| Database | `bifrost` |
| User | `bifrost` |
| Password | ver `.env` (default de dev em `.env.example`) |

URL JDBC típica:

```text
jdbc:postgresql://localhost:5432/bifrost
```

## Parar / dados

```bash
docker compose down        # mantém volume
docker compose down -v     # apaga dados do volume
```

## Nota

Não usar esta senha default fora de localhost. Produção/demo em rede: credenciais fortes via env (ver `conventions/security.md`).
