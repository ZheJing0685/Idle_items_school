const express = require('express');
const cors = require('cors');
const crypto = require('crypto');

const app = express();
app.use(cors());
app.use(express.json({ limit: '10mb' }));

const DIM = 384;
const SEED = 42;

function seededRandom(seed) {
  let s = seed;
  return () => {
    s = (s * 1664525 + 1013904223) & 0xffffffff;
    return (s >>> 0) / 0xffffffff;
  };
}

const projections = [];
const rng = seededRandom(SEED);
for (let i = 0; i < DIM; i++) {
  const row = [];
  for (let j = 0; j < 128; j++) row.push(rng() * 2 - 1);
  projections.push(row);
}

function embed(text) {
  const normalized = text.toLowerCase().replace(/[^a-z0-9\u4e00-\u9fff\s]/g, ' ').trim();
  const tokens = normalized.split(/\s+/).filter(Boolean);
  if (tokens.length === 0) return new Array(DIM).fill(0);

  const ngrams = new Set();
  for (const t of tokens) {
    ngrams.add(t);
    for (let i = 0; i < t.length - 2; i++) ngrams.add(t.substring(i, i + 3));
  }

  const vec = new Array(DIM).fill(0);
  for (const key of ngrams) {
    const hash = crypto.createHash('md5').update(key).digest();
    for (let i = 0; i < DIM; i++) {
      vec[i] += projections[i][Math.abs(hash[i % hash.length]) % 128];
    }
  }

  const mag = Math.sqrt(vec.reduce((s, v) => s + v * v, 0));
  if (mag > 0) for (let i = 0; i < DIM; i++) vec[i] /= mag;
  return vec;
}

app.post('/v1/embeddings', (req, res) => {
  try {
    const { input } = req.body;
    if (!input) return res.status(400).json({ error: 'Missing input' });
    const inputs = Array.isArray(input) ? input : [input];
    const data = inputs.map((text, i) => ({
      object: 'embedding', index: i, embedding: embed(String(text)),
    }));
    res.json({
      object: 'list', data,
      model: 'text-embedding-3-large',
      usage: { prompt_tokens: inputs.join(' ').length, total_tokens: inputs.join(' ').length },
    });
  } catch (e) { res.status(500).json({ error: e.message }); }
});

app.get('/v1/models', (_, res) => {
  res.json({ object: 'list', data: [
    { id: 'text-embedding-3-large', object: 'model', created: Date.now(), owned_by: 'local' },
  ]});
});

app.get('/health', (_, res) => res.json({ status: 'ok' }));

const PORT = process.env.PORT || 11434;
app.listen(PORT, () => console.log(`Local embedding service on :${PORT}`));
