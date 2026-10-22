#!/usr/bin/env node
// Caça-bugs: roda o jogo por muitas fases e checa invariantes.
//   node test/bugs.js
//
// Não é teste de comportamento (isso é o harness.js): aqui o foco é
// encontrar estados inválidos depois de muita partida: NaN, fora do
// labirinto, contadores quebrados, receita inconsistente, etc.

const { g } = require("./dom.js");



let falhas = 0;
function check(nome, cond, extra) {
  if (!cond) { falhas++; console.log("  FALHA " + nome + (extra ? " -> " + extra : "")); }
}
const num = (v) => typeof v === "number" && isFinite(v);

// 1) muitas fases em todas as dificuldades
for (const diff of ["normal", "zen"]) {
  g.setDiff(diff);
  for (let lv = 1; lv <= 30; lv++) {
    g.start();
    g.setLevel(lv);
    for (let i = 0; i < 1200; i++) {
      g.step(0.033);
      if (!num(g.score)) { check("score virou NaN", false, "diff=" + diff + " fase=" + lv + " i=" + i); break; }
      if (!num(g.lives)) { check("vidas viraram NaN", false, "diff=" + diff + " fase=" + lv); break; }
    }
    const r = g.recipe;
    if (r) {
      check("receita tem numero valido", num(r.cur) && num(r.need) && r.need > 0, JSON.stringify(r));
      check("receita nao passa do alvo", r.cur <= r.need, JSON.stringify(r));
      check("receita coerente com a fase", r.id === "boss" ? lv % 5 === 0 : lv % 5 !== 0,
        "fase " + lv + " receita " + r.id);
    }
    check("fase continua a mesma", g.level === lv, "fase " + g.level + " vs " + lv);
  }
}

// 2) a galinha e os gatos nunca ficam dentro da parede
g.setDiff("normal");
g.setLevel(12);
g.start();
for (let i = 0; i < 3000; i++) {
  g.step(0.033);
  const p = g.player;
  if (!p) continue;
  check("galinha dentro do tabuleiro", p.r >= 0 && p.r < 30 && p.c >= 0 && p.c < 30, p.r + "," + p.c);
  check("galinha em tile valido", num(p.r) && num(p.c) && p.r >= 0 && p.c >= 0, JSON.stringify(p));
}

// 3) morre e continua: as vidas caem, o jogo nao trava
g.setLevel(3);
g.start();
const v0 = g.lives;
for (let k = 0; k < 5; k++){ g.die(); for (let i = 0; i < 250; i++) g.step(0.033); }
check("vidas diminuem ao morrer", g.lives <= v0, v0 + " -> " + g.lives);
check("vidas nao ficam negativos", g.lives >= 0, g.lives);

// 4) a receita completa e soma o bonus
g.setLevel(2);
const need = g.recipe ? g.recipe.need : 0;
const antes = g.score;
if (g.recipe) g.bumpRecipe(g.recipe.id, g.recipe.need);
check("receita marca como pronta", !g.recipe || g.recipe.done === true || g.recipe.cur >= g.recipe.need, JSON.stringify(g.recipe));
check("receita vale 500 pontos", g.score - antes >= 500, "ganhou " + (g.score - antes) + " de " + need);

// 5)章 capitulos: 6 capitulos, 5 fases cada, sem buraco
g.setLevel(1); const c1 = g.chapter;
g.setLevel(5); check("fases 1-5 no mesmo capitulo", g.chapter === c1, g.chapter);
g.setLevel(6); check("fase 6 em outro capitulo", g.chapter !== c1, g.chapter);
g.setLevel(30); check("fase 30 em algum capitulo", !!g.chapter, g.chapter);
g.setLevel(30); check("fase 30 e a ultima", g.chapter === (g.setLevel(30), g.chapter), "");

// 6) velocidades crescem e a galinha nunca fica mais lenta que os gatos
g.setDiff("normal");
let anterior = 0;
for (let lv = 1; lv <= 30; lv++) {
  g.setLevel(lv);
  check("gatos nao voltam a ficar lentos na fase " + lv, g.ghostSpeed > 0, g.ghostSpeed);
  check("galinha mais rapida que os gatos na fase " + lv, g.playerSpeed > g.ghostSpeed,
    "fase " + lv + " galinha " + g.playerSpeed.toFixed(2) + " gatos " + g.ghostSpeed.toFixed(2));
  check("velocidade cresce na fase " + lv, lv === 1 || g.ghostSpeed >= anterior,
    "fase " + lv + " " + g.ghostSpeed.toFixed(2) + " vs " + anterior.toFixed(2));
  anterior = g.ghostSpeed;
}

// 7) o atualizador: probe barato, URL valida e download do jogo novo
(async function(){
  const realFetch = global.fetch;
  global.AndroidUpdate = { saveContent(){}, resetContent(){} };   // ponte do app
  let urls = [];
  let shaAtual = "sha-novo-1";
  global.fetch = async function(u){
    urls.push(String(u));
    const s = String(u);
    if (s.indexOf("api.github.com") >= 0 && s.indexOf("commits") >= 0)
      return { ok:true, status:200, json: async () => [{ sha:shaAtual }] };
    if (s.indexOf("api.github.com") >= 0 && s.indexOf("contents") >= 0){
      const novo = '<html><script>const GAME_BUILD = "9999-01-01.1";</' + 'script></html>';
      return { ok:true, status:200, text: async () => JSON.stringify({ encoding:"base64", content: btoa(novo) }) };
    }
    return { ok:false, status:404, text: async () => "" };
  };
  const upd = await g.checkContentUpdate();
  check("o atualizador achou a versao nova", upd && upd.build === "9999-01-01.1", JSON.stringify(upd));
  check("a url da API nao ficou com dois ?", !urls.some(u => u.indexOf("main?t=") >= 0), urls.join(" | "));
  const baixou = urls.filter(u => u.indexOf("contents") >= 0).length;
  check("baixou o jogo so uma vez", baixou === 1, "baixou " + baixou);
  // segunda vez: o probe diz que nada mudou, nao baixa de novo
  urls = [];
  const upd2 = await g.checkContentUpdate();
  check("sem novidade, o atualizador nao baixa o jogo", urls.filter(u => u.indexOf("contents") >= 0).length === 0, urls.join(" | "));
  check("sem novidade, nao retorna nada", upd2 === null, JSON.stringify(upd2));
  // 8) a atualizacao automatica nao pode recarregar no meio da partida
  let salvos = 0;
  global.AndroidUpdate = { saveContent(){ salvos++; } };
  shaAtual = "sha-novo-2";                 // o repositorio mudou de novo
  g.start();                                   // jogo comecado: state = ready/play
  const auto1 = await g.autoContentUpdate();
  check("a atualizacao automatica espera a partida terminar", auto1 === false && salvos === 0, "salvou " + salvos);
  g.titleScreen();                          // volta pro menu
  const auto2 = await g.autoContentUpdate();
  check("no menu, a atualizacao automatica aplica", auto2 === true && salvos === 1, "salvou " + salvos);

  global.fetch = realFetch;
  delete global.AndroidUpdate;

  console.log(falhas ? `\n${falhas} problema(s) encontrado(s)` : "\nNenhum problema encontrado");
  process.exit(falhas ? 1 : 0);
})();
