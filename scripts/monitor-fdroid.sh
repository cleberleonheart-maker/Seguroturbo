#!/usr/bin/env bash
# Verifica o status da submissão do SeguroTurbo no F-Droid (issue RFP #4599).
# Uso: ./scripts/monitor-fdroid.sh

set -euo pipefail

PROJETO="2167965"   # fdroid/rfp
ISSUE="4599"

JSON=$(curl -fsSL "https://gitlab.com/api/v4/projects/${PROJETO}/issues/${ISSUE}")

python3 - "$JSON" <<'PY'
import json, sys
from datetime import datetime, timezone

i = json.loads(sys.argv[1])

estados = {"opened": "ABERTA (aguardando revisão)", "closed": "FECHADA", "locked": "TRAVADA"}
estado = estados.get(i["state"], i["state"].upper())

print(f"Issue:      #{i['iid']} — {i['title']}")
print(f"URL:        {i['web_url']}")
print(f"Estado:     {estado}")
print(f"Criada:     {i['created_at'][:10]}")
print(f"Atualizada: {i['updated_at'][:10]}")
print(f"Labels:     {', '.join(i['labels']) or '(nenhuma)'}")
print(f"Comentários: {i.get('user_notes_count', 0)}")

if i.get("user_notes_count", 0) > 0:
    print("\n>>> HÁ RESPOSTAS DOS REVISORES! Acesse a issue acima. <<<")
elif i["state"] == "closed":
    print("\n>>> Issue fechada — confira se o app foi aceito. <<<")
else:
    print("\nSem novidades: ainda na fila de revisão.")
PY
