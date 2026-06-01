import { spawn } from 'child_process';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const PORT = 5173;
const CLOUDFLARED = 'C:\\Program Files (x86)\\cloudflared\\cloudflared.exe';

console.log(`\n[1/2] Starting Vite dev server on port ${PORT}...`);

const vite = spawn('npx', ['vite'], { cwd: __dirname, stdio: 'inherit', shell: true });

console.log('[2/2] Starting Cloudflare Tunnel...');

const cloudflared = spawn(CLOUDFLARED, ['tunnel', '--url', `http://localhost:${PORT}`], {
  stdio: 'pipe',
  shell: false,
});

let urlFound = false;

function checkOutput(data) {
  if (urlFound) return;
  const output = data.toString();
  const match = output.match(/https:\/\/[a-z0-9-]+\.trycloudflare\.com/);
  if (match) {
    urlFound = true;
    console.log('\n========================================');
    console.log('  Tunnel ready!');
    console.log(`  Mobile URL: ${match[0]}`);
    console.log('========================================\n');
  }
}

cloudflared.stdout.on('data', checkOutput);
cloudflared.stderr.on('data', checkOutput);

vite.on('error', (err) => {
  console.error('Vite failed:', err.message);
  process.exit(1);
});

process.on('SIGINT', () => {
  cloudflared.kill();
  vite.kill();
  process.exit(0);
});
