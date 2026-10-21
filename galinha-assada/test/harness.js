#!/usr/bin/env node
// Testes do Galinha Assada (index.html), rodando o script do jogo em Node
// com um DOM mínimo. Não precisa de navegador nem de rede.
//
//   node test/harness.js
//
// Cobre: o modo Zen (dificuldade nova, ciclo do menu, HUD, morte sem perder
// vida, chefe e clone continuam aparecendo) e a regressão das outras
// dificuldades (morte normal, fim de jogo, recorde por dificuldade).

const fs = require("fs");
const path = require("path");

// ---------------- DOM mínimo ----------------
function el(id) {
  const e = {
    _id: id, value: "", textContent: "", className: "", disabled: false,
    style: new Proxy({}, { get: () => "", set: () => true }),
    dataset: {}, options: [], scrollTop: 0, scrollHeight: 0,
    classList: { toggle() {}, add() {}, remove() {}, contains() { return false; } },
    focus() {}, blur() {}, click() {}, appendChild() {}, removeChild() {},
    addEventListener() {}, removeEventListener() {},
    setAttribute() {}, getAttribute() { return null; },
    querySelector() { return el("x"); }, querySelectorAll() { return []; },
    closest() { return null; },
    getBoundingClientRect() { return { top: 0, left: 0, width: 0, height: 0 }; },
    getContext() { return ctx2d; },
    width: 672, height: 672,
  };
  Object.defineProperty(e, "innerHTML", {
    get() { return this._ih || ""; },
    set(v) { this._ih = String(v); }
  });
  return e;
}

// contexto 2D falso: qualquer método é no-op, propriedades viram valores úteis
const grad = { addColorStop() {} };
const ctx2d = new Proxy({}, {
  get(t, k) {
    if (k === "measureText") return () => ({ width: 10 });
    if (k === "createLinearGradient" || k === "createRadialGradient") return () => grad;
    if (k === "getImageData") return () => ({ data: new Uint8ClampedArray(4) });
    if (k === "canvas") return el("canvas");
    if (typeof k === "string" && /^(font|fillStyle|strokeStyle|lineWidth|globalAlpha|globalCompositeOperation|textAlign|textBaseline|lineCap|lineJoin|filter|shadowBlur|shadowColor|imageSmoothingEnabled|miterLimit|lineDashOffset|shadowOffsetX|shadowOffsetY)$/.test(k)) return "";
    return () => {};
  },
  set() { return true; }
});

const cache = {};
function getEl(sel) { if (!cache[sel]) cache[sel] = el(sel); return cache[sel]; }

const store = {};
global.localStorage = {
  getItem: k => (k in store ? store[k] : null),
  setItem: (k, v) => { store[k] = String(v); },
  removeItem: k => { delete store[k]; },
};
global.document = {
  getElementById: getEl,
  querySelector: getEl, querySelectorAll: () => [],
  createElement: () => el("created"), createTextNode: () => ({}),
  body: el("body"), addEventListener() {}, activeElement: null,
  documentElement: el("html"),
};
global.window = global;
global.addEventListener = () => {};
global.removeEventListener = () => {};
global.navigator = {
  userAgent: "node", vibrate: () => true, share: null,
  getGamepads: () => [],
  mediaDevices: null,
};
global.location = { href: "http://localhost/", origin: "http://localhost", protocol: "http:", pathname: "/" };
global.matchMedia = () => ({ matches: false, addEventListener() {}, addListener() {} });
global.visualViewport = null;
global.performance = { now: () => Date.now() };
global.requestAnimationFrame = () => 0;
global.cancelAnimationFrame = () => {};
global.AudioContext = function () {
  // AudioParam falso: qualquer método (setValueAtTime, ramp, etc.) é no-op
  const param = () => new Proxy({ value: 0 }, {
    get(t, k) { if (k in t) return t[k]; return () => {}; },
    set(t, k, v) { t[k] = v; return true; }
  });
  const node = () => ({
    connect() {}, disconnect() {}, start() {}, stop() {},
    gain: param(), frequency: param(), detune: param(), pan: param(),
    type: "sine", buffer: null, Q: param(),
    createBufferSource() { return node(); },
    createGain() { return node(); },
    createOscillator() { return node(); },
    createBiquadFilter() { return node(); },
    createStereoPanner() { return node(); },
    createDynamicsCompressor() { return node(); },
    createBuffer(ch, len) { return { getChannelData: () => new Float32Array(len) }; },
  });
  return {
    currentTime: 0, state: "running", resume() {}, close() {},
    destination: node(),
    createGain: node, createOscillator: node, createBiquadFilter: node,
    createBufferSource: node, createDynamicsCompressor: node, createBuffer: node,
    sampleRate: 44100,
  };
};
global.webkitAudioContext = global.AudioContext;
global.fetch = async () => ({ ok: false, status: 0, json: async () => ({}) });
global.alert = () => {};
global.prompt = () => "Player";
global.confirm = () => true;

// ---------------- executa o script do jogo ----------------
const html = fs.readFileSync(path.join(__dirname, "..", "index.html"), "utf8");
const m = html.match(/<script>([\s\S]*)<\/script>/);
if (!m) { console.error("script do jogo não encontrado"); process.exit(1); }
(0, eval)(m[1]);
const g = global.__galinha;
if (!g) { console.error("gancho __galinha não encontrado"); process.exit(1); }

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

console.log(falhas ? `\n${falhas} falha(s)` : "\nTudo certo");
process.exit(falhas ? 1 : 0);
