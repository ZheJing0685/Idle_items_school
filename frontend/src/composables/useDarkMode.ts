import { ref, watch } from 'vue';

const STORAGE_KEY = 'theme-mode';
const MEDIA = '(prefers-color-scheme: dark)';

function initial(): boolean {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored === 'dark') return true;
  if (stored === 'light') return false;
  return window.matchMedia(MEDIA).matches;
}

const isDark = ref(initial());

function apply(v: boolean) {
  const root = document.documentElement;
  root.classList.toggle('dark', v);
  localStorage.setItem(STORAGE_KEY, v ? 'dark' : 'light');
  root.style.colorScheme = v ? 'dark' : 'light';
}

apply(isDark.value);

const mq = window.matchMedia(MEDIA);
mq.addEventListener('change', (e) => {
  if (!localStorage.getItem(STORAGE_KEY)) {
    isDark.value = e.matches;
  }
});

watch(isDark, apply);

/* prefers-reduced-motion */
const rmq = window.matchMedia('(prefers-reduced-motion: reduce)');
document.documentElement.classList.toggle('reduced-motion', rmq.matches);
rmq.addEventListener('change', () => {
  document.documentElement.classList.toggle('reduced-motion', rmq.matches);
});

export function useDarkMode() {
  return {
    isDark,
    toggle: () => { isDark.value = !isDark.value; },
  };
}
