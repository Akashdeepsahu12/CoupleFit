#!/usr/bin/env node
// ASCII Diorama Live Server with Active Codebase Reactivity
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import * as esbuild from 'esbuild';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const dioramaDir = __dirname;
const projectRoot = path.resolve(__dirname, '..');
const port = parseInt(process.argv[2] || process.env.PORT || '5173', 10);

console.log(`[Diorama Server] Initializing...`);
console.log(`  Diorama Directory: ${dioramaDir}`);
console.log(`  Project Root:      ${projectRoot}`);

// Pre-bundle main.js
const bundle = async () => {
  const entry = path.join(dioramaDir, 'main.js');
  const outfile = path.join(dioramaDir, 'build', 'main.js');
  fs.mkdirSync(path.join(dioramaDir, 'build'), { recursive: true });
  await esbuild.build({
    entryPoints: [entry],
    bundle: true,
    format: 'esm',
    outfile: outfile,
    logLevel: 'warning'
  });
};

try {
  await bundle();
  console.log(`[Diorama Server] Initial bundle completed.`);
} catch (err) {
  console.error(`[Diorama Server] Bundle failed:`, err);
}

// Active SSE Clients
const clients = new Set();

const broadcastCodeChange = (relativePath) => {
  const payload = JSON.stringify({
    type: 'code_change',
    file: relativePath.replace(/\\/g, '/'),
    timestamp: Date.now(),
    timeStr: new Date().toLocaleTimeString()
  });
  console.log(`[Codebase React] Broadcast file change: ${relativePath}`);
  for (const res of clients) {
    try {
      res.write(`data: ${payload}\n\n`);
    } catch {
      clients.delete(res);
    }
  }
};

// File Watcher on projectRoot
const ignoredPatterns = [
  /node_modules/,
  /(^|[\\/])\.git([\\/]|$)/,
  /(^|[\\/])\.diorama([\\/]|$)/,
  /(^|[\\/])\.diorama_src([\\/]|$)/,
  /(^|[\\/])\.gradle([\\/]|$)/,
  /(^|[\\/])\.kotlin([\\/]|$)/,
  /(^|[\\/])build([\\/]|$)/,
  /(^|[\\/])\.idea([\\/]|$)/,
  /(^|[\\/])\.system_generated([\\/]|$)/,
  /\.lock$/,
  /\.bin$/,
  /\.tmp$/,
  /~$/
];

let debounceTimer = null;
const changedFiles = new Set();

try {
  fs.watch(projectRoot, { recursive: true }, (eventType, filename) => {
    if (!filename) return;
    const norm = filename.replace(/\\/g, '/');
    if (ignoredPatterns.some(pat => pat.test(norm))) return;

    changedFiles.add(norm);
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      const recent = Array.from(changedFiles);
      changedFiles.clear();
      if (recent.length > 0) {
        broadcastCodeChange(recent[0]);
      }
    }, 200);
  });
  console.log(`[Diorama Server] Watching ${projectRoot} for real-time code changes...`);
} catch (e) {
  console.warn(`[Diorama Server] Recursive watch failed: ${e.message}`);
}

const MIME = {
  '.html': 'text/html',
  '.js': 'text/javascript',
  '.jpg': 'image/jpeg',
  '.png': 'image/png',
  '.glb': 'model/gltf-binary',
  '.gltf': 'model/gltf+json',
  '.json': 'application/json',
  '.css': 'text/css'
};

const server = http.createServer(async (req, res) => {
  const url = decodeURIComponent(req.url.split('?')[0]);

  // Server-Sent Events Endpoint
  if (url === '/api/code-events') {
    res.writeHead(200, {
      'Content-Type': 'text/event-stream',
      'Cache-Control': 'no-cache, no-transform',
      'Connection': 'keep-alive',
      'Access-Control-Allow-Origin': '*'
    });
    res.write(`data: {"type":"connected","message":"Watching codebase for reactions"}\n\n`);
    clients.add(res);
    req.on('close', () => clients.delete(res));
    return;
  }

  // Manual Trigger Endpoint
  if (url === '/api/trigger-pulse' && req.method === 'POST') {
    broadcastCodeChange('manual_api_trigger.kt');
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'ok', triggered: true }));
    return;
  }

  // Re-bundle main.js on request
  if (url === '/build/main.js') {
    try {
      await bundle();
    } catch (err) {
      console.error(err);
    }
  }

  const p = path.join(dioramaDir, url === '/' ? 'index.html' : url);
  if (!p.startsWith(dioramaDir) || !fs.existsSync(p) || fs.statSync(p).isDirectory()) {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    return res.end('404 Not Found');
  }

  res.writeHead(200, {
    'Content-Type': MIME[path.extname(p)] || 'application/octet-stream',
    'Cache-Control': 'no-store'
  });
  fs.createReadStream(p).pipe(res);
});

server.listen(port, '127.0.0.1', () => {
  console.log(`\n========================================================`);
  console.log(`🌸 ASCII DIORAMA CODEBASE REACTOR ONLINE`);
  console.log(`   URL: http://127.0.0.1:${port}/`);
  console.log(`   • Responsive Monospace Tilt-Shift Miniature`);
  console.log(`   • Real-Time File Watcher on CoupleFit Codebase`);
  console.log(`   • Space = Pause/Resume | C = Simulate Code Pulse`);
  console.log(`========================================================\n`);
});
