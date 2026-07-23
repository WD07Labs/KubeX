# KubeX

> Develop KubeJS with a dedicated workspace, TypeScript support, Rhino compatibility and source-level debugging.

KubeX is a Minecraft mod that improves the KubeJS development workflow.

Instead of writing scripts directly inside `kubejs/`, you develop inside a dedicated `kubex/` workspace, build using modern JavaScript tooling, and let KubeX publish the compiled output back into KubeJS.

KubeX also provides runtime debugging, Rhino compatibility fixes, and source map lookup so you debug the code you actually wrote—not bundled JavaScript.

---

# Why KubeX?

Traditional KubeJS development has several pain points.

- Scripts are edited directly inside `kubejs/`
- Large projects become difficult to organize
- Bundled JavaScript is hard to debug
- Callback exceptions may fail silently
- Generated line numbers don't match your original source

KubeX solves these problems by introducing a dedicated workspace, Rhino-aware post-processing, runtime diagnostics, and source-level debugging.

---

# Architecture

```text
VSCode

↓

kubex/src

↓

esbuild + Babel

↓

KubeX

 • Rhino compatibility lowering
 • Runtime debug instrumentation
 • Source map support
 • Validation

↓

kubejs

↓

Minecraft
```

---

# Features

- Dedicated JavaScript / TypeScript workspace
- Automatic synchronization into `kubejs`
- Rhino compatibility post-processing
- Runtime callback debugging
- Source map lookup
- Automatic `/reload`
- ProbeJS workspace integration

---

# Workspace

Running

```mcfunction
/kubex init js
```

or

```mcfunction
/kubex init ts
```

creates

```text
kubex/
├── .probe/
├── .vscode/
├── esbuild.config.mjs
├── output/
├── package.json
└── src/
    ├── assets/
    ├── config/
    ├── data/
    ├── client_scripts/
    ├── server_scripts/
    └── startup_scripts/
```

If ProbeJS development assets already exist, KubeX copies them into the workspace automatically.

---

# Build

KubeX currently uses Node-based tooling.

```bash
cd kubex
npm install
npm run build
```

The generated bundles are written to

```text
kubex/output/
```

---

# Sync

After building,

```mcfunction
/kubex sync
```

KubeX will

- validate generated JavaScript
- apply Rhino compatibility fixes
- inject runtime debug instrumentation (optional)
- publish files into `kubejs`
- copy source maps
- automatically execute `/reload`

KubeX also performs an automatic sync during server startup when possible.

---

# Rhino Compatibility

Before publishing into `kubejs/`, KubeX performs Java-side post-processing on bundled output.

Current transformations include

- package import rewriting
- Rhino compatibility fixes
- Babel loop helper fixes
- runtime debug instrumentation
- source map preservation

---

# Source Maps

KubeX copies generated `.map` files into `kubejs`.

Using

```mcfunction
/kubex doctor
```

generated locations such as

```text
server_scripts/main.js:412
```

become

```text
server_scripts/main.ts:37:19

pokemon.getDisplayName(true)
        ^
```

making bundled stack traces much easier to understand.

---

# Commands

### Initialize

```mcfunction
/kubex init js
```

```mcfunction
/kubex init ts
```

---

### Synchronize

```mcfunction
/kubex sync
```

---

### Debug

```mcfunction
/kubex debug
```

```mcfunction
/kubex debug on
```

```mcfunction
/kubex debug off
```

---

### Doctor

```mcfunction
/kubex doctor <group> <line> [column]
```

or

```mcfunction
/kubex doctor "main.js:1450:1"
```

---

# Development Flow

```text
1. Install the KubeX mod

↓

2. /kubex init ts

↓

3. Open kubex/ in VSCode

↓

4. npm install

↓

5. Write code

↓

6. npm run build

↓

7. /kubex sync

↓

8. Play and debug using /kubex doctor
```

---

# Roadmap

## Planned

- [ ] Runtime Trace
- [ ] Rhino Doctor
- [ ] Performance Profiler

---