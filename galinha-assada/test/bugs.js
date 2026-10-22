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
  for (let lv = 1; lv <= g.totalLevels; lv++) {
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
g.setLevel(30); check("fase 30 nao e mais a ultima", g.totalLevels > 30, g.totalLevels);

// 6) velocidades crescem e a galinha nunca fica mais lenta que os gatos
g.setDiff("normal");
let anterior = 0;
for (let lv = 1; lv <= g.totalLevels; lv++) {
  g.setLevel(lv);
  check("gatos nao voltam a ficar lentos na fase " + lv, g.ghostSpeed > 0, g.ghostSpeed);
  check("galinha mais rapida que os gatos na fase " + lv, g.playerSpeed > g.ghostSpeed,
    "fase " + lv + " galinha " + g.playerSpeed.toFixed(2) + " gatos " + g.ghostSpeed.toFixed(2));
  check("velocidade cresce na fase " + lv, lv === 1 || g.ghostSpeed >= anterior,
    "fase " + lv + " " + g.ghostSpeed.toFixed(2) + " vs " + anterior.toFixed(2));
  anterior = g.ghostSpeed;
}

// 7) relogio: congela os gatos no lugar
g.start();
g.step(3.4);                       // termina a contagem: state = play
const cat = g.ghosts[0];
cat.active = true; cat.state = "chase"; cat.releaseAt = 0;
g.collectPowerup("clock");
check("relogio liga o gelo", g.freezeTimer > 0, g.freezeTimer);
const pos0 = [cat.r, cat.c, cat.prog];
for (let i = 0; i < 5; i++) g.step(0.05);
check("relogio congela os gatos", cat.r === pos0[0] && cat.c === pos0[1] && cat.prog === pos0[2],
  JSON.stringify([cat.r, cat.c, cat.prog]) + " era " + JSON.stringify(pos0));

g.freezeTimer = 0;   // o relogio do teste anterior ainda congelava os gatos

// 8) farol: protege e afasta os gatos
const antesVidas = g.lives;
cat.r = g.player.r; cat.c = g.player.c; cat.prog = 0;   // gato em cima da galinha
g.collectPowerup("farol");
check("farol liga a luz", g.farolTimer > 0, g.farolTimer);
g.step(0.1);
check("farol protege a galinha", g.state === "play" && g.lives === antesVidas, g.state + " vidas " + g.lives);
let maxD = 0;
for (let i = 0; i < 12 && g.farolTimer > 0; i++){
  g.step(0.1);
  maxD = Math.max(maxD, Math.abs(cat.r - g.player.r) + Math.abs(cat.c - g.player.c));
}
check("farol afasta o gato", maxD >= 1,
  "distancia maxima " + maxD + " | cat=" + JSON.stringify({st:cat.state,r:cat.r,c:cat.c,dir:cat.dir,want:cat.want,prog:cat.prog,sp:cat.speed}) +
  " | player=" + JSON.stringify({r:g.player.r,c:g.player.c,st:g.state}) +
  " | farol=" + g.farolTimer + " freeze=" + g.freezeTimer + " diff=" + g.CFG.diff);

// 8b) painel de conquistas: tudo listado, em duas colunas
g.openAchievements();
const achHtml = document.getElementById("panel").innerHTML || "";
check("conquistas em grade", achHtml.indexOf("ach-grid") >= 0, "sem grade");
const achCards = (achHtml.match(/class="ach[ "]/g) || []).length;
check("todas as conquistas aparecem", achCards === g.ACH.length, achCards + " de " + g.ACH.length);
check("nomes das conquistas no painel", g.ACH.every(a => achHtml.indexOf(a.name) >= 0), "faltou nome");

// 8c) cachorro: entra na fase 3, tem IA propria e nao entra na casa
g.setDiff("normal");
g.start(); g.setLevel(2);
check("sem cachorro na fase 2", !g.ghosts.some(x => x.dog && x.active), g.ghosts.map(x => x.name).join(","));
g.start(); g.setLevel(6);
const dog = g.ghosts.find(x => x.dog && x.active);
check("cachorro na fase 6", !!dog && dog.name === "Cachorro", dog && dog.name);
let casaOk = true, dogPos = "";
for (let i = 0; i < 200 && dog; i++) {
  g.step(0.05);
  if (dog.state !== "chase" && dog.state !== "eaten") continue;   // saindo da casa ainda pode estar dentro
  if (dog.r >= 9 && dog.r <= 11 && dog.c >= 8 && dog.c <= 12) { casaOk = false; dogPos = "cachorro na casa " + dog.r + "," + dog.c; break; }
}
check("cachorro nao entra na casa", casaOk, dogPos);

function sairDaContagem(){
  for (let i = 0; i < 140 && g.state === "ready"; i++) g.step(0.05);
  return g.state === "play";
}

g.start(); g.setLevel(5);
let desenhou = true;
try { g.render(); } catch (e) { desenhou = false; }
check("desenho com cachorro nao quebra", desenhou, "erro no render");
check("na fase de chefe o cachorro vira chefe", !g.ghosts.some(x => x.dog && x.active),
  g.ghosts.map(x => x.name).join(","));

// investida: late e acelera perto da galinha
g.start(); g.setLevel(6);
sairDaContagem();
const d2 = g.ghosts.find(x => x.dog && x.active);
g.collectPowerup("farol");            // protege enquanto o cachorro fica em cima
d2.state = "chase"; d2.r = g.player.r; d2.c = g.player.c; d2.prog = 0;
let dashSeen = false;
for (let i = 0; i < 100; i++) {
  g.step(0.05);
  if (d2.dashT > 0) { dashSeen = true; break; }
}
check("cachorro da investidas", dashSeen, "dashT=" + d2.dashT + " estado=" + g.state);

// com pimenta o cachorro vira comida e conta no progresso
g.start(); g.setLevel(6);
sairDaContagem();
const d3 = g.ghosts.find(x => x.dog && x.active);
const dogs0 = g.prog.dogs || 0;
g.collectPowerup("chili");
d3.state = "chase"; d3.r = g.player.r; d3.c = g.player.c; d3.prog = 0;
g.step(0.05);
check("cachorro assustado pode ser comido", g.prog.dogs === dogs0 + 1 && d3.state === "eaten",
  "dogs " + dogs0 + " -> " + g.prog.dogs + " estado " + d3.state);

// 9) skins
check("a skin padrao e a classica", g.skin === "classica" && g.skinUnlocked(g.skinById("classica")), g.skin);
check("skin bloqueada nao entra", g.setSkin("fantasma") === false && g.skin === "classica", g.skin);
g.prog.clones = 5;
check("skin desbloqueada entra", g.setSkin("fantasma") === true && g.skin === "fantasma", g.skin);
check("a skin troca a paleta", !!g.skinPal() && g.skinPal().g1 === "#cbd5e1", JSON.stringify(g.skinPal()));
g.setSkin("classica");

// 10) o atualizador: probe barato, URL valida e download do jogo novo
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
