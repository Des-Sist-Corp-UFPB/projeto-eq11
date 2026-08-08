/* =============================================================================
   StudyAI — JavaScript client-side
   Responsável por: CSRF do HTMX, flip do cartão, navegação do carrossel,
   seleção de chips e contador de palavras. NÃO faz chamadas de IA (isso é no backend).
   ============================================================================= */

// ----- CSRF: injeta o token do Spring Security em toda requisição HTMX -----
// O token e o nome do header vêm das meta tags renderizadas pelo layout Thymeleaf.
document.addEventListener('DOMContentLoaded', function () {
  document.body.addEventListener('htmx:configRequest', function (evt) {
    var tokenMeta = document.querySelector('meta[name="_csrf"]');
    var headerMeta = document.querySelector('meta[name="_csrf_header"]');
    if (tokenMeta && headerMeta) {
      evt.detail.headers[headerMeta.getAttribute('content')] = tokenMeta.getAttribute('content');
    }
  });
});

// ----- Seleção de chips (ex.: banca). Atualiza um input hidden associado. -----
function selectChip(el, hiddenId) {
  var parent = el.parentElement;
  parent.querySelectorAll('.chip').forEach(function (c) { c.classList.remove('selected'); });
  el.classList.add('selected');
  var hidden = document.getElementById(hiddenId);
  if (hidden) hidden.value = el.getAttribute('data-valor') || el.textContent.trim();
}

// ----- Contador de palavras de um textarea -----
function updateWC(el, targetId) {
  var words = el.value.trim().split(/\s+/).filter(function (w) { return w.length > 0; }).length;
  var alvo = document.getElementById(targetId);
  if (alvo) alvo.textContent = words + (words === 1 ? ' palavra' : ' palavras');
}

// =============================================================================
//  CARROSSEL DE FLASHCARDS (client-side)
//  O fragmento devolvido pelo HTMX chama fcInit() após preencher window.__cards.
// =============================================================================
window.__cards = [];
window.__idx = 0;

function fcInit() {
  window.__idx = 0;
  fcRender();
}

function fcRender() {
  var cards = window.__cards || [];
  if (!cards.length) return;
  var card = cards[window.__idx];

  var elFront = document.getElementById('fc-front');
  var elBack = document.getElementById('fc-back');
  var elCounter = document.getElementById('fc-counter');
  var elBar = document.getElementById('fc-bar');
  var elCard = document.getElementById('fc-card');

  if (elCard) elCard.classList.remove('flipped'); // sempre volta para a frente ao trocar
  if (elFront) elFront.textContent = card.frente;
  if (elBack) elBack.textContent = card.verso;
  if (elCounter) elCounter.textContent = (window.__idx + 1) + ' / ' + cards.length;
  if (elBar) elBar.style.width = (((window.__idx + 1) / cards.length) * 100) + '%';
}

function fcFlip() {
  var elCard = document.getElementById('fc-card');
  if (elCard) elCard.classList.toggle('flipped');
}

function fcNext() {
  var cards = window.__cards || [];
  if (!cards.length) return;
  window.__idx = (window.__idx + 1) % cards.length;
  fcRender();
}

function fcPrev() {
  var cards = window.__cards || [];
  if (!cards.length) return;
  window.__idx = (window.__idx - 1 + cards.length) % cards.length;
  fcRender();
}

// =============================================================================
//  ROADMAP DE ESTUDOS (client-side)
//  As semanas vêm renderizadas pelo servidor; aqui só abrimos/fechamos o bloco.
// =============================================================================

// Recebe o cabeçalho clicado e alterna a semana correspondente.
function toggleSemana(el) {
  var semana = el.closest('.semana');
  if (semana) semana.classList.toggle('aberta');
}
