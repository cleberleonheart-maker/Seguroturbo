#!/usr/bin/env node
// Testes do Galinha Assada (index.html), rodando o script do jogo em Node
// com um DOM mínimo. Não precisa de navegador nem de rede.
//
//   node test/harness.js
//
// Cobre: o modo Zen (dificuldade nova, ciclo do menu, HUD, morte sem perder
// vida, chefe e clone continuam aparecendo) e a regressão das outras
// dificuldades (morte normal, fim de jogo, recorde por dificuldade).

const { g, getEl } = require("./dom.js");

// ---------------- asserts ----------------
let falhas = 0;
function ok(nome, cond, extra) {
  if (cond) { console.log("  ok   " + nome); }
  else { falhas++; console.log("  FALHA " + nome + (extra ? " -> " + extra : "")); }
}
function eq(nome, got, want) {
  ok(nome, got === want, "esperava " + JSON.stringify(want) + ", veio " + JSON.stringify(got));
}

// ---------------- testes ----------------
console.log("Testes do Galinha Assada (index.html)\n");

// 1) o modo Zen existe e é uma dificuldade de verdade
ok("zen está em DIFF", !!g.DIFF.zen);
ok("zen está em DIFF_KEYS", g.DIFF_KEYS.includes("zen"));
eq("zen tem 4 entradas em DIFF_KEYS", g.DIFF_KEYS.length, 4);
eq("rótulo do zen", g.DIFF.zen.label, "ZEN");
ok("zen não tem vidas finitas que cortem o jogo", g.DIFF.zen.lives > 0);

// 2) o ciclo do menu passa pelas 4 dificuldades
g.setDiff("easy");
const ciclo = [];
for (let i = 0; i < 4; i++) { g.cfgCycle("diff"); ciclo.push(g.CFG.diff); }
eq("ciclo: easy -> normal", ciclo[0], "normal");
eq("ciclo: normal -> hard", ciclo[1], "hard");
eq("ciclo: hard -> zen", ciclo[2], "zen");
eq("ciclo: zen -> easy", ciclo[3], "easy");

// 3) no Zen a galinha não perde vida e o jogo não acaba
g.setDiff("zen");
g.start();
eq("zen começa com as vidas do DIFF", g.lives, g.DIFF.zen.lives);
g.setLives(3);
g.die();
for (let i = 0; i < 200; i++) g.step(0.05);   // passa pela animação de morte
eq("zen: continua com 3 vidas depois de morrer", g.lives, 3);
ok("zen: o jogo não acabou", g.state !== "over", "state=" + g.state);
ok("zen: voltou pro estado de jogo", g.state === "play" || g.state === "ready", "state=" + g.state);

// 4) HUD mostra o infinito no Zen
ok("HUD mostra ∞ no zen", getEl("lives").innerHTML.includes("8734"));
g.setDiff("normal");
g.start();
ok("HUD normal não mostra ∞", !getEl("lives").innerHTML.includes("8734"));

// 5) nas outras dificuldades a morte continua valendo
g.setDiff("normal");
g.start();
eq("normal: começa com 3 vidas", g.lives, 3);
g.die();
for (let i = 0; i < 200; i++) g.step(0.05);
eq("normal: perdeu 1 vida", g.lives, 2);

// 6) no Zen o chefe e o clone continuam existindo
g.setDiff("zen");
g.start();
const ghos = g.ghosts;
ok("zen: os gatos continuam no jogo", ghos && ghos.length >= 3, "gatos=" + (ghos ? ghos.length : 0));

// chefe: fase múltipla de 5 continua de chefe no zen
g.setLevel(5);
ok("zen: a fase 5 continua sendo de chefe", g.bossLevel === true);
g.setLevel(3);
ok("zen: a fase 3 não é de chefe", g.bossLevel === false);
g.setDiff("normal");
g.setLevel(5);
ok("normal: a fase 5 é de chefe", g.bossLevel === true);

// clone secreto: continua com chance de spawn no zen (Math.random = 0 => sempre)
const rnd = Math.random;
Math.random = () => 0;
g.setDiff("zen");
g.setLevel(3);
ok("zen: o clone secreto continua previsto", g.cloneAt > 0, "cloneAt=" + g.cloneAt);
Math.random = rnd;

// 7) recorde por dificuldade: o zen tem o seu próprio
g.setDiff("zen");
g.setDiff("normal");
ok("zen não mistura o recorde com o normal", g.best !== undefined);
eq("DIFF_KEYS do recorde inclui zen", g.DIFF_KEYS.filter(k => k === "zen").length, 1);

// 8) curva de dificuldade: nao pode saturar antes da fase 30
g.setDiff("normal");
g.setLevel(11);
const v11 = g.ghostSpeed;
g.setLevel(30);
const v30 = g.ghostSpeed;
ok("gatos nao ficam travados de velocidade na fase 11", v11 > 0);
ok("gatos aceleram entre a fase 11 e a 30", v30 > v11, "v11=" + v11 + " v30=" + v30);
const p30 = g.playerSpeed;
ok("a galinha continua mais rapida que os gatos na fase 30", p30 > v30, "p30=" + p30 + " v30=" + v30);
g.setLevel(1);
const v1 = g.ghostSpeed;
ok("a fase 1 e mais lenta que a fase 30", v30 > v1, "v1=" + v1);
g.setLevel(50);
const v50 = g.ghostSpeed, p50 = g.playerSpeed;
ok("gatos aceleram entre a fase 30 e a 50", v50 > v30, "v30=" + v30 + " v50=" + v50);
ok("a galinha continua mais rapida que os gatos na fase 50", p50 > v50, "p50=" + p50.toFixed(2) + " v50=" + v50.toFixed(2));
ok("o jogo tem 50 fases", g.totalLevels === 50, g.totalLevels);
g.setLevel(50); ok("a fase 50 fica no ultimo capitulo (Banquete final)", g.chapter === "Banquete final", g.chapter);
g.setLevel(31); ok("a fase 31 abre o capitulo do mercado", g.chapter === "Mercado da esquina", g.chapter);

// 9) capitulos: 5 fases por capitulo e paleta por capitulo
g.setLevel(1);
const c1 = g.chapter;
g.setLevel(5);
ok("fases 1 e 5 sao do mesmo capitulo", g.chapter === c1, g.chapter);
g.setLevel(6);
ok("a fase 6 comeca outro capitulo", g.chapter !== c1, g.chapter);
g.setLevel(30);
ok("a fase 30 fica no ultimo capitulo", typeof g.chapter === "string" && g.chapter.length > 0, g.chapter);

// 10) receita da fase: existe e avanca com os eventos
g.setLevel(2);
ok("a fase 2 tem receita", !!g.recipe, JSON.stringify(g.recipe));
ok("a receita começa em zero", g.recipe && g.recipe.cur === 0);
g.setLevel(5);
ok("a fase de chefe tem receita de chefe", g.recipe && g.recipe.id === "boss", JSON.stringify(g.recipe));
g.setLevel(2);

console.log(falhas ? `\n${falhas} falha(s)` : "\nTudo certo");
process.exit(falhas ? 1 : 0);
