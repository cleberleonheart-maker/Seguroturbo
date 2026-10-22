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
if (!m) { console.error("script do jogo nao encontrado"); process.exit(1); }
(0, eval)(m[1]);
const g = global.__galinha;
if (!g) { console.error("gancho __galinha nao encontrado"); process.exit(1); }
module.exports = { g, getEl, el, ctx2d };
