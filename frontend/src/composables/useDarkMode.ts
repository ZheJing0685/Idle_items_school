import { ref, watch } from 'vue';

const STORAGE_KEY = 'greenloop-theme';
const MEDIA = '(prefers-color-scheme: dark)';

export type ThemeMode = 'dark' | 'light' | 'system';

function getSystemDark(): boolean {
  return window.matchMedia(MEDIA).matches;
}

function readStored(): ThemeMode {
  try {
    const v = localStorage.getItem(STORAGE_KEY);
    if (v === 'dark' || v === 'light') return v;
  } catch { /* 无痕/隐私模式可能无法访问 localStorage */ }
  return 'system';
}

const themeMode = ref<ThemeMode>(readStored());

function apply(mode: ThemeMode) {
  const root = document.documentElement;
  root.classList.remove('dark', 'light');

  if (mode === 'dark') {
    root.classList.add('dark');
    root.style.colorScheme = 'dark';
  } else if (mode === 'light') {
    root.classList.add('light');
    root.style.colorScheme = 'light';
  } else {
    /* system: 让 CSS 中的 @media (prefers-color-scheme: dark) :root:not(.light) 处理 */
    root.style.colorScheme = '';
  }
}

const isDark = ref(
  themeMode.value === 'dark' ||
  (themeMode.value === 'system' && getSystemDark()),
);

function syncIsDark() {
  if (themeMode.value === 'dark') isDark.value = true;
  else if (themeMode.value === 'light') isDark.value = false;
  else isDark.value = getSystemDark();
}

apply(themeMode.value);

/* 监听系统暗色变化 — 仅在 system 模式下自动跟随 */
const mq = window.matchMedia(MEDIA);
mq.addEventListener('change', (e) => {
  if (themeMode.value === 'system') {
    isDark.value = e.matches;
    /* apply() 不需要调用，CSS media query 会处理 */
  }
});

watch(themeMode, (mode) => {
  /* 添加过渡 class 实现平滑切换 */
  const root = document.documentElement;
  root.classList.add('theme-transitioning');
  apply(mode);
  syncIsDark();
  try { localStorage.setItem(STORAGE_KEY, mode); } catch { /* 隐私模式可能拒绝写入 */ }
  setTimeout(() => root.classList.remove('theme-transitioning'), 350);
});

/* prefers-reduced-motion */
const rmq = window.matchMedia('(prefers-reduced-motion: reduce)');
document.documentElement.classList.toggle('reduced-motion', rmq.matches);
rmq.addEventListener('change', () => {
  document.documentElement.classList.toggle('reduced-motion', rmq.matches);
});

export function useDarkMode() {
  function toggle() {
    themeMode.value = isDark.value ? 'light' : 'dark';
  }

  function setMode(mode: ThemeMode) {
    themeMode.value = mode;
  }

  function cycleMode() {
    if (themeMode.value === 'dark') setMode('light');
    else if (themeMode.value === 'light') setMode('system');
    else setMode('dark');
  }

  return {
    isDark,
    themeMode,
    toggle,
    setMode,
    cycleMode,
  };
}
