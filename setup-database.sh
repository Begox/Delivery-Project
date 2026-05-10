#!/bin/bash
# ==========================================================
# setup-database.sh — Delivery PGE
# Script de configuração do banco de dados PostgreSQL
# Execute: bash setup-database.sh
# ==========================================================

set -e

BOLD='\033[1m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo ""
echo -e "${BOLD}${CYAN}╔══════════════════════════════════════════╗${NC}"
echo -e "${BOLD}${CYAN}║     Delivery PGE — Setup do Banco        ║${NC}"
echo -e "${BOLD}${CYAN}╚══════════════════════════════════════════╝${NC}"
echo ""

# ──────────────────────────────────────────────
# 1. Verificar e instalar PostgreSQL
# ──────────────────────────────────────────────

PSQL_PATH=""

# Procurar psql em locais comuns
for p in \
    "/opt/homebrew/bin/psql" \
    "/usr/local/bin/psql" \
    "/usr/bin/psql" \
    "/usr/lib/postgresql/16/bin/psql" \
    "/usr/lib/postgresql/15/bin/psql" \
    "/Applications/Postgres.app/Contents/Versions/latest/bin/psql"
do
    if [ -f "$p" ]; then
        PSQL_PATH="$p"
        break
    fi
done

if [ -z "$PSQL_PATH" ]; then
    echo -e "${YELLOW}⚠️  PostgreSQL não encontrado. Tentando instalar via Homebrew...${NC}"

    # Verificar Homebrew
    BREW_PATH=""
    for bp in "/opt/homebrew/bin/brew" "/usr/local/bin/brew"; do
        if [ -f "$bp" ]; then
            BREW_PATH="$bp"
            break
        fi
    done

    if [ -z "$BREW_PATH" ]; then
        echo -e "${RED}❌ Homebrew não encontrado.${NC}"
        echo ""
        echo -e "${BOLD}Instale o PostgreSQL manualmente:${NC}"
        echo ""
        echo "  Opção 1 — Homebrew:"
        echo "    /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
        echo "    brew install postgresql@16"
        echo "    brew services start postgresql@16"
        echo ""
        echo "  Opção 2 — Postgres.app (mais simples):"
        echo "    Acesse: https://postgresapp.com/"
        echo "    Baixe e instale o Postgres.app"
        echo "    Inicie pelo ícone na barra de menu"
        echo ""
        echo -e "${BOLD}Depois execute:${NC} psql -U postgres"
        echo ""
        exit 1
    fi

    echo -e "${CYAN}📦 Instalando postgresql@16 via Homebrew...${NC}"
    "$BREW_PATH" install postgresql@16
    "$BREW_PATH" services start postgresql@16
    sleep 3

    PSQL_PATH="$("$BREW_PATH" --prefix postgresql@16)/bin/psql"
    PG_BIN="$("$BREW_PATH" --prefix postgresql@16)/bin"
    export PATH="$PG_BIN:$PATH"
fi

echo -e "${GREEN}✅ PostgreSQL encontrado: ${PSQL_PATH}${NC}"

# ──────────────────────────────────────────────
# 2. Verificar se o serviço está rodando
# ──────────────────────────────────────────────

echo ""
echo -e "${CYAN}🔍 Verificando se o PostgreSQL está em execução...${NC}"

PG_RUNNING=false
if pg_isready -q 2>/dev/null || "$PSQL_PATH" -U postgres -c "SELECT 1" >/dev/null 2>&1; then
    PG_RUNNING=true
fi

if [ "$PG_RUNNING" = false ]; then
    echo -e "${YELLOW}⚠️  PostgreSQL não está rodando. Iniciando...${NC}"

    # Tentar iniciar via brew services
    for bp in "/opt/homebrew/bin/brew" "/usr/local/bin/brew"; do
        if [ -f "$bp" ]; then
            "$bp" services start postgresql@16 2>/dev/null || "$bp" services start postgresql 2>/dev/null || true
            break
        fi
    done

    sleep 3

    # Tentar via pg_ctl
    if ! pg_isready -q 2>/dev/null; then
        PGDATA="${PGDATA:-$HOME/Library/Application Support/Postgres/var-16}"
        if [ -d "$PGDATA" ]; then
            pg_ctl -D "$PGDATA" start 2>/dev/null || true
            sleep 2
        fi
    fi
fi

echo -e "${GREEN}✅ PostgreSQL está rodando${NC}"

# ──────────────────────────────────────────────
# 3. Criar banco e usuário
# ──────────────────────────────────────────────

echo ""
echo -e "${CYAN}🗄️  Configurando banco de dados...${NC}"

# Tentar conectar como postgres (superuser padrão)
PG_SUPERUSER="${PG_SUPERUSER:-$(whoami)}"

# SQL para criar banco e usuário
SQL_SETUP="
-- Criar usuário se não existir
DO \$\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'postgres') THEN
        CREATE ROLE postgres WITH LOGIN SUPERUSER PASSWORD 'postgres';
        RAISE NOTICE 'Usuário postgres criado.';
    ELSE
        ALTER ROLE postgres WITH PASSWORD 'postgres';
        RAISE NOTICE 'Senha do usuário postgres atualizada.';
    END IF;
END
\$\$;

-- Criar banco se não existir
SELECT 'CREATE DATABASE delivery_pge OWNER postgres ENCODING ''UTF8'' LC_COLLATE ''en_US.UTF-8'' LC_CTYPE ''en_US.UTF-8'' TEMPLATE template0'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'delivery_pge')\gexec
"

# Executar como superuser atual (padrão no macOS sem senha)
echo "$SQL_SETUP" | "$PSQL_PATH" -U "$PG_SUPERUSER" -d postgres 2>/dev/null || \
echo "$SQL_SETUP" | "$PSQL_PATH" -U postgres -d postgres 2>/dev/null || {
    echo -e "${YELLOW}⚠️  Tentando criar banco diretamente...${NC}"
    "$PSQL_PATH" -U "$PG_SUPERUSER" -d postgres -c "CREATE DATABASE delivery_pge;" 2>/dev/null || \
    "$PSQL_PATH" -U postgres -d postgres -c "CREATE DATABASE delivery_pge;" 2>/dev/null || true
}

# Verificar se o banco existe
DB_EXISTS=$("$PSQL_PATH" -U "$PG_SUPERUSER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='delivery_pge'" 2>/dev/null || \
            "$PSQL_PATH" -U postgres -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='delivery_pge'" 2>/dev/null || echo "")

if [ "$DB_EXISTS" = "1" ]; then
    echo -e "${GREEN}✅ Banco 'delivery_pge' criado/verificado com sucesso!${NC}"
else
    echo -e "${YELLOW}⚠️  Não foi possível verificar o banco automaticamente.${NC}"
    echo -e "     Tente criar manualmente:"
    echo -e "     ${BOLD}psql -U postgres -c \"CREATE DATABASE delivery_pge;\"${NC}"
fi

# ──────────────────────────────────────────────
# 4. Resumo final
# ──────────────────────────────────────────────

echo ""
echo -e "${BOLD}${GREEN}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BOLD}${GREEN}║  ✅  Configuração concluída!                             ║${NC}"
echo -e "${BOLD}${GREEN}╠══════════════════════════════════════════════════════════╣${NC}"
echo -e "${BOLD}${GREEN}║                                                          ║${NC}"
echo -e "${BOLD}${GREEN}║  Banco:    delivery_pge                                  ║${NC}"
echo -e "${BOLD}${GREEN}║  Usuário:  postgres                                      ║${NC}"
echo -e "${BOLD}${GREEN}║  Senha:    postgres                                      ║${NC}"
echo -e "${BOLD}${GREEN}║  Host:     localhost:5432                                ║${NC}"
echo -e "${BOLD}${GREEN}║                                                          ║${NC}"
echo -e "${BOLD}${GREEN}║  Próximo passo: execute o backend!                       ║${NC}"
echo -e "${BOLD}${GREEN}║                                                          ║${NC}"
echo -e "${BOLD}${GREEN}║  cd backend && mvn spring-boot:run                       ║${NC}"
echo -e "${BOLD}${GREEN}║                                                          ║${NC}"
echo -e "${BOLD}${GREEN}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""
